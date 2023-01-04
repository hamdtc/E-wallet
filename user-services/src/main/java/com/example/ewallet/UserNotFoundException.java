package com.example.ewallet;


public class UserNotFoundException extends Exception{

    public UserNotFoundException() {
        super("User not found");
    }
}

