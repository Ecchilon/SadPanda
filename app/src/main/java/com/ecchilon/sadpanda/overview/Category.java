package com.ecchilon.sadpanda.overview;

import com.ecchilon.sadpanda.R;

import lombok.Getter;

/**
 * Created by Alex on 28-9-2014.
 */
public enum Category {
    Misc("Misc", R.color.misc),
    NonH("Non-H", R.color.non_h),
    ArtistCG("Artist CG Sets", R.color.artist_cg),
    ImageSet("Image Sets", R.color.image_set),
    Manga("Manga", R.color.manga),
    GameCG("Game CG Sets", R.color.game_cg),
    Doujinshi("Doujinshi", R.color.doujinshi),
    Western("Western", R.color.doujinshi),
    Cosplay("Cosplay", R.color.cosplay),
    AsianPorn("Asian Porn", R.color.asian_porn);

    private final String name;
    @Getter
    private final int color;

    Category(String name, int color) {
        this.name = name;
        this.color = color;
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
