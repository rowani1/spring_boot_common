package com.common.core.exception;

import com.common.core.result.IResultCode;
import com.common.core.result.ResultCode;

public class BizException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int code;

    public BizException(IResultCode resultCode) {
        super(resultCode != null ? resultCode.getMessage() : ResultCode.INTERNAL_ERROR.getMessage());
        this.code = resultCode != null ? resultCode.getCode() : ResultCode.INTERNAL_ERROR.getCode();
    }

    public BizException(IResultCode resultCode, String message) {
        super(message);
        this.code = resultCode != null ? resultCode.getCode() : ResultCode.INTERNAL_ERROR.getCode();
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BizException(String message) {
        super(message);
        this.code = ResultCode.INTERNAL_ERROR.getCode();
    }

    public BizException(String message, Throwable cause) {
        super(message, cause);
        this.code = ResultCode.INTERNAL_ERROR.getCode();
    }

    public int getCode() {
        return code;
    }
}
