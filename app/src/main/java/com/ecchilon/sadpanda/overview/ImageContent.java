package com.ecchilon.sadpanda.overview;

import com.ecchilon.sadpanda.R;

/**
 * Created by Alex on 21-9-2014.
 */
public enum ImageContent {
    ImageSet("imageset", R.color.image_set),
    Doujinshi("doujinshi", R.color.doujinshi),
    Western("western", R.color.western),
    Manga("manga", R.color.manga),
    NonH("non-h", R.color.non_h),
    Cosplay("cosplay", R.color.cosplay),
    Misc("misc", R.color.misc),
    AsianPorn("asianporn", R.color.asian_porn),
    GameCG("gamecg", R.color.game_cg),
    ArtistCG("artistcg", R.color.artist_cg);

    private final String name;
    private final int color;

    private ImageContent(String name, int color) {
        this.name = name;
        this.color = color;
    }

    public static ImageContent parseContent(String content) {
        for(ImageContent entry : ImageContent.values()) {
            if(entry.name.equals(content)) {
                return entry;
            }
        }

        throw new IllegalArgumentException("ImageContent not known!");
    }

    public int getColor() {
        return color;
    }
}
