package com.ecchilon.sadpanda.util;

import android.os.Looper;

public class NetUtils {
	public static void assertNotMainThread() {
	    if(Looper.myLooper() == Looper.getMainLooper()) {
	        throw new IllegalStateException("API can't be called from UI Thread!");
	    }
	}
}
