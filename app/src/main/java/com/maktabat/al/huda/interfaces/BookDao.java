package com.maktabat.al.huda.interfaces;

/**
 * Created by User on 6/12/2019.
 */

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.maktabat.al.huda.model.Book;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;



@Dao
public interface BookDao {

    @Insert(onConflict = REPLACE)
    void AddBook(Book book);

    @Query("Select * from book_table where id =:id")
    Book GetBook(String id);

    @Delete
    int DeleteBook(Book book);
}
