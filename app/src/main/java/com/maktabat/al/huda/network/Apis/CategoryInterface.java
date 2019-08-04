package com.maktabat.al.huda.network.Apis;

import com.maktabat.al.huda.model.Book;
import com.maktabat.al.huda.model.Category;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.POST;

/**
 * Created by User on 5/31/2019.
 */

public interface CategoryInterface {

    @POST("Category/GetAllCategories")
    Call<List<Category>> getAllCategories();

}
