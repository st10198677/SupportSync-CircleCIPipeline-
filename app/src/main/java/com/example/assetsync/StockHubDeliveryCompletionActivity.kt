package com.example.assetsync

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class StockHubDeliveryCompletionActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var imageView: ImageView
    private lateinit var progressDialog: ProgressDialog
    private var capturedImageUri: Uri? = null
    private var deliveryId: String = ""

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_PERMISSION_CAMERA = 2
        private const val REQUEST_PERMISSION_STORAGE = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stockhub_delivery_completed)

        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        progressDialog = ProgressDialog(this).apply {
            setMessage("Uploading image...")
            setCancelable(false)
        }

        // Retrieve data from the intent
        deliveryId = intent.getStringExtra("deliveryId") ?: ""
        val driverId = intent.getStringExtra("driverId") ?: "N/A"
        val location = intent.getStringExtra("location") ?: "N/A"
        val quantity = intent.getLongExtra("quantity", 0L).toInt()
        val imageUrl = intent.getStringExtra("imageUrl") ?: ""

        // Find views by ID
        val textViewLocation: TextView = findViewById(R.id.textViewLocation)
        val textViewDriverId: TextView = findViewById(R.id.textViewDriverId)
        val textViewQuantity: TextView = findViewById(R.id.textViewQuantity)
        imageView = findViewById(R.id.imageViewDelivery)

        // Display retrieved data in the UI
        textViewDriverId.text = "Driver ID: $driverId"
        textViewQuantity.text = "Quantity of Boxes: $quantity"
        textViewLocation.text = "Location: $location"

        val buttonCaptureImage: ImageButton = findViewById(R.id.buttonSelectImage)
        val buttonConfirmDelivery: ImageButton = findViewById(R.id.buttonConfirmDelivery)
        val buttonLogout: ImageButton = findViewById(R.id.buttonLogout)

        buttonCaptureImage.setOnClickListener { captureImage() }

        buttonConfirmDelivery.setOnClickListener {
            if (capturedImageUri != null) {
                uploadImageAndStoreData() // Use the updated method
            } else {
                showToast("Please capture an image before confirming delivery.")
            }
        }

        buttonLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun requestPermissions() {
        val cameraPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
        val storagePermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (cameraPermission != PackageManager.PERMISSION_GRANTED || storagePermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_PERMISSION_CAMERA)
        } else {
            captureImage()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CAMERA) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                captureImage()
            } else {
                Toast.makeText(this, "Permission denied. Cannot capture image.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun captureImage() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } else {
            Toast.makeText(this, "Camera not available.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imageView.setImageBitmap(imageBitmap)  // Display captured image

            // Convert the Bitmap to Uri and set it to capturedImageUri
            capturedImageUri = getImageUriFromBitmap(imageBitmap)
            Log.d("StockHubDelivery", "Image captured and URI set successfully.")
        }
    }

    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri? {
        val path = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "CapturedImage", null)
        return if (path != null) {
            Uri.parse(path)
        } else {
            Log.e("StockHubDelivery", "Failed to insert image into MediaStore.")
            null
        }
    }

    private fun uploadImageAndStoreData() {
        progressDialog.show()
        capturedImageUri?.let { uri ->
            // Create a unique storage path using the delivery ID
            val imageRef = storage.reference.child("stock/image/$deliveryId.jpg")

            imageRef.putFile(uri).addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    // Update Firestore with the image URL
                    val documentRef = db.collection("stock").document(deliveryId)
                    documentRef.update("deliveryImages", FieldValue.arrayUnion(downloadUrl.toString()))
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            showToast("Delivery confirmation complete.")
                        }
                        .addOnFailureListener {
                            progressDialog.dismiss()
                            showToast("Failed to update delivery with image URL.")
                        }
                }.addOnFailureListener {
                    progressDialog.dismiss()
                    showToast("Failed to retrieve image URL.")
                }
            }.addOnFailureListener {
                progressDialog.dismiss()
                showToast("Failed to upload image.")
            }
        } ?: run {
            progressDialog.dismiss()
            showToast("No image to upload.")
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Confirm Logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { _, _ ->
                val intent = Intent(this, LogInActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

