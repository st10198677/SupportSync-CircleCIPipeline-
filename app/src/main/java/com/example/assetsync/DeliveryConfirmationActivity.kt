package com.example.assetsync

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class DeliveryConfirmationActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private var driverId: String = ""
    private var deliveryId: String = ""
    private var location: String = ""
    private var quantity: Long = 0L
    private var imageUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivery_confirmation)

        db = FirebaseFirestore.getInstance()

        // Retrieve data from the intent
        deliveryId = intent.getStringExtra("deliveryId") ?: ""
        driverId = intent.getStringExtra("driverId") ?: ""
        location = intent.getStringExtra("location") ?: ""
        quantity = intent.getLongExtra("quantity", 0L)

        // Find views by ID
        val textViewDriverId: TextView = findViewById(R.id.textViewDriverId)
        val textViewQuantity: TextView = findViewById(R.id.textViewQuantity)
        val editTextEmail: EditText = findViewById(R.id.editTextEmail)
        val editTextPasscode: EditText = findViewById(R.id.editTextPasscode)
        val buttonCompleteDelivery: Button = findViewById(R.id.buttonConfirmDelivery)

        textViewDriverId.text = driverId
        textViewQuantity.text = quantity.toString()

        buttonCompleteDelivery.setOnClickListener {
            val email = editTextEmail.text.toString().trim()
            val passcode = editTextPasscode.text.toString().trim()

            if (email.isNotEmpty()) {
                verifyEmailAndCompleteDelivery(email)
            } else if (passcode.length == 4) {
                verifyPasscodeAndCompleteDelivery(passcode)
            } else {
                Toast.makeText(this, "Enter either email or 4-digit passcode", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun verifyEmailAndCompleteDelivery(email: String) {
        db.collection("users").whereEqualTo("email", email).get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(this, "Email not found", Toast.LENGTH_SHORT).show()
                } else {
                    completeDelivery("User with email $email")
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error verifying email", Toast.LENGTH_SHORT).show()
            }
    }

    private fun verifyPasscodeAndCompleteDelivery(passcode: String) {
        db.collection("users").whereEqualTo("passcode", passcode).get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(this, "Invalid passcode", Toast.LENGTH_SHORT).show()
                } else {
                    completeDelivery("User with passcode $passcode")
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error verifying passcode", Toast.LENGTH_SHORT).show()
            }
    }

    private fun completeDelivery(completedBy: String) {
        val deliveryUpdate = hashMapOf(
            "status" to "completed",
            "completedBy" to completedBy
        )

        db.collection("stock").document(deliveryId)
            .set(deliveryUpdate, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Delivery marked as completed", Toast.LENGTH_SHORT).show()
                navigateToStockHubDeliveryCompletionActivity()
            }
            .addOnFailureListener { e ->
                Log.e("DeliveryConfirmation", "Error updating delivery status: ${e.message}")
                Toast.makeText(this, "Error marking delivery as completed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToStockHubDeliveryCompletionActivity() {
        val intent = Intent(this, StockHubDeliveryCompletionActivity::class.java).apply {
            putExtra("driverId", driverId)
            putExtra("deliveryId", deliveryId)
            putExtra("location", location)
            putExtra("quantity", quantity)
            putExtra("imageUrl", imageUrl)
        }
        startActivity(intent)
        finish()  // This will end the current activity after starting StockHubDeliveryCompletionActivity
    }
}


