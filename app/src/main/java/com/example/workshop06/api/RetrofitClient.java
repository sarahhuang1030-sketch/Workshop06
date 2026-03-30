package com.example.workshop06.api;

import android.content.Context;
import android.content.SharedPreferences;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Interceptor;
import okhttp3.logging.HttpLoggingInterceptor;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class RetrofitClient {




        // ✅ PUT YOUR BASE URL HERE
        private static final String BASE_URL = "http://10.0.2.2:8080/";

        private static Retrofit retrofit;

    public static Retrofit getRetrofitInstance(Context context) {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        Interceptor authInterceptor = chain -> {
            SharedPreferences prefs = context.getSharedPreferences("teleconnect_prefs", Context.MODE_PRIVATE);
            String token = prefs.getString("jwt_token", null);

            Request request = chain.request();

            if (token != null && !token.isEmpty()) {
                request = request.newBuilder()
                        .addHeader("Authorization", "Bearer " + token)
                        .build();
            }

            return chain.proceed(request);
        };

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(logging)
                .build();

        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static ApiService getApiService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }

}
