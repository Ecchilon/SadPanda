package com.ecchilon.sadpanda.imageviewer.data;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ThumbEntry {
	private String url;
	private int width;
	private int height;
	private int offset;
}
