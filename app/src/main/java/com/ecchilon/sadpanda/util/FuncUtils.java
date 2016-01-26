package com.ecchilon.sadpanda.util;

import rx.functions.Func1;

public class FuncUtils {
	public static <T> Func1<? super T, Boolean> not(Func1<? super T, Boolean> predicate) {
		return input -> !predicate.call(input);
	}
}
