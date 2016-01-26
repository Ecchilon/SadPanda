package com.ecchilon.sadpanda.api;

import lombok.Getter;

/**
 * Created by SkyArrow on 2014/2/19.
 * @author alex on 2014/9/23.
 */
public enum ApiErrorCode {
    GALLERY_NOT_EXIST(1),
    PHOTO_NOT_EXIST(2),
    PHOTO_DATA_REQUIRED(3),
    SHOWKEY_INVALID(4),
    SHOWKEY_NOT_FOUND(5),
    API_ERROR(6),
    PHOTO_NOT_FOUND(9),
    TOKEN_NOT_FOUND(10),
    TOKEN_OR_PAGE_INVALID(11),
    TOKEN_INVALID(12),
    SHOWKEY_EXPIRED(13),
    GALLERY_PINNED(14);

    @Getter
    private final int errorCode;

    private ApiErrorCode(int code) {
        this.errorCode = code;
    }
}
