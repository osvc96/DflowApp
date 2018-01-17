package com.example.oscar.dflowapp;

/**
 * Created by juanjose on 1/17/18.
 */

import com.example.oscar.dflowapp.vo.MessageWrapper;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;


public interface MSGraphAPIService {
    @POST("/v1.0/me/microsoft.graph.sendmail")
    Call<Void> sendMail(
            @Header("Content-type") String contentTypeHeader,
            @Body MessageWrapper mail);
}