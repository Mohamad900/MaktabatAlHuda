package com.maktabat.al.huda.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

/**
 * Created by User on 7/27/2019.
 */

@Parcel
public class Question {
    @SerializedName("Id")
    @Expose
    public String id;
    @SerializedName("UserQuestion")
    @Expose
    public String userQuestion;
    @SerializedName("Response")
    @Expose
    public String response;

    @ParcelConstructor
    public Question(String userQuestion, String response) {
        this.userQuestion = userQuestion;
        this.response = response;
    }

    public Question(String userQuestion) {
        this.userQuestion = userQuestion;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        id = id;
    }

    public String getUserQuestion() {
        return userQuestion;
    }

    public void setUserQuestion(String userQuestion) {
        userQuestion = userQuestion;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        response = response;
    }
}
