package com.example.todoapp.api


import com.example.todoapp.models.Notification
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiInterface {


    @Headers(
        "Content-Type: application/json",
        "Authorization: key=AAAAz4bahPM:APA91bErp8yKTmNTxC_zTxEHZfFZE0Jhx6gpBw1odpPgUiDitrG1XUAY4m6s4GsOcAqRc9O0EFcK5niC_07VNM5p4CytC1Iipw_zrfxEsCDLU9vZQXjcWGDNNV8l2iEblqbGUdCtSaBq"
    )

    @POST("fcm/send")
    fun sendNotification(@Body notification: Notification) : Call<Notification>
}