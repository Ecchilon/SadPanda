package com.ecchilon.sadpanda.api;

import lombok.Data;
import lombok.EqualsAndHashCode;
import okhttp3.Response;

/**
 * Created by SkyArrow on 2014/2/19.
 * @author alex on 2014/9/23.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ApiCallException extends RuntimeException {

    private final ApiErrorCode code;
    private final Response response;

    public ApiCallException(ApiErrorCode code) {
        this.code = code;
        response = null;
    }

    public ApiCallException(ApiErrorCode code, Response response) {
        this.code = code;
        this.response = response;
    }

    public ApiCallException(ApiErrorCode code, String detailMessage) {
        super(detailMessage);
        this.code = code;
        response = null;
    }

    public ApiCallException(ApiErrorCode code, Throwable throwable) {
        super(throwable);
        this.code = code;
        response = null;
    }
}
