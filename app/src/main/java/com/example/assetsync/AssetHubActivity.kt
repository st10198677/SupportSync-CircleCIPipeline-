package com.example.assetsync

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class AssetHubActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var assetsRecyclerView: RecyclerView
    private lateinit var welcomeMessage: TextView
    private lateinit var progressBar: ProgressBar
    private var employeeId: String = "EmployeeId"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assethub)

        // Initialize views
        progressBar = findViewById(R.id.progressBar)
        welcomeMessage = findViewById(R.id.txtWelcomeMessage)
        assetsRecyclerView = findViewById(R.id.recyclerViewAssets)

        // Set up RecyclerView with a LinearLayoutManager
        assetsRecyclerView.layoutManager = LinearLayoutManager(this)

        // Retrieve employee ID from the intent, default to "users" if not found
        employeeId = intent.getStringExtra("EmployeeId") ?: "users"

        // Check if employee ID is provided and proceed to fetch user details and assets
        if (employeeId.isNotEmpty()) {
            fetchUserDetails()
            fetchAssets()
        } else {
            Toast.makeText(this, "Employee ID not provided", Toast.LENGTH_SHORT).show()
        }
    }

    // Fetch user details using the employee ID
    private fun fetchUserDetails() {
        progressBar.visibility = View.VISIBLE // Show progress bar while fetching

        firestore = FirebaseFirestore.getInstance()
        firestore.collection("users")
            .whereEqualTo("EmployeeId", employeeId)
            .get()
            .addOnSuccessListener { documents ->
                progressBar.visibility = View.GONE // Hide the progress bar once data is fetched
                if (documents.size() > 0) {  // Check if the collection contains any documents
                    val document = documents.documents.first()  // Access the first document
                    val firstName = document.getString("FirstName") ?: "N/A"
                    val lastName = document.getString("LastName") ?: "N/A"
                    welcomeMessage.text = "Welcome, $firstName $lastName"
                    Log.d("AssetHubActivity", "User found: $firstName $lastName")
                } else {
                    Toast.makeText(this, "User details not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                progressBar.visibility = View.GONE // Hide progress bar on failure
                Log.e("AssetHubActivity", "Error fetching user details: ${exception.message}")
            }
    }


    // Fetch assets associated with the employee ID
    private fun fetchAssets() {
        progressBar.visibility = View.VISIBLE // Show progress bar while fetching

        firestore.collection("assets")
            .whereEqualTo("EmployeeId", employeeId)
            .get()
            .addOnSuccessListener { documents ->
                progressBar.visibility = View.GONE // Hide the progress bar once data is fetched
                if (documents.isEmpty) {
                    Toast.makeText(this, "No assets found for this user.", Toast.LENGTH_SHORT).show()
                } else {
                    // Create a list to store assets
                    val assets = mutableListOf<Asset>()

                    for (document in documents) {
                        val assetCategory = document.getString("assetCategory") ?: "N/A"
                        val assetMake = document.getString("assetMake") ?: "N/A"
                        val assetModel = document.getString("assetModel") ?: "N/A"
                        val assetCode = document.getString("assetCode") ?: "N/A"

                        Log.d("AssetHubActivity", "Asset found: Category=$assetCategory, Make=$assetMake, Model=$assetModel, Code=$assetCode")

                        // Add the asset to the list
                        assets.add(Asset(assetCategory, assetMake, assetModel, assetCode))
                    }

                    // Set up the RecyclerView with the assets
                    assetsRecyclerView.adapter = AssetAdapter(assets)
                }
            }
            .addOnFailureListener { exception ->
                progressBar.visibility = View.GONE // Hide progress bar on failure
                Log.e("AssetHubActivity", "Error fetching assets: ${exception.message}")
            }
    }

    // Asset data class to represent each asset
    data class Asset(val category: String, val make: String, val model: String, val code: String)

    // Adapter class for the RecyclerView
    class AssetAdapter(private val assets: List<Asset>) : RecyclerView.Adapter<AssetAdapter.AssetViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssetViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.asset_item, parent, false)
            return AssetViewHolder(view)
        }

        override fun onBindViewHolder(holder: AssetViewHolder, position: Int) {
            val asset = assets[position]
            holder.bind(asset)
        }

        override fun getItemCount(): Int = assets.size

        // ViewHolder class for each asset item
        class AssetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val txtAssetCategory: TextView = itemView.findViewById(R.id.txtAssetCategory)
            private val txtAssetMake: TextView = itemView.findViewById(R.id.txtAssetMake)
            private val txtAssetModel: TextView = itemView.findViewById(R.id.txtAssetModel)
            private val btnReportIssue: Button = itemView.findViewById(R.id.btnReportIssue)

            fun bind(asset: Asset) {
                txtAssetCategory.text = asset.category
                txtAssetMake.text = asset.make
                txtAssetModel.text = asset.model

                // Set the "Report Issue" button functionality
                btnReportIssue.setOnClickListener {
                    if (asset.code.isNotEmpty()) {
                        val context = itemView.context
                        val intent = Intent(context, ReportIssueActivity::class.java)
                        intent.putExtra("assetCode", asset.code)
                        context.startActivity(intent)
                    } else {
                        Toast.makeText(itemView.context, "Asset code is missing", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
