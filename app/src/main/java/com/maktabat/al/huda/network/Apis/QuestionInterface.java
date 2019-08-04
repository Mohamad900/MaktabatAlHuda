package com.maktabat.al.huda.network.Apis;

import com.maktabat.al.huda.model.Category;
import com.maktabat.al.huda.model.Question;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by User on 7/27/2019.
 */

public interface QuestionInterface {
    @POST("Question/GetAllFilteredQuestions")
    Call<List<Question>> getAllFilteredQuestions();
    @POST("Question/SendQuestion")
    Call<Boolean> sendQuestion(@Body Question questionObj);
}
