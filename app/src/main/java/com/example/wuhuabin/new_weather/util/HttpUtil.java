package com.example.wuhuabin.new_weather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

/**
 * Created by wuhuabin on 2017/3/22.
 */

public class HttpUtil {

    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
        OkHttpClient okHttpClient=new OkHttpClient();
        Request request=new Request.Builder().url(address).build();
        okHttpClient.newCall(request).enqueue(callback);
    }

    public static void sendRetrofit(String address, final Callback<String> callback){
        Retrofit retrofit=new Retrofit.Builder().baseUrl(address).addConverterFactory(GsonConverterFactory.create()).build();
        RequestService requestService = retrofit.create(RequestService.class);
        final Call<String> call = requestService.sendRequset();
        call.enqueue(callback);
    }

    public interface RequestService {
        @GET("/")
        Call<String> sendRequset();
    }

}
