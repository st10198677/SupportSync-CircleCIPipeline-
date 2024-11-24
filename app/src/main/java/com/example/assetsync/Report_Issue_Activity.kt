package com.example.assetsync

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class ReportIssueActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var edtIssueDescription: EditText
    private lateinit var txtAssetCode: TextView
    private lateinit var btnSubmitIssue: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_issue)

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Get Views
        edtIssueDescription = findViewById(R.id.edtIssueDescription)
        txtAssetCode = findViewById(R.id.txtAssetCode)
        btnSubmitIssue = findViewById(R.id.btnSubmitIssue)

        // Retrieve asset code passed via intent
        val assetCode = intent.getStringExtra("assetCode") ?: ""
        txtAssetCode.text = assetCode // Display the asset code (if needed for debugging)

        // Submit issue to Firestore
        btnSubmitIssue.setOnClickListener {
            val issueDescription = edtIssueDescription.text.toString().trim()
            if (issueDescription.isEmpty()) {
                Toast.makeText(this, "Please describe the issue.", Toast.LENGTH_SHORT).show()
            } else {
                submitIssue(assetCode, issueDescription)
            }
        }
    }

    private fun submitIssue(assetCode: String, issueDescription: String) {
        // Prepare data to store in the "report" collection
        val reportData = hashMapOf(
            "assetCode" to assetCode,
            "issueDescription" to issueDescription,
            "timestamp" to System.currentTimeMillis() // Add a timestamp for reference
        )

        // Store data in Firestore
        firestore.collection("report")
            .add(reportData)
            .addOnSuccessListener {
                Toast.makeText(this, "Issue reported successfully!", Toast.LENGTH_SHORT).show()
                finish() // Close the activity after successful submission
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to report issue: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
