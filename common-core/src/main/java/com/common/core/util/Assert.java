package com.common.core.util;

import com.common.core.exception.BizException;
import com.common.core.result.IResultCode;
import com.common.core.result.ResultCode;
import java.util.Collection;

public final class Assert {

    private Assert() {
    }

    public static void notNull(Object obj, String message) {
        if (obj == null) {
            throw new BizException(ResultCode.BAD_REQUEST, message);
        }
    }

    public static void notNull(Object obj, IResultCode resultCode) {
        if (obj == null) {
            throw new BizException(resultCode);
        }
    }

    public static void notEmpty(String str, String message) {
        if (str == null || str.trim().isEmpty()) {
            throw new BizException(ResultCode.BAD_REQUEST, message);
        }
    }

    public static void notEmpty(Collection<?> collection, String message) {
        if (collection == null || collection.isEmpty()) {
            throw new BizException(ResultCode.BAD_REQUEST, message);
        }
    }

    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new BizException(ResultCode.BAD_REQUEST, message);
        }
    }

    public static void isTrue(boolean expression, IResultCode resultCode) {
        if (!expression) {
            throw new BizException(resultCode);
        }
    }

    public static void isFalse(boolean expression, String message) {
        if (expression) {
            throw new BizException(ResultCode.BAD_REQUEST, message);
        }
    }
}
