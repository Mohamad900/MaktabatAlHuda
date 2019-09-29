package com.maktabat.al.huda.network.Apis;

import com.maktabat.al.huda.model.Category;
import com.maktabat.al.huda.model.Question;
import com.maktabat.al.huda.model.QuestionCategory;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by User on 7/27/2019.
 */

public interface QuestionInterface {

    @POST("Question/GetAllFilteredQuestionsByCategory")
    Call<List<Question>> GetAllFilteredQuestionsByCategory(@Body QuestionCategory category);

    @POST("Question/SendQuestion")
    Call<Boolean> sendQuestion(@Body Question questionObj);

    @POST("Question/GetQuestionsCategoriesDetails")
    Call<List<Category>> getQuestionsCategories();

}
