package com.example.todoapp.auth

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.todoapp.BossMainActivity
import com.example.todoapp.EmployeeMainActivity
import com.example.todoapp.R
import com.example.todoapp.models.Users
import com.example.todoapp.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@SuppressLint("CustomSplashScreen")
// The SplashActivity is displayed when the app is launched, providing a brief introduction or logo.

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth // Firebase authentication instance
    private var isAppFirstLaunch = true // Variable to track the first app launch
    // Function that runs when the activity is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setting the content view to the layout defined in 'activity_splash.xml'
        setContentView(R.layout.activity_splash)

        // Initializing Firebase authentication
        auth = FirebaseAuth.getInstance()

        // Retrieving the current user's ID from Firebase
        val currentUser = auth.currentUser?.uid

        // Checking if a user is already logged in
        if (currentUser != null){

            // Using a delayed handler to perform tasks after a specific time (3 seconds)
            if (isAppFirstLaunch){
                Handler(Looper.getMainLooper()).postDelayed({

                    // Using lifecycleScope to handle background operations efficiently

                    // Trying to access the user's data from Firebase Realtime Database
                    FirebaseDatabase.getInstance().getReference("Users").child(currentUser).addListenerForSingleValueEvent(object :
                        ValueEventListener {
                        // Executed when data is successfully read from the database
                        override fun onDataChange(snapshot: DataSnapshot) {

                            // Extracting the current user's data and mapping it to the Users class
                            val currentUserData = snapshot.getValue(Users::class.java)

                            // Checking the type of user and opening corresponding layouts
                            when(currentUserData?.userType){

                                // If the user is of type "Boss", open the BossMainActivity
                                "Boss" -> {
                                    startActivity(Intent(this@SplashActivity, BossMainActivity::class.java))
                                    finish() // Finish the current SplashActivity
                                    Log.d("Boss", "Boss Layout Opened") // Log message for debugging
                                }

                                // If the user is of type "Employee", open the EmployeeMainActivity
                                "Employee" -> {
                                    startActivity(Intent(this@SplashActivity, EmployeeMainActivity::class.java))
                                    finish() // Finish the current SplashActivity
                                    Log.d("Emp", "Employee Layout Opened") // Log message for debugging
                                }

                                // If the user type is not specified or unknown, open the EmployeeMainActivity by default
                                else -> {
                                    startActivity(Intent(this@SplashActivity, EmployeeMainActivity::class.java))
                                    finish() // Finish the current SplashActivity
                                    Log.d("Emp", "Default Layout Opened") // Log message for debugging
                                }
                            }
                        }

                        // Executed if there is an error accessing the data from the database
                        override fun onCancelled(error: DatabaseError) {
                            Utils.hideDialog() // Hide any open dialog boxes
                            Utils.showToast(this@SplashActivity, error.message) // Show a toast message with the error information
                        }

                    })

                }, 1500) // Delay of 3 seconds before the code inside the handler is executed
                isAppFirstLaunch = false
            }


        } else {
            // If there is no logged-in user, open the LoginActivity

            // Using a delayed handler to open the Login Activity after a delay of 3 seconds
            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(this, LoginActivity::class.java) // Creating an Intent to open the LoginActivity
                startActivity(intent) // Starting the LoginActivity
                finish() // Finish the current SplashActivity
            }, 1500) // Delay of 3 seconds before the code inside the handler is executed
        }
    }
}

