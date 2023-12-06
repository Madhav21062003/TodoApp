package com.example.todoapp.models

import java.util.UUID

data class Works(

    // generates the unique id for particular user
    val id: String = UUID.randomUUID().toString(),
    val workId:String?= null,
    val workTitle : String? = null,
    val workDesc : String? = null,
    val bossId : String? = null,
    val workPriority: String? = null,
    val workLastDate: String? = null,
    val workStatus: String? = null,

    var expanded:Boolean = false
)
