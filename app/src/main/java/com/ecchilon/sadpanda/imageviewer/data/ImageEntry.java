package com.ecchilon.sadpanda.imageviewer.data;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ImageEntry {
    private String filename;
    private String src;
    private Long galleryId;
    private String token;
    private Integer page;
    private ThumbEntry thumbEntry;
}
