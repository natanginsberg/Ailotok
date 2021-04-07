package com.speech.ailotok.model;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

public class Topic implements Serializable {

    private final String url;
    private final String name;
    private final Drawable picture;
    private final String flowString;

    public Topic(String name, String url, String flowString) {
        this.name = name;
        this.url = url;
        this.flowString = flowString;
        picture = null;
    }

    public Topic(String name, Drawable picture, String flowString) {
        this.name = name;
        this.picture = picture;
        this.flowString = flowString;
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

    public String getFlowString() {
        return flowString;
    }
}
