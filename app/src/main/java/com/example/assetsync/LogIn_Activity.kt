package com.example.assetsync

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.firestore.FirebaseFirestore

class LogInActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val PREF_NAME = "themePref"
    private val THEME_KEY = "isDarkMode"

    override fun onCreate(savedInstanceState: Bundle?) {
        // Load theme from preferences
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean(THEME_KEY, false)
        setAppTheme(isDarkMode)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Find views by ID
        val editTextEmail: EditText = findViewById(R.id.editTextEmail)
        val editTextPassword: EditText = findViewById(R.id.editTextPassword)
        val buttonLoginAssetHub: Button = findViewById(R.id.buttonLoginAssetHub)
        val buttonLoginStockHub: Button = findViewById(R.id.buttonLoginStockHub)
        val textViewForgotPassword: TextView = findViewById(R.id.textViewForgotPassword)
        val switchTheme: SwitchMaterial = findViewById(R.id.switchLightDarkMode)

        // Set the theme switch state
        switchTheme.isChecked = isDarkMode

        // Listen for theme switch changes
        switchTheme.setOnCheckedChangeListener { _, isChecked ->
            setAppTheme(isChecked)
            saveThemePreference(isChecked)
        }

        // AssetHub button click listener
        buttonLoginAssetHub.setOnClickListener {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()
            signInWithEmailPassword(email, password, "AssetHub")
        }

        // StockHub button click listener
        buttonLoginStockHub.setOnClickListener {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()
            signInWithEmailPassword(email, password, "StockHub")
        }

        // Hyperlink for forgot password
        textViewForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun setAppTheme(isDarkMode: Boolean) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun saveThemePreference(isDarkMode: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(THEME_KEY, isDarkMode)
        editor.apply()
    }

    private fun signInWithEmailPassword(email: String, password: String, loginType: String) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email or Password cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val db = FirebaseFirestore.getInstance()
        val usersRef = db.collection("users")

        // Query Firestore to find the user with matching email and password
        usersRef.whereEqualTo("Email", email)
            .whereEqualTo("Password", password)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d("LogInActivity", "No matching documents found")
                    Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
                } else {
                    for (document in documents) {
                        val role = document.getString("Role") ?: ""
                        Log.d("LogInActivity", "User role: $role")

                        when (loginType) {
                            "StockHub" -> {
                                // Check role for StockHub access
                                when (role) {
                                    "Driver" -> startActivity(
                                        Intent(
                                            this,
                                            StockHubDashboardActivity::class.java
                                        )
                                    )

                                    "Admin" -> startActivity(
                                        Intent(
                                            this,
                                            StockHubDashboardActivity::class.java
                                        )
                                    )

                                    else -> Toast.makeText(
                                        this,
                                        "Access restricted to Drivers and Admins only",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            "AssetHub" -> {
                                // Check role for AssetHub access using the same format as StockHub
                                when (role) {
                                    "Admin" -> startActivity(
                                        Intent(
                                            this,
                                            AssetHubActivity::class.java
                                        )
                                    )

                                    "Employee" -> startActivity(
                                        Intent(
                                            this,
                                            AssetHubActivity::class.java
                                        )
                                    )

                                    else -> Toast.makeText(
                                        this,
                                        "Access restricted to Admins and Employees only",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("LogInActivity", "Error checking user role: ${exception.message}")
                Toast.makeText(this, "Error logging in: ${exception.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }
}