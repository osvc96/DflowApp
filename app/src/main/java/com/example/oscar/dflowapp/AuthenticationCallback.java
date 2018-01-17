package com.example.oscar.dflowapp;

/**
 * Created by juanjose on 1/17/18.
 */

interface AuthenticationCallback<T> {
    void onSuccess(T data);
    void onError(Exception e);
}
