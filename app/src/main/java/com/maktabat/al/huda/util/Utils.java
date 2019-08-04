package com.maktabat.al.huda.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.format.DateFormat;
import android.util.Base64;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maktabat.al.huda.model.Book;
import com.maktabat.al.huda.model.Category;
import com.maktabat.al.huda.model.Question;
import com.maktabat.al.huda.model.Slider;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Admin on 19-Sep-18.
 */

public class Utils {

    public static void setNewForBooksAndCategories(Context context){
        SharedPreferences prefs = context.getSharedPreferences("CacheData", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean("IsNewForBooks",true);
        edit.putBoolean("IsNewForCategories",true);
        edit.putBoolean("IsNewForQuestions",true);
        edit.apply();
    }
/*    public static void setNewForSliders(Context context){
        SharedPreferences prefs = context.getSharedPreferences("CacheData", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean("IsNewSliders",true);
        edit.apply();
    }*/
    public static void saveCacheDataForNewBooks(Context context, ArrayList<Book> newBooks){
        SharedPreferences prefs = context.getSharedPreferences("CacheData", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("NewBooks",new Gson().toJson(newBooks));
        edit.apply();
    }

    public static void saveCacheDataForSliders(Context context, List<Slider> sliders){
        SharedPreferences prefs = context.getSharedPreferences("CacheData", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("Sliders",new Gson().toJson(sliders));
        edit.apply();
    }

    public static void clearCacheDataForNewBooks(Context context ){
        SharedPreferences prefs = context.getSharedPreferences("CacheData", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("NewBooks",null);
        edit.apply();
    }

    public static void clearCacheDataForSliders(Context context ){
        SharedPreferences prefs = context.getSharedPreferences("CacheData", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("Sliders",null);
        edit.apply();
    }

    public static void clearCacheDataForRecommendedBooks(Context context ){
        SharedPreferences prefs = context.getSharedPreferences("CacheData", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("RecommendedBooks",null);
        edit.apply();
    }

    public static void clearCacheDataForPopularBooks(Context context ){
        SharedPreferences prefs = context.getSharedPreferences("CacheData", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("PopularBooks",null);
        edit.apply();
    }

    public static void clearCacheDataForCategories(Context context ){
        SharedPreferences prefs = context.getSharedPreferences("CacheData", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("Categories",null);
        edit.apply();
    }

    public static void clearCacheDataForQuestions(Context context){
        SharedPreferences prefs = context.getSharedPreferences("CacheData", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("Questions",null);
        edit.apply();
    }

    public static void saveCacheDataForCatgeories(Context context, ArrayList<Category> categories){
        SharedPreferences prefs = context.getSharedPreferences("CacheData", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("Categories",new Gson().toJson(categories));
        edit.apply();
    }

    public static void saveCacheDataForQuestions(Context context, List<Question> questions){
        SharedPreferences prefs = context.getSharedPreferences("CacheData", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("Questions",new Gson().toJson(questions));
        edit.apply();
    }

    public static void saveCacheDataForRecommendedBooks(Context context, ArrayList<Book> recommendedBooks){
        SharedPreferences prefs = context.getSharedPreferences("CacheData", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("RecommendedBooks",new Gson().toJson(recommendedBooks));
        edit.apply();
    }

    public static void saveCacheDataForPopularBooks(Context context, ArrayList<Book> popularBooks){
        SharedPreferences prefs = context.getSharedPreferences("CacheData", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("PopularBooks",new Gson().toJson(popularBooks));
        edit.apply();
    }

    public static Boolean hasCacheDataForNewBooks(Context context){
        try {
            SharedPreferences prefs = context.getSharedPreferences("CacheData", Context.MODE_PRIVATE);
            if(prefs.contains("NewBooks")){
                Type list = new TypeToken<ArrayList<Book>>() {}.getType();
                ArrayList<Book> newBooks = new Gson().fromJson(prefs.getString("NewBooks", null), list);
                if(newBooks!=null){
                    if(newBooks.size()>0){
                        return  true;
                    }
                }
            }
            return false;
        }catch (Exception e){
            return false;
        }
    }

    public static Boolean hasCacheDataForSliders(Context context){
        try {
            SharedPreferences prefs = context.getSharedPreferences("CacheData", Context.MODE_PRIVATE);
            if(prefs.contains("Sliders")){
                Type list = new TypeToken<ArrayList<Slider>>() {}.getType();
                ArrayList<Slider> sliders = new Gson().fromJson(prefs.getString("Sliders", null), list);
                if(sliders!=null){
                    if(sliders.size()>0){
                        return  true;
                    }
                }
            }
            return false;
        }catch (Exception e){
            return false;
        }
    }

    public static Boolean hasCacheDataForRecommendedBooks(Context context){
        try {
            SharedPreferences prefs = context.getSharedPreferences("CacheData", Context.MODE_PRIVATE);
            if(prefs.contains("RecommendedBooks")){
                Type list = new TypeToken<ArrayList<Book>>() {}.getType();
                ArrayList<Book> recommendedBooks = new Gson().fromJson(prefs.getString("RecommendedBooks", null), list);
                if(recommendedBooks!=null){
                    if(recommendedBooks.size()>0){
                        return  true;
                    }
                }
            }
            return false;
        }catch (Exception e){
            return false;
        }
    }

    public static Boolean hasCacheDataForPopularBooks(Context context){
        try {
            SharedPreferences prefs = context.getSharedPreferences("CacheData", Context.MODE_PRIVATE);
            if(prefs.contains("PopularBooks")){
                Type list = new TypeToken<ArrayList<Book>>() {}.getType();
                ArrayList<Book> popularBooks = new Gson().fromJson(prefs.getString("PopularBooks", null), list);
                if(popularBooks!=null){
                    if(popularBooks.size()>0){
                        return  true;
                    }
                }
            }
            return false;
        }catch (Exception e){
            return false;
        }
    }

    public static Boolean hasCacheDataForCategories(Context context){
        try {
            SharedPreferences prefs = context.getSharedPreferences("CacheData", Context.MODE_PRIVATE);
            if(prefs.contains("Categories")){
                Type list = new TypeToken<ArrayList<Category>>() {}.getType();
                ArrayList<Category> categories = new Gson().fromJson(prefs.getString("Categories", null), list);
                if(categories!=null){
                    if(categories.size()>0){
                        return  true;
                    }
                }
            }
            return false;
        }catch (Exception e){
            return false;
        }
    }

    public static Boolean hasCacheDataForQuestions(Context context){
        try {
            SharedPreferences prefs = context.getSharedPreferences("CacheData", Context.MODE_PRIVATE);
            if(prefs.contains("Questions")){
                Type list = new TypeToken<ArrayList<Question>>() {}.getType();
                ArrayList<Question> questions = new Gson().fromJson(prefs.getString("Questions", null), list);
                if(questions!=null){
                    if(questions.size()>0){
                        return  true;
                    }
                }
            }
            return false;
        }catch (Exception e){
            return false;
        }
    }

    public static ArrayList<Category> getCacheDataForCategories(Context context){
        try {
            SharedPreferences prefs = context.getSharedPreferences("CacheData", Context.MODE_PRIVATE);
            Type list = new TypeToken<ArrayList<Category>>() {
            }.getType();
            return new Gson().fromJson(prefs.getString("Categories", null), list);
        }catch (Exception e){
            return new ArrayList<Category>();
        }
    }
    public static ArrayList<Question> getCacheDataForQuestions(Context context){
        try {
            SharedPreferences prefs = context.getSharedPreferences("CacheData", Context.MODE_PRIVATE);
            Type list = new TypeToken<ArrayList<Question>>() {
            }.getType();
            return new Gson().fromJson(prefs.getString("Questions", null), list);
        }catch (Exception e){
            return new ArrayList<Question>();
        }
    }
    public static ArrayList<Slider> getCacheDataForSliders(Context context){
        try {
            SharedPreferences prefs = context.getSharedPreferences("CacheData", Context.MODE_PRIVATE);
            Type list = new TypeToken<ArrayList<Slider>>() {
            }.getType();
            return new Gson().fromJson(prefs.getString("Sliders", null), list);
        }catch (Exception e){
            return new ArrayList<Slider>();
        }
    }

    public static ArrayList<Book> getCacheDataForNewBooks(Context context){
        try {
            SharedPreferences prefs = context.getSharedPreferences("CacheData", Context.MODE_PRIVATE);
            Type list = new TypeToken<ArrayList<Book>>() {
            }.getType();
            return new Gson().fromJson(prefs.getString("NewBooks", null), list);
        }catch (Exception e){
            return new ArrayList<Book>();
        }
    }

    public static ArrayList<Book> getCacheDataForRecommendedBooks(Context context){
        try {
            SharedPreferences prefs = context.getSharedPreferences("CacheData", Context.MODE_PRIVATE);
            Type list = new TypeToken<ArrayList<Book>>() {
            }.getType();
            return new Gson().fromJson(prefs.getString("RecommendedBooks", null), list);
        }catch (Exception e){
            return new ArrayList<Book>();
        }
    }

    public static ArrayList<Book> getCacheDataForPopularBooks(Context context){
        try {
            SharedPreferences prefs = context.getSharedPreferences("CacheData", Context.MODE_PRIVATE);
            Type list = new TypeToken<ArrayList<Book>>() {
            }.getType();
            return new Gson().fromJson(prefs.getString("PopularBooks", null), list);
        }catch (Exception e){
            return new ArrayList<Book>();
        }
    }

    public static Boolean checkIfNewForBooks(Context context){
        SharedPreferences prefs = context.getSharedPreferences("CacheData", Context.MODE_PRIVATE);
        return prefs.getBoolean("IsNewForBooks",false);
    }
/*    public static Boolean checkIfNewForSliders(Context context){
        SharedPreferences prefs = context.getSharedPreferences("CacheData", Context.MODE_PRIVATE);
        return prefs.getBoolean("IsNewForSliders",false);
    }*/
    public static Boolean checkIfNewForCategories(Context context){
        SharedPreferences prefs = context.getSharedPreferences("CacheData", Context.MODE_PRIVATE);
        return prefs.getBoolean("IsNewForCategories",false);
    }

    public static Boolean checkIfNewForQuestions(Context context){
        SharedPreferences prefs = context.getSharedPreferences("CacheData", Context.MODE_PRIVATE);
        return prefs.getBoolean("IsNewForQuestions",false);
    }

    public static void changeNewForBooks(Context context){
        SharedPreferences prefs = context.getSharedPreferences("CacheData", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean("IsNewForBooks",false);
        edit.apply();
    }
    public static void changeNewForSliders(Context context){
        SharedPreferences prefs = context.getSharedPreferences("CacheData", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean("IsNewForSliders",false);
        edit.apply();
    }

    public static void changeNewForCategories(Context context){
        SharedPreferences prefs = context.getSharedPreferences("CacheData", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean("IsNewForCategories",false);
        edit.apply();
    }

    public static void changeNewForQuestions(Context context){
        SharedPreferences prefs = context.getSharedPreferences("CacheData", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean("IsNewForQuestions",false);
        edit.apply();
    }

    public static String getNameFromPath(String path) {
        int index = path.lastIndexOf("/");
        return path.substring(index + 1);
    }

    public static String longToDateString(long millis) {
        return new SimpleDateFormat("MM/dd/yyyy HH:mm").format(millis);
    }

    public static String buildTextFileSize(long size) {
        double doubleSize;
        NumberFormat format;
        if (size > 1073741824) {
            doubleSize = ((double) size) / 1.073741824E9d;
            format = NumberFormat.getInstance();
            format.setMaximumFractionDigits(1);
            return format.format(doubleSize) + " " + "gigabyteShort";
        } else if (size > 1048576) {
            doubleSize = ((double) size) / 1048576.0d;
            format = NumberFormat.getInstance();
            format.setMaximumFractionDigits(1);
            return format.format(doubleSize) + " " + "MB";
        } else if (size > 1024) {
            return String.valueOf(String.valueOf(size / 1024)) + " " + "KB";
        } else {
            return String.valueOf(size) + " " + "B";
        }
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String BitmapToString(Bitmap bitmap){
        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] b=baos.toByteArray();
        String temp=Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    public static Bitmap StringToBitmap(String encodedString){
        try {
            byte [] encodeByte= Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap=BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }

    public static byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        return stream.toByteArray();
    }


    public static Bitmap convertByteArrayToBitmap(byte[] bytes) {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public static byte[] imageURLToBytesArray(String src) throws MalformedURLException {
        URL url = new URL(src);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try (InputStream inputStream = url.openStream()) {
            int n = 0;
            byte [] buffer = new byte[ 1024 ];
            while (-1 != (n = inputStream.read(buffer))) {
                output.write(buffer, 0, n);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return output.toByteArray();
    }
}
