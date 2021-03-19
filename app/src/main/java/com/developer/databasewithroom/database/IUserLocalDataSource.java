package com.developer.databasewithroom.database;

import com.developer.databasewithroom.model.User;

import java.util.List;

import io.reactivex.Flowable;

public interface IUserLocalDataSource  {

    Flowable<User> getUserById(int userId);
    Flowable<List<User>> getAllUsers();
    void insertUser(User... users);
    void updateUser(User... users);
    void deleteUser(User... users);
    void deleteAllUsers();

}
