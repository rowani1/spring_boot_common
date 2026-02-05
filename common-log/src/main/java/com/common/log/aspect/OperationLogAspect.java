package com.common.log.aspect;

import com.common.log.annotation.OperationLog;
import com.common.log.dto.OperationLogDTO;
import com.common.log.storage.LogStorage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * 操作日志切面
 */
@Slf4j
@Aspect
public class OperationLogAspect {

    private static final String TRACE_ID_KEY = "traceId";
    private static final int DEFAULT_MAX_PARAM_LENGTH = 2000;

    private final LogStorage logStorage;
    private final ObjectMapper objectMapper;
    private final UserInfoProvider userInfoProvider;
    private final Executor executor;
    private final int maxParamLength;

    public OperationLogAspect(LogStorage logStorage, ObjectMapper objectMapper,
                              UserInfoProvider userInfoProvider, Executor executor) {
        this(logStorage, objectMapper, userInfoProvider, executor, DEFAULT_MAX_PARAM_LENGTH);
    }

    public OperationLogAspect(LogStorage logStorage, ObjectMapper objectMapper,
                              UserInfoProvider userInfoProvider, Executor executor, int maxParamLength) {
        this.logStorage = logStorage;
        this.objectMapper = objectMapper;
        this.userInfoProvider = userInfoProvider;
        this.executor = executor;
        this.maxParamLength = maxParamLength > 0 ? maxParamLength : DEFAULT_MAX_PARAM_LENGTH;
    }

    @Around("@annotation(com.common.log.annotation.OperationLog)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long startTime = System.currentTimeMillis();
        OperationLogDTO logDTO = new OperationLogDTO();
        logDTO.setCreateTime(LocalDateTime.now());
        logDTO.setTraceId(MDC.get(TRACE_ID_KEY));

        // 获取注解信息（支持接口代理场景）
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        OperationLog annotation = AnnotationUtils.findAnnotation(method, OperationLog.class);

        if (annotation == null) {
            // 尝试从目标类方法获取
            try {
                Method targetMethod = point.getTarget().getClass()
                        .getMethod(method.getName(), method.getParameterTypes());
                annotation = AnnotationUtils.findAnnotation(targetMethod, OperationLog.class);
            } catch (NoSuchMethodException e) {
                log.warn("无法获取@OperationLog注解");
            }
        }

        if (annotation != null) {
            logDTO.setModule(annotation.module());
            logDTO.setAction(annotation.action());
            logDTO.setDescription(annotation.description());
        }
        logDTO.setMethod(method.getName());

        // 获取请求信息
        boolean saveRequest = annotation != null && annotation.saveRequest();
        boolean saveResponse = annotation != null && annotation.saveResponse();
        fillRequestInfo(logDTO, saveRequest, point);

        // 获取用户信息
        if (userInfoProvider != null) {
            try {
                logDTO.setUserId(userInfoProvider.getUserId());
                logDTO.setUsername(userInfoProvider.getUsername());
            } catch (Exception e) {
                log.debug("获取用户信息失败", e);
            }
        }

        Object result = null;
        try {
            result = point.proceed();
            logDTO.setStatus(1);

            // 保存响应结果
            if (saveResponse && result != null) {
                logDTO.setResponseData(truncate(toJson(result)));
            }
        } catch (Throwable e) {
            logDTO.setStatus(0);
            logDTO.setErrorMsg(truncate(e.getMessage()));
            throw e;
        } finally {
            logDTO.setCostTime(System.currentTimeMillis() - startTime);
            saveLogAsync(logDTO);
        }

        return result;
    }

    private void fillRequestInfo(OperationLogDTO logDTO, boolean saveRequest, ProceedingJoinPoint point) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                logDTO.setRequestUrl(request.getRequestURI());
                logDTO.setIp(getClientIp(request));
                logDTO.setUserAgent(truncate(request.getHeader("User-Agent")));
            }

            // 保存请求参数（过滤不可序列化类型）
            if (saveRequest) {
                Object[] args = point.getArgs();
                if (args != null && args.length > 0) {
                    List<Object> filteredArgs = filterArgs(args);
                    if (!filteredArgs.isEmpty()) {
                        logDTO.setRequestParams(truncate(toJson(filteredArgs)));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("获取请求信息失败", e);
        }
    }

    private List<Object> filterArgs(Object[] args) {
        List<Object> filtered = new ArrayList<>();
        for (Object arg : args) {
            if (arg == null) {
                filtered.add(null);
            } else if (isSerializable(arg)) {
                filtered.add(arg);
            } else {
                filtered.add("[" + arg.getClass().getSimpleName() + "]");
            }
        }
        return filtered;
    }

    private boolean isSerializable(Object obj) {
        return !(obj instanceof ServletRequest)
                && !(obj instanceof ServletResponse)
                && !(obj instanceof MultipartFile)
                && !(obj instanceof InputStream)
                && !(obj instanceof OutputStream);
    }

    private void saveLogAsync(OperationLogDTO logDTO) {
        if (executor != null) {
            executor.execute(() -> saveLog(logDTO));
        } else {
            // 无executor时同步执行
            saveLog(logDTO);
        }
    }

    private void saveLog(OperationLogDTO logDTO) {
        try {
            logStorage.save(logDTO);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }

    private String truncate(String str) {
        if (str == null) {
            return null;
        }
        return str.length() > maxParamLength ? str.substring(0, maxParamLength) + "..." : str;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 用户信息提供者接口
     */
    public interface UserInfoProvider {
        String getUserId();
        String getUsername();
    }
}
