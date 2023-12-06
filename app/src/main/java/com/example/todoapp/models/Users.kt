package com.example.todoapp.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.UUID

@Parcelize
data class Users(

    // generates the unique id for particular user
    val id: String = UUID.randomUUID().toString(),


    val userType: String?= null,
    val userId: String? = null,
    val userName: String? = null,
    val userEmail: String? = null,
    val userImage: String? = null,
    val userPassword: String? = null,
    val userToken : String? = null
) : Parcelable
