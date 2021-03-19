package com.developer.databasewithroom.local;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.developer.databasewithroom.model.User;

import static com.developer.databasewithroom.local.UserDatabase.DATABASE_VERSION;

@Database(entities = {User.class}, version = DATABASE_VERSION)
public abstract class UserDatabase extends RoomDatabase {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "dataRoom";

    public abstract UserDAO userDAO();

    private static UserDatabase mInstance;

    public static UserDatabase getInstance(Context context){
        if (mInstance == null){
            mInstance = Room.databaseBuilder(context, UserDatabase.class, DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return mInstance;
    }






}
