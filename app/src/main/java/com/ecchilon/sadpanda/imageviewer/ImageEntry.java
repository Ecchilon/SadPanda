package com.ecchilon.sadpanda.imageviewer;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Created by Alex on 21-9-2014.
 */
@Data
@Accessors(chain = true)
public class ImageEntry {
    private String filename;
    private String src;
    private Long galleryId;
    private String token;
    private Integer page;
}
