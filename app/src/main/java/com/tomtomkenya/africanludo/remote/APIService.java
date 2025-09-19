package com.tomtomkenya.africanludo.remote;

import com.tomtomkenya.africanludo.helper.AppConstant;
import com.tomtomkenya.africanludo.model.MyResponse;
import com.tomtomkenya.africanludo.model.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key="+ AppConstant.SERVER_KEY
    })
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}

