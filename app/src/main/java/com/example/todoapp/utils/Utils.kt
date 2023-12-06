package com.example.todoapp.utils

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.todoapp.R

object Utils {

    // Here we made a utils class for our Progress Dialog when we want to use it we directly access it from this class
    private var dialog: AlertDialog? = null

    fun showDialog(context: Context){
        dialog = AlertDialog.Builder(context).setView(R.layout.progress_diaog).setCancelable(false).create()
        dialog!!.show()
    }

    fun  hideDialog(){
        dialog?.dismiss()
    }

    fun showToast(context: Context, message:String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

}