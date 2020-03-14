package com.example.treasurehunt.util;

import android.app.Application;

import com.example.treasurehunt.Models.Users;

public class UserClient extends Application {
    private Users user;

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

}
