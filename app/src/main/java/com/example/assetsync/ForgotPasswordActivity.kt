package com.example.assetsync

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var editTextName: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextEmployeeNumber: EditText
    private lateinit var buttonResetPassword: Button
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        // Initialize views
        editTextName = findViewById(R.id.editTextName)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextEmployeeNumber = findViewById(R.id.editTextEmployeeNumber)
        buttonResetPassword = findViewById(R.id.buttonResetPassword)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Set up back arrow functionality
        val backArrow: ImageView = findViewById(R.id.backArrow)
        backArrow.setOnClickListener { finish() }

        // Handle password reset button click
        buttonResetPassword.setOnClickListener { verifyUserAndProvideInstructions() }
    }

    private fun verifyUserAndProvideInstructions() {
        // Get input values
        val email = editTextEmail.text.toString().trim()
        val name = editTextName.text.toString().trim()
        val employeeNumber = editTextEmployeeNumber.text.toString().trim()

        // Check for empty fields
        if (email.isEmpty() || name.isEmpty() || employeeNumber.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Firestore query to verify user identity based on email and employee number
        val query: Query = db.collection("users")
            .whereEqualTo("Email", email)
            .whereEqualTo("EmployeeId", employeeNumber)

        query.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (!task.result.isEmpty) {
                    // User is found; provide instructions
                    Toast.makeText(this, "Verification successful. Please contact admin to reset your password.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "User not found. Please check your details.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
