package com.rakib.soberpoint.Notification;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APISERVICESHIT {


    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAaEpjQH0:APA91bG3yAyDhfZzcIj912y-ZdovzxnlWeuUvHzY8Ba7Pa5pJ7wu8XtYm7ip_4s_ylWGmgZ-RfiCVsxswHititc6mxKrU94AOE7h4MS10uf0a9ADAaQ-gAX8AD7NMGog6FCNG2HOVtSU"
    })

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);


}
