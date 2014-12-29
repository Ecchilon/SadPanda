package com.ecchilon.sadpanda.api;

import com.squareup.okhttp.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.http.HttpResponse;

/**
 * Created by SkyArrow on 2014/2/19.
 * @author alex on 2014/9/23.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ApiCallException extends Exception {

    private final ApiErrorCode code;
    private final String url;
    private final Response response;

    public ApiCallException(ApiErrorCode code) {
        this.code = code;
        url = null;
        response = null;
    }

    public ApiCallException(ApiErrorCode code, String url, Response response) {
        this.code = code;
        this.url = url;
        this.response = response;
    }

    public ApiCallException(ApiErrorCode code, String detailMessage) {
        super(detailMessage);
        this.code = code;
        url = null;
        response = null;
    }

    public ApiCallException(ApiErrorCode code, Throwable throwable) {
        super(throwable);
        this.code = code;
        url = null;
        response = null;
    }
}
