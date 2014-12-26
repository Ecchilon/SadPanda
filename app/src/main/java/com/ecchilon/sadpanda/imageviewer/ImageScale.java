package com.ecchilon.sadpanda.imageviewer;

import com.google.common.collect.ImmutableMap;

public enum ImageScale {
	HUNDRED,
	DOUBLE,
	FIT_TO_SCREEN;

	private static final ImmutableMap<String, ImageScale> scaleMap = ImmutableMap.<String, ImageScale>builder()
			.put("100", HUNDRED)
			.put("double", DOUBLE)
			.put("fit", FIT_TO_SCREEN)
			.build();


	public static ImageScale getScale(String scaleString) {
		if(scaleMap.containsKey(scaleString)) {
			return scaleMap.get(scaleString);
		}
		else {
			return FIT_TO_SCREEN;
		}
	}
}
