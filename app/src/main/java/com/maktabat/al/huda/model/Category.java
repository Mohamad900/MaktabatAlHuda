package com.maktabat.al.huda.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

/**
 * Created by User on 5/31/2019.
 */

@Parcel
public class Category {

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

    public Category(String name) {
        this.name = name;
    }

    public Category(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @ParcelConstructor
    public Category(String id, String name, String numberOfBooks) {
        this.id = id;
        this.name = name;
        this.numberOfBooks = numberOfBooks;
    }

    public Boolean getRecommended() {
        return isRecommended;
    }

    public void setRecommended(Boolean recommended) {
        isRecommended = recommended;
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

    public String getNumberOfBooks() {
        return numberOfBooks;
    }

    public void setNumberOfBooks(String numberOfBooks) {
        this.numberOfBooks = numberOfBooks;
    }
}
