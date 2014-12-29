package com.ecchilon.sadpanda.util;

import lombok.Getter;

public class AsyncTaskResult<T> {
	@Getter
	private T result;
	@Getter
	private Exception error;

	public AsyncTaskResult(T result) {
		super();
		this.result = result;
	}

	public boolean isSuccessful() {
		return error == null;
	}

	public AsyncTaskResult(Exception error) {
		super();
		this.error = error;
	}
}