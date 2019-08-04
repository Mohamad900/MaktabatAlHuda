package com.maktabat.al.huda.network.Apis;

import com.maktabat.al.huda.model.Book;
import com.maktabat.al.huda.model.Category;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by User on 5/31/2019.
 */

public interface BookInterface {

    @POST("Book/GetBooksDetails")
    Call<List<Book>> getAllBooks();

    @POST("Book/GetRecommendedAndPopularBooks")
    Call<List<Book>> getRecommendedAndPopularBooks();

    @POST("Book/GetNewBooks")
    Call<List<Book>> getNewBooks();

    @POST("Book/GetBooksByCategory")
    Call<List<Book>> getBooksByCategory(@Body Category category);

    @GET("Book/SearchBooks")
    Call<List<Book>> searchBooks(@Query("searchWord") String searchWord);
}
