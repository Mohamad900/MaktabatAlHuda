package com.maktabat.al.huda.network.Apis;

import com.maktabat.al.huda.model.Book;
import com.maktabat.al.huda.model.Slider;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.POST;

/**
 * Created by User on 6/20/2019.
 */

public interface SliderInterface {

    @POST("Slider/GetSliders")
    Call<List<Slider>> getSliders();
}
