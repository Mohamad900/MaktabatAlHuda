package com.maktabat.al.huda.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by User on 6/20/2019.
 */

public class Slider {
    @SerializedName("Id")
    @Expose
    public String id;
    @SerializedName("Image")
    @Expose
    public String image;
    @SerializedName("Title")
    @Expose
    public String title;

    public Slider(String id, String image, String title) {
        this.id = id;
        this.image = image;
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
