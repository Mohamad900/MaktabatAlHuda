package com.maktabat.al.huda.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.parceler.ParcelConstructor;

/**
 * Created by User on 9/29/2019.
 */

public class QuestionCategory {

    @SerializedName("Id")
    @Expose
    public String id;
    @SerializedName("Name")
    @Expose
    public String name;
    @SerializedName("NumberOfBooks")
    @Expose
    public String numberOfBooks;
    @SerializedName("IsRecommended")
    @Expose
    public Boolean isRecommended;


    public QuestionCategory(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
