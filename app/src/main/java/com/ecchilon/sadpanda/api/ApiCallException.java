package com.ecchilon.sadpanda.api;

import lombok.Data;
import lombok.EqualsAndHashCode;
import okhttp3.Response;

@Data
@EqualsAndHashCode(callSuper = true)
public class ApiCallException extends RuntimeException {

	private final ApiErrorCode code;

	public ApiCallException(ApiErrorCode code) {
		this.code = code;
	}

	public ApiCallException(ApiErrorCode code, String detailMessage) {
		super(detailMessage);
		this.code = code;
	}
}
