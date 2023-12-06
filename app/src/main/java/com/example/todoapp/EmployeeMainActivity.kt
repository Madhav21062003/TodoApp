package com.example.todoapp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todoapp.adapters.EmployeeWorkAdapter
import com.example.todoapp.api.ApiUtilities
import com.example.todoapp.auth.LoginActivity
import com.example.todoapp.databinding.ActivityEmployeeMainBinding
import com.example.todoapp.models.Notification
import com.example.todoapp.models.NotificationData
import com.example.todoapp.models.Users
import com.example.todoapp.models.Works
import com.example.todoapp.utils.Utils
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EmployeeMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmployeeMainBinding
    private lateinit var employeeWorkAdapter: EmployeeWorkAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmployeeMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        


        // Logout User
        binding.tbEmployee.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.logout -> {
                    showLogoutDialog()
                    true
                }

                else  -> false

            }
        }

        employeeWorkAdapter = EmployeeWorkAdapter(this@EmployeeMainActivity,::onProgressButtonClicked, ::onCompletedButtonClicked)

        prepareRvForEmployeeWorkAdapter()
        showEmployeeWorks()


    }

    private fun prepareRvForEmployeeWorkAdapter() {
         employeeWorkAdapter = EmployeeWorkAdapter(this@EmployeeMainActivity,::onProgressButtonClicked, ::onCompletedButtonClicked)
        binding.rvEmployeeWork.apply {
            layoutManager = LinearLayoutManager(this@EmployeeMainActivity, LinearLayoutManager.VERTICAL, false)
            adapter = employeeWorkAdapter
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun showEmployeeWorks() {

        val empId = FirebaseAuth.getInstance().currentUser?.uid
        var workRef = FirebaseDatabase.getInstance().getReference("Works")
             workRef.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (workRooms in snapshot.children){
                        if (workRooms.key?.contains(empId!!) == true){
                                val empWorRef = workRef.child(workRooms.key!!)
                            empWorRef.addValueEventListener(object : ValueEventListener{
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val workList = ArrayList<Works>()
                                    for (works in snapshot.children){
                                        val work = works.getValue(Works::class.java)
                                        workList.add(work!!)
                                    }

                                    if (workList.isEmpty()){
                                        binding.tvOne.text = View.VISIBLE.toString()
                                    }else{
                                        binding.tvOne.text = View.INVISIBLE.toString()
                                    }
                                    employeeWorkAdapter.differ.submitList(workList)
                                    Utils.hideDialog()
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    TODO("Not yet implemented")
                                }

                            })
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }


    private fun showLogoutDialog() {
        val  builder = AlertDialog.Builder(this@EmployeeMainActivity)
        val alertDialog = builder.create()
        alertDialog.setCancelable(false)
        builder.setTitle("Log Out")
            .setMessage("Are you sure you want to logout")
            .setPositiveButton("Yes"){_, _->
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this@EmployeeMainActivity, LoginActivity::class.java))
                this.finish()
            }
            .setNegativeButton("No"){_, _->
                alertDialog.dismiss()
            }
            .show()
    }

    private fun onProgressButtonClicked(works: Works, progressButton:MaterialButton){
        if (progressButton.text != "In Progress" ){

            val  builder = AlertDialog.Builder(this@EmployeeMainActivity)
            val alertDialog = builder.create()
            alertDialog.setCancelable(false)
            builder.setTitle("Starting Work")
                .setMessage("Are you starting this work ?")
                .setPositiveButton("Yes"){_, _->
                    progressButton.apply {
                        text = "In Progress"
                        setTextColor(ContextCompat.getColor(this@EmployeeMainActivity, R.color.Light5))
                    }
                    updateStatus(works, "2")
                }
                .setNegativeButton("No"){_, _->
                    alertDialog.dismiss()
                }
                .show()
        }
        else {

            Utils.showToast(this@EmployeeMainActivity, "Work in progress or completed")

        }

    }

    private fun updateStatus(works: Works, status:String) {

        Utils.showDialog(this)
        val empId = FirebaseAuth.getInstance().currentUser?.uid
        var workRef = FirebaseDatabase.getInstance().getReference("Works")
        workRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (workRooms in snapshot.children){
                    if (workRooms.key?.contains(empId!!) == true){
                        val empWorRef = workRef.child(workRooms.key!!)
                        empWorRef.addListenerForSingleValueEvent(object : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {

                                for (allWorks in snapshot.children){
                                    val work = allWorks.getValue(Works::class.java)
                                    if (works.workId == work?.workId){
                                        empWorRef.child(allWorks.key!!).child("workStatus").setValue(status)
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }

                        })
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun onCompletedButtonClicked(works: Works, completedButton:MaterialButton){

        if (completedButton.text !=  "Work Completed"){

            val  builder = AlertDialog.Builder(this@EmployeeMainActivity)
            val alertDialog = builder.create()
            alertDialog.setCancelable(false)
            builder.setTitle("Completed Work")
                .setMessage("Are you sure completed this work ?")
                .setPositiveButton("Yes"){_, _->
                    completedButton.apply {
                        text = "Work Completed"
                        setTextColor(ContextCompat.getColor(this@EmployeeMainActivity, R.color.Light5))
                    }
                    updateStatus(works, "3")
                    sendNotification(works.bossId, works.workTitle.toString())

                }
                .setNegativeButton("No"){_, _->
                    alertDialog.dismiss()
                }
                .show()
        }
        else {

            Utils.showToast(this@EmployeeMainActivity, "Work in progress or completed")
        }


    }

    private fun sendNotification(bossId: String?, workTitle: String) {
        val bossDataSnapshot = FirebaseDatabase.getInstance().getReference("Users").child(bossId!!).get()
        bossDataSnapshot.addOnSuccessListener {
            val empDetail = it.getValue(Users::class.java)
            val empToken = empDetail?.userToken
            val notification = Notification(empToken, NotificationData("Work Completed", workTitle))
            ApiUtilities.api.sendNotification(notification).enqueue(object :
                Callback<Notification> {
                override fun onResponse(
                    call: Call<Notification>,
                    response: Response<Notification>
                ) {
                    if (response.isSuccessful){
                        Log.d("notify", "Notification send")
                    }
                }

                override fun onFailure(call: Call<Notification>, t: Throwable) {
                    TODO("Not yet implemented")
                }

            })
        }

    }
}