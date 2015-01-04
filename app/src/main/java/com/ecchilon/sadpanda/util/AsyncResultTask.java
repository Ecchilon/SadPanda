package com.ecchilon.sadpanda.util;

import android.os.AsyncTask;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public abstract class AsyncResultTask<Params, Progress, Result> extends AsyncTask<Params, Progress, AsyncResultTask.AsyncTaskResult<Result>> {

	public static interface Callback<T> {
		void onSuccess(T result);
		void onError(Exception e);
	}

	private final boolean mAllowNullResult;

	@Setter
	private Callback<Result> listener;

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

		if(listener != null) {
			if (result.isSuccessful()) {
				if (result.getResult() == null && !mAllowNullResult) {
					listener.onError(new IllegalArgumentException("Empty result not allowed"));
				}
				else {
					listener.onSuccess(result.getResult());
				}
			}
			else {
				listener.onError(result.getError());
			}
		}
	}

	protected abstract Result call(Params... params) throws Exception;

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
