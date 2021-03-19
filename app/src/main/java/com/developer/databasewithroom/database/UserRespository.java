package com.developer.databasewithroom.database;

import com.developer.databasewithroom.model.User;

import java.util.List;

import io.reactivex.Flowable;

public class UserRespository implements IUserLocalDataSource{

    private static IUserLocalDataSource mLocalDataSource;
    private static UserRespository mInstance;

    public UserRespository(IUserLocalDataSource mLocalDataSource) {
        this.mLocalDataSource = mLocalDataSource;
    }

    public static UserRespository getInstance(IUserLocalDataSource mLocalDataSourc){
        if (mInstance == null){
            mInstance = new UserRespository(mLocalDataSourc);
        }
        return mInstance;
    }

    @Override
    public Flowable<User> getUserById(int userId) {
        return mLocalDataSource.getUserById(userId);
    }

    @Override
    public Flowable<List<User>> getAllUsers() {
        return mLocalDataSource.getAllUsers();
    }

    @Override
    public void insertUser(User... users) {
        mLocalDataSource.insertUser(users);
    }

    @Override
    public void updateUser(User... users) {
        mLocalDataSource.updateUser(users);
    }

    @Override
    public void deleteUser(User... users) {
        mLocalDataSource.deleteUser(users);
    }

    @Override
    public void deleteAllUsers() {
        mLocalDataSource.deleteAllUsers();

    }
}
