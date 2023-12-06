package com.example.todoapp.auth


import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.RadioButton
import android.widget.Toast

import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog

import androidx.lifecycle.lifecycleScope
import com.example.todoapp.R
import com.example.todoapp.models.Users

import com.example.todoapp.databinding.ActivityRegisterBinding
import com.example.todoapp.utils.Utils
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


// This class manages the user registration process.

class RegisterActivity : AppCompatActivity() {

    // Declaring necessary variables
    private lateinit var binding: ActivityRegisterBinding // View binding for the activity
    private lateinit var mAuth: FirebaseAuth // Firebase authentication instance

    // Task 1: Upload Image
    private var userImageUrl: Uri? = null // Variable to store the selected user image URI

    // Registering activity result launcher to get image from device storage
    private val selectImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
        userImageUrl = it // Set the selected image URI to the userImageUrl variable
        binding.ivUserImage.setImageURI(userImageUrl) // Display the selected image in the ImageView
    }

    // Here we make an empty string and store the value of the radio group value that will be selected, either "boss" or "employee"
    private var userType: String = ""

    // onCreate method called when the activity is starting
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflating the layout using View Binding
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setting onClickListener for selecting user image from the device gallery
        binding.apply {
            ivUserImage.setOnClickListener {
                // Open the device's local gallery to select an image
                selectImage.launch("image/*")
            }
        }

        // Setting the radio group to handle user type selection
        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            userType = findViewById<RadioButton>(checkedId).text.toString()
            Log.d("user Type", userType) // Logging the selected user type
        }

        // Initialize Firebase authentication instance
        mAuth = FirebaseAuth.getInstance()

        // If the user already has an account, this click listener takes them to the Login screen
        binding.tvSignIn.setOnClickListener {
            startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
            finish()
        }

        // Register button click listener to initiate the user registration process
        binding.btnregister.setOnClickListener {
            createUser() // Call the method to register the user
        }
    }

    // Function to create a new user account
    private fun createUser() {
        Utils.showDialog(this@RegisterActivity) // Display a dialog indicating the registration process

        val name = binding.etName.text.toString()
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
            if (userImageUrl == null)
                Utils.showToast(this@RegisterActivity, "Please Select One Image")
            else if (password == confirmPassword) {
                if (userType != "") {
                    uploadImageUri(name, email, password)
                } else {
                    Utils.hideDialog()
                    Utils.showToast(this, "Select User Type")
                }
            } else
                Utils.showToast(this@RegisterActivity, "Password are Not Matching.....")
        } else {
            Utils.hideDialog()
            Utils.showToast(this@RegisterActivity, "Empty Fields are Not Allowed")
        }
    }

    // Function to upload the user's image to Firebase Storage
    private fun uploadImageUri(name: String, email: String, password: String) {
        // Getting the current user ID
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val storageReference =
            FirebaseStorage.getInstance().getReference("Profile").child(currentUserId)
                .child("Profile.jpg")

        lifecycleScope.launch {
            try {
                val uploadTask = storageReference.putFile(userImageUrl!!).await()

                if (uploadTask.task.isSuccessful) {
                    val downloadURL = storageReference.downloadUrl.await()
                    saveUserData(name, email, password, downloadURL)
                } else {
                    Utils.hideDialog()
                    Toast.makeText(
                        this@RegisterActivity,
                        "Upload Failed: ${uploadTask.task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Utils.hideDialog()
                Toast.makeText(
                    this@RegisterActivity,
                    "Upload Failed: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()

            }
        }
    }

    // Function to save user data to the Firebase Realtime Database
    @SuppressLint("SuspiciousIndentation")
    private fun saveUserData(name: String, email: String, password: String, downloadURL: Uri?) {

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful)
                return@OnCompleteListener
            val token = task.result
            lifecycleScope.launch {
                val db = FirebaseDatabase.getInstance().getReference("Users")
                try {
                    val firebaseAuth =
                        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                            .await()

                    if (firebaseAuth.user != null) {

                        // Email Verification
                        FirebaseAuth.getInstance().currentUser?.sendEmailVerification()
                            ?.addOnSuccessListener {
                                // Inflate the custom layout for the dialog
                                val dialog = layoutInflater.inflate(R.layout.accout_dialog, null)
                                val alertDialog = AlertDialog.Builder(this@RegisterActivity)
                                    .setView(dialog)
                                    .setCancelable(false)
                                    .create()
                                alertDialog.show()
                                Utils.hideDialog()
                                // Handling the button Click
                                dialog.findViewById<Button>(R.id.btnOk).setOnClickListener {
                                    alertDialog.dismiss()
                                    startActivity(
                                        Intent(
                                            this@RegisterActivity,
                                            LoginActivity::class.java
                                        )
                                    )
                                    finish()
                                }

                            }
                        val uID = firebaseAuth.user?.uid.toString()

                        val saveUserType = if (userType == "Boss") "Boss" else "Employee"
                        val user =
                            Users(
                                userType = saveUserType,
                                userId = uID,
                                userName = name,
                                userEmail = email,
                                userImage = downloadURL.toString(),
                                userPassword = password,
                                userToken = token
                            )
                        db.child(uID).setValue(user).await()

                    } else {
                        Utils.hideDialog()
                        Utils.showToast(this@RegisterActivity, "Signed Up Failed ")
                    }
                } catch (e: Exception) {
                    Utils.hideDialog()
                    Utils.showToast(this@RegisterActivity, e.message.toString())
                }
            }
        })


    }
}
