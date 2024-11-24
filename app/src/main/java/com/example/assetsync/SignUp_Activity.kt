package com.example.assetsync

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    // Declare views
    private lateinit var editTextFirstName: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextConfirmPassword: EditText
    private lateinit var spinnerRole: Spinner
    private lateinit var buttonSignUp: Button
    private lateinit var textViewLogin: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Initialize views
        editTextFirstName = findViewById(R.id.editTextFirstName)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword)
        spinnerRole = findViewById(R.id.spinnerRole)
        buttonSignUp = findViewById(R.id.buttonSignUp)
        textViewLogin = findViewById(R.id.textViewLogin)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Set click listener for the Sign Up button
        buttonSignUp.setOnClickListener {
            handleSignUp()
        }

        // Set click listener for the Login TextView
        textViewLogin.setOnClickListener {
            navigateToLogin()
        }
    }

    // Handle the signup process
    private fun handleSignUp() {
        val firstName = editTextFirstName.text.toString().trim()
        val email = editTextEmail.text.toString().trim()
        val password = editTextPassword.text.toString().trim()
        val confirmPassword = editTextConfirmPassword.text.toString().trim()
        val selectedRole = spinnerRole.selectedItem.toString()

        // Validate that all fields are filled
        if (firstName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if passwords match
        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show()
            return
        }

        // Proceed with Firebase signup
        createFirebaseUser(firstName, email, password, selectedRole)
    }

    // Create a new user with Firebase Authentication and save the user data to Firestore
    private fun createFirebaseUser(firstName: String, email: String, password: String, role: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Get the current user ID
                    val userId = auth.currentUser?.uid

                    if (userId != null) {
                        // Save the user data to Firestore
                        val userMap = hashMapOf(
                            "first_name" to firstName,
                            "email" to email,
                            "role" to role // Save selected role
                        )

                        firestore.collection("users").document(userId).set(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(this, "User registered successfully", Toast.LENGTH_SHORT).show()
                                navigateToLogin() // Redirect to login after successful registration
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    // If sign-up fails, show an error message
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Navigate to the login page
    private fun navigateToLogin() {
        val intent = Intent(this, LogInActivity::class.java)
        startActivity(intent)
        finish() // Optional: finish current activity to prevent going back to the sign-up page
    }
}

