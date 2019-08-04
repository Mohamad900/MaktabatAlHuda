package com.maktabat.al.huda.network.Apis;

import com.maktabat.al.huda.model.Book;
import com.maktabat.al.huda.model.Settings;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.POST;

/**
 * Created by User on 7/14/2019.
 */

public interface SettingsInterface {
    @POST("Settings/GetSettings")
    Call<Settings> getSettings();
}
