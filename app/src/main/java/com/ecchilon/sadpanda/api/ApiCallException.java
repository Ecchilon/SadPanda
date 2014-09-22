package com.ecchilon.sadpanda.api;

import org.apache.http.HttpResponse;

/**
 * Created by SkyArrow on 2014/2/19.
 */
public class ApiCallException extends RuntimeException {
    private ApiErrorCode code;
    private String url;
    private HttpResponse response;

    public ApiCallException(ApiErrorCode code) {
        this.code = code;
    }

    public ApiCallException(ApiErrorCode code, String url, HttpResponse response) {
        this.code = code;
        this.url = url;
        this.response = response;
    }

    public ApiCallException(ApiErrorCode code, String detailMessage) {
        super(detailMessage);
        this.code = code;
    }

    public ApiCallException(ApiErrorCode code, Throwable throwable) {
        super(throwable);
        this.code = code;
    }

    public ApiErrorCode getCode() {
        return code;
    }

    public String getUrl() {
        return url;
    }

    public HttpResponse getResponse() {
        return response;
    }
}
