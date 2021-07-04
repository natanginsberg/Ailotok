package com.speech.ailotok.model;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

public class Topic implements Serializable {

    private final String url;
    private final String name;
    private final Drawable picture;

    public Topic(String name, String url) {
        this.name = name;
        this.url = url;
        picture = null;
    }

    public Topic(String name, Drawable picture) {
        this.name = name;
        this.picture = picture;
        url = null;
    }

    public Drawable getPicture() {
        return picture;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

}
