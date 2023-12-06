package com.example.todoapp.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.example.todoapp.BossMainActivity
import com.example.todoapp.EmployeeMainActivity
import com.example.todoapp.models.Users
import com.example.todoapp.databinding.ActivityLoginBinding
import com.example.todoapp.databinding.ForgotPasswordDialogBinding
import com.example.todoapp.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// This class handles the logic for the login functionality in the app.

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding // Binding for the Login Activity

    // Function that runs when the activity is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflating the layout using the binding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setting up the login button to handle user authentication
        binding.btnLogin.setOnClickListener {
            // Retrieving email and password from the input fields
            val email = binding.etLoginEmail.text.toString()
            val password = binding.etLoginPassword.text.toString()

            // Checking if the email and password fields are not empty
            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Initiating the login process with the provided email and password
                loginUser(email, password)
            } else {
                // Showing a toast message if any field is empty
                Utils.showToast(this@LoginActivity, "Please enter all the Details")
            }
        }

        // Setting up the text view to handle navigation to the registration page
        binding.tvSignUp.setOnClickListener {
            startActivity(
                Intent(
                    this@LoginActivity,
                    RegisterActivity::class.java
                )
            ) // Start the RegisterActivity
            finish() // Finish the current LoginActivity
        }

        // Setting up the text view to handle the "Forgot Password" functionality
        binding.tvForgotPassword.setOnClickListener {
            showForgotPasswordDialog() // Show the dialog for resetting the password
        }
    }

    // Function to display the forgot password dialog
    private fun showForgotPasswordDialog() {
        // Inflating the dialog layout using the binding
        val dialog = ForgotPasswordDialogBinding.inflate(LayoutInflater.from(this@LoginActivity))
        val alertDialog = AlertDialog.Builder(this@LoginActivity)
            .setView(dialog.root)
            .create()
        alertDialog.setCancelable(false)
        alertDialog.show()

        // Setting up the functionality for the "Back to Login" button in the dialog
        dialog.tvBackToLogin.setOnClickListener {
            startActivity(
                Intent(
                    this@LoginActivity,
                    LoginActivity::class.java
                )
            ) // Navigate back to LoginActivity
            finish() // Finish the current LoginActivity
        }

        // Setting up the functionality for the "Forgot Password" button in the dialog
        dialog.btnForgotPassword.setOnClickListener {
            val email =
                dialog.etEmail.text.toString() // Retrieving the email from the dialog input field
            alertDialog.dismiss() // Dismiss the dialog
            resetPassword(email) // Initiating the password reset process
        }
    }

    // Here i explain explain why i use the lifecycle.launch  (it is a coroutine)
//        In the provided code, lifecycleScope.launch is used to send a password reset email using Firebase Authentication.
//        By using this, the app can continue functioning smoothly even during the process of sending the email,
//        without blocking the user interface. Additionally, it ensures that the task is canceled if the activity
//        is closed or destroyed, preventing any potential memory leaks or unnecessary resource usage.
//        Any exceptions that occur during the process are caught and displayed to the user as a message using
//        the Utils.showToast method.

    // Function to reset the user's password
    private fun resetPassword(email: String) {
        // Using lifecycleScope to handle background operations efficiently


        lifecycleScope.launch {
            try {
                // Sending a password reset email using Firebase Authentication
                FirebaseAuth.getInstance().sendPasswordResetEmail(email).await()
                Utils.showToast(
                    this@LoginActivity,
                    "Please Check email and reset your password"
                ) // Displaying a toast message
            } catch (e: Exception) {
                Utils.showToast(
                    this@LoginActivity,
                    e.message.toString()
                ) // Displaying an error message if any exception occurs
            }
        }
    }

    // Function to handle the login process
    private fun loginUser(email: String, password: String) {
        Utils.showDialog(this@LoginActivity) // Showing a dialog to indicate the login process

        // Getting the Firebase authentication instance
        val firebaseAuth = FirebaseAuth.getInstance()

        // Using lifecycleScope to handle background operations efficiently
        lifecycleScope.launch {
            try {
                // Signing in with the provided email and password
                val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()

                // Retrieving the current user's ID and email verification status
                val currentUser = authResult.user?.uid
                val verifyEmail = firebaseAuth.currentUser?.isEmailVerified

                // Checking if the email is verified
                if (verifyEmail == true) {
                    Utils.hideDialog()
                    // If the user is verified, retrieve user data and navigate to the appropriate activity
                    if (currentUser != null) {
                        Utils.hideDialog()
                        FirebaseDatabase.getInstance().getReference("Users").child(currentUser)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    // Extracting user data from the database
                                    val currentUserData = snapshot.getValue(Users::class.java)

                                    // Navigating to the appropriate activity based on the user type
                                    when (currentUserData?.userType) {
                                        "Boss" -> {
                                            startActivity(
                                                Intent(
                                                    this@LoginActivity,
                                                    BossMainActivity::class.java
                                                )
                                            )

                                            finish()
                                            Log.d(
                                                "Boss",
                                                "Boss Layout Opened"
                                            ) // Logging for debugging purposes
                                        }

                                        "Employee" -> {
                                            Utils.hideDialog()
                                            startActivity(
                                                Intent(
                                                    this@LoginActivity,
                                                    EmployeeMainActivity::class.java
                                                )
                                            )
//                                            Utils.hideDialog()
                                            finish()
                                            Log.d(
                                                "Emp",
                                                "Employee Layout Opened"
                                            ) // Logging for debugging purposes
                                        }

                                        else -> {
                                            Utils.hideDialog()
                                            startActivity(
                                                Intent(
                                                    this@LoginActivity,
                                                    EmployeeMainActivity::class.java
                                                )
                                            )
//                                            Utils.hideDialog()
                                            finish()
                                            Log.d(
                                                "Emp",
                                                "Default Layout Opened"
                                            ) // Logging for debugging purposes
                                        }
                                    }
                                }

                                // Handling any error that occurs during data retrieval
                                override fun onCancelled(error: DatabaseError) {
                                    Utils.hideDialog()
                                    Utils.showToast(
                                        this@LoginActivity,
                                        error.message
                                    ) // Displaying an error message
                                }

                            })
                    } else {
                        Utils.hideDialog()
                        Utils.showToast(
                            this@LoginActivity,
                            "User Not found Sign Up First"
                        ) // Displaying an error message
                    }
                } else {
                    Utils.hideDialog()
                    Utils.showToast(
                        this@LoginActivity,
                        "Email is not verified"
                    ) // Displaying an error message
                }

            } catch (e: Exception) {
                Utils.hideDialog()
                Utils.showToast(
                    this@LoginActivity,
                    e.message!!
                ) // Displaying an error message if an exception occurs
            }
        }
    }
}

