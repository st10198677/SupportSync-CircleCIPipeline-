package com.example.assetsync

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class StockHubDriverActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var storageReference: StorageReference
    private lateinit var imageView: ImageView
    private var imageUri: Uri? = null

    companion object {
        private const val GALLERY_REQUEST_CODE = 1000
        private const val CAMERA_PERMISSION_CODE = 1001
        private const val TAG = "StockHubDriverActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stockhub_driver)

        // Initialize Firestore and Storage
        db = Firebase.firestore
        storageReference = FirebaseStorage.getInstance().reference

        // Find views by their ID
        val editTextDriverId: EditText = findViewById(R.id.editTextDriverId)
        val editTextDeliveryLocation: EditText = findViewById(R.id.editTextDeliveryLocation)
        val editTextQuantity: EditText = findViewById(R.id.editTextQuantity)
        val buttonStartDelivery: Button = findViewById(R.id.buttonStartDelivery)
        val buttonSelectImage: ImageButton = findViewById(R.id.buttonSelectImage)  // Corrected here
        imageView = findViewById(R.id.imageViewCaptured)

        // Request permission to access external storage
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), CAMERA_PERMISSION_CODE)
        }

        // Set a click listener on the Start Delivery button
        buttonStartDelivery.setOnClickListener {
            val driverId = editTextDriverId.text.toString().trim()
            val deliveryLocation = editTextDeliveryLocation.text.toString().trim()
            val quantityString = editTextQuantity.text.toString().trim()

            // Validate that all fields are filled
            if (driverId.isNotEmpty() && deliveryLocation.isNotEmpty() && quantityString.isNotEmpty() && imageUri != null) {
                // Convert quantity to a Long
                val quantity = quantityString.toLongOrNull()
                if (quantity != null) {
                    checkDriverIdExists(driverId, deliveryLocation, quantity)
                } else {
                    Toast.makeText(this, "Quantity must be a valid number", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill all fields and select an image", Toast.LENGTH_SHORT).show()
            }
        }

        // Set up image selection when the button is clicked
        buttonSelectImage.setOnClickListener {
            openGallery()
        }

        // Set up Bottom Navigation
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        setupBottomNavigation(bottomNavigationView)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            imageView.setImageURI(imageUri)
        }
    }

    private fun checkDriverIdExists(driverId: String, deliveryLocation: String, quantity: Long) {
        Log.d(TAG, "Checking if Driver ID exists: $driverId")
        db.collection("users")
            .whereEqualTo("EmployeeId", driverId)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    documents.forEach { document ->
                        Log.d(TAG, "Found user: ${document.id} with EmployeeId: ${document.getString("EmployeeId")}")
                    }
                    uploadImageAndStoreData(driverId, deliveryLocation, quantity)
                } else {
                    Log.d(TAG, "Driver ID does not exist: $driverId")
                    Toast.makeText(this@StockHubDriverActivity, "Driver ID does not exist.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting documents: ", exception)
                Toast.makeText(this@StockHubDriverActivity, "Database error. Please try again.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadImageAndStoreData(driverId: String, deliveryLocation: String, quantity: Long) {
        val deliveryId = db.collection("stock").document().id // Create a unique delivery ID

        val imageRef = storageReference.child("stock/image/$deliveryId.jpg")
        Log.d(TAG, "Uploading image to: stock/image/$deliveryId.jpg")

        imageRef.putFile(imageUri!!).addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                Log.d(TAG, "Image upload successful. URL: $uri")

                val deliveryData = hashMapOf(
                    "driverId" to driverId,
                    "deliveryId" to deliveryId,
                    "location" to deliveryLocation,
                    "quantity" to quantity, // Store quantity as Long
                    "image" to uri.toString(),
                    "status" to "pending" // Set the status to "Pending"
                )

                db.collection("stock").document(deliveryId).set(deliveryData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Delivery started successfully!", Toast.LENGTH_SHORT).show()
                        // No navigation, just saving the data
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Failed to store delivery data in Firestore: ${e.message}")
                        Toast.makeText(this, "Failed to store delivery data", Toast.LENGTH_SHORT).show()
                    }
            }.addOnFailureListener {
                Log.e(TAG, "Failed to retrieve image download URL: ${it.message}")
                Toast.makeText(this, "Failed to get image URL", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Log.e(TAG, "Failed to upload image to Firebase Storage: ${it.message}")
            Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBottomNavigation(bottomNavigationView: BottomNavigationView) {
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    // Navigate to StockHubDashboardActivity if not already on that screen
                    startActivity(Intent(this, StockHubDashboardActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                    finish()
                    true
                }
                R.id.nav_start_delivery -> {
                    // Stay on StockHubDriverActivity (no action needed)
                    true
                }
                R.id.nav_logout -> {
                    // Navigate to LogInActivity and clear the back stack
                    startActivity(Intent(this, LogInActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}
