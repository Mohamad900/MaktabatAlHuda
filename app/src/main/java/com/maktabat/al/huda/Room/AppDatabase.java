package com.maktabat.al.huda.Room;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.maktabat.al.huda.interfaces.BookDao;
import com.maktabat.al.huda.model.Book;

/**
 * Created by User on 3/29/2018.
 */

@Database(entities = {Book.class},version = 1 , exportSchema = false)
public abstract  class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public static AppDatabase getAppDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "AlHudaLibrary")
                            // allow queries on the main thread.
                            // Don't do this on a real app! See PersistenceBasicSample for an example.
                            .allowMainThreadQueries()
                            .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    //define tables
    public abstract BookDao bookDao();
}
