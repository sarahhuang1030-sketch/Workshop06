package com.example.workshop06.api;

import com.example.workshop06.model.AddressRequest;
import com.example.workshop06.model.AddressResponse;
import com.example.workshop06.model.CurrentPlanResponse;
import com.example.workshop06.model.LoginRequest;
import com.example.workshop06.model.LoginResponse;
import com.example.workshop06.model.MeResponse;
import com.example.workshop06.model.UpdateProfileRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface ApiService {
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("api/me")
    Call<MeResponse> getMe(@Header("Authorization") String token);

    @PUT("api/me/profile")
    Call<Void> updateProfile(
            @Header("Authorization") String token,
            @Body UpdateProfileRequest body
    );

    @GET("api/me/current-plan")
    Call<CurrentPlanResponse> getCurrentPlan(@Header("Authorization") String token);

    @GET("api/billing/address")
    Call<AddressResponse> getBillingAddress(@Header("Authorization") String token);

    @POST("api/billing/address")
    Call<Void> createBillingAddress(
            @Header("Authorization") String token,
            @Body AddressRequest body
    );

    @PUT("api/billing/address")
    Call<Void> updateBillingAddress(
            @Header("Authorization") String token,
            @Body AddressRequest body
    );


}