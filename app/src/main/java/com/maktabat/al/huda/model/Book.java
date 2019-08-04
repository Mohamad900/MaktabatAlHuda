package com.maktabat.al.huda.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

@Parcel
@Entity(tableName = "book_table")
public class Book {

    @SerializedName("Id")
    @Expose
    @PrimaryKey
    @NotNull
    public String id;
    @SerializedName("Title")
    @Expose
    @ColumnInfo(name = "title")
    public String title;
    @SerializedName("Author")
    @Expose
    public String author;
    @SerializedName("DocumentName")
    @Expose
    public String documentName;
    @SerializedName("Image")
    @Expose
    @ColumnInfo(name = "image")
    public String image;
    @SerializedName("Description")
    @Expose
    public String description;
    @SerializedName("Publisher")
    @Expose
    public String publisher;
    @SerializedName("IsRecommended")
    @Expose
    public Boolean isRecommended;
    @SerializedName("IsPopular")
    @Expose
    public Boolean isPopular;
    @SerializedName("Category")
    @Expose
    @Ignore
    public Category category;


    public Book(){}

    public Book(@NonNull String id, String title) {
        this.id = id;
        this.title=title;
    }

    @ParcelConstructor
    public Book(@NonNull String id, String title, String author, String documentName, String image, String description, String publisher, Boolean isRecommended, Boolean isPopular, Category category) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.documentName = documentName;
        this.image = image;
        this.description = description;
        this.publisher = publisher;
        this.isRecommended = isRecommended;
        this.isPopular = isPopular;
        this.category = category;
    }


    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Boolean getRecommended() {
        return isRecommended;
    }

    public void setRecommended(Boolean recommended) {
        isRecommended = recommended;
    }

    public Boolean getPopular() {
        return isPopular;
    }

    public void setPopular(Boolean popular) {
        isPopular = popular;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

}
