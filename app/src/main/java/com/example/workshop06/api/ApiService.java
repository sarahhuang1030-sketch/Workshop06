package com.example.workshop06.api;

import com.example.workshop06.model.LoginRequest;
import com.example.workshop06.model.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);
}