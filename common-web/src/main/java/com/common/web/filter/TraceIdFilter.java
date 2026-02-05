package com.common.web.filter;

import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

public class TraceIdFilter extends OncePerRequestFilter {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String TRACE_ID_KEY = "traceId";

    private static final int MAX_TRACE_ID_LENGTH = 64;
    private static final Pattern TRACE_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9\\-_]+$");

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (!isValidTraceId(traceId)) {
            traceId = generateTraceId();
        }

        MDC.put(TRACE_ID_KEY, traceId);
        response.setHeader(TRACE_ID_HEADER, traceId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID_KEY);
        }
    }

    private boolean isValidTraceId(String traceId) {
        if (!StringUtils.hasText(traceId)) {
            return false;
        }
        if (traceId.length() > MAX_TRACE_ID_LENGTH) {
            return false;
        }
        return TRACE_ID_PATTERN.matcher(traceId).matches();
    }

    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
