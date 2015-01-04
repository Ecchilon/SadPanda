package com.ecchilon.sadpanda.util;

import android.os.AsyncTask;
import lombok.Getter;

public abstract class AsyncResultTask<Params, Progress, Result> extends AsyncTask<Params, Progress, AsyncResultTask.AsyncTaskResult<Result>> {

	private final boolean mAllowNullResult;

	public AsyncResultTask(boolean allowNullResult) {
		this.mAllowNullResult = allowNullResult;
	}

	@Override
	protected final AsyncTaskResult<Result> doInBackground(Params... params) {
		try {
			 return new AsyncTaskResult<Result>(call(params));
		}
		catch (Exception e) {
			return new AsyncTaskResult<Result>(e);
		}
	}

	@Override
	protected final void onPostExecute(AsyncTaskResult<Result> result) {
		super.onPostExecute(result);

		if(result.isSuccessful()) {
			if(result.getResult() == null && !mAllowNullResult) {
				onError(new IllegalArgumentException("Empty result not allowed"));
			}
			else {
				onSuccess(result.getResult());
			}
		}
		else {
			onError(result.getError());
		}
	}

	protected abstract Result call(Params... params) throws Exception;
	protected abstract void onSuccess(Result result);
	protected abstract void onError(Exception e);

	public static class AsyncTaskResult<T> {
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
}
