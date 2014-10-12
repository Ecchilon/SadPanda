package com.ecchilon.sadpanda.overview;

import com.ecchilon.sadpanda.R;

import lombok.Getter;

/**
 * Created by Alex on 28-9-2014.
 */
public enum Category {
    Misc("Misc", R.color.misc, R.string.misc),
    NonH("Non-H", R.color.non_h, R.string.nonh),
    ArtistCG("Artist CG Sets", R.color.artist_cg, R.string.artist_cg),
    ImageSet("Image Sets", R.color.image_set, R.string.imageset),
    Manga("Manga", R.color.manga, R.string.manga),
    GameCG("Game CG Sets", R.color.game_cg, R.string.game_cg),
    Doujinshi("Doujinshi", R.color.doujinshi, R.string.doujinshi),
    Western("Western", R.color.doujinshi, R.string.western),
    Cosplay("Cosplay", R.color.cosplay, R.string.cosplay),
    AsianPorn("Asian Porn", R.color.asian_porn, R.string.asianporn);

    private final String name;
    @Getter
    private final int color;
    @Getter
    private final int resName;

    Category(String name, int color, int resName) {
        this.name = name;
        this.color = color;
        this.resName = resName;
    }

    public static Category fromName(String name) {
        for(Category type : Category.values()) {
            if(type.name.equals(name)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Name " + name + " is not a valid content type!");
    }
}
