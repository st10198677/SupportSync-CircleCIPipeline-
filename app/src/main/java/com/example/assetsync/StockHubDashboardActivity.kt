package com.example.assetsync

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

class StockHubDashboardActivity : AppCompatActivity() {

    // Firebase Firestore instance
    private lateinit var db: FirebaseFirestore

    // RecyclerView and its adapter for displaying deliveries
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DeliveryAdapter

    // List to store pending deliveries
    private val pendingDeliveries = mutableListOf<Delivery>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stockhub_dashboard)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Initialize RecyclerView and BottomNavigationView
        recyclerView = findViewById(R.id.recyclerViewDeliveries)
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)

        // Setup RecyclerView and load data
        setupRecyclerView()
        loadDeliveries()

        // Setup bottom navigation functionality
        setupBottomNavigation(bottomNavigationView)
    }

    /**
     * Sets up the RecyclerView with a LinearLayoutManager and adapter.
     */
    private fun setupRecyclerView() {
        // Set up the DeliveryAdapter with the lambda for handling button click
        adapter = DeliveryAdapter(pendingDeliveries) { delivery ->
            continueWithDelivery(delivery)  // Handle the continue button click
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    /**
     * Loads pending deliveries from Firestore and updates the RecyclerView.
     */
    private fun loadDeliveries() {
        db.collection("stock")
            .whereEqualTo("status", "pending") // Query only pending deliveries
            .get()
            .addOnSuccessListener { documents ->
                // Clear the existing list to avoid duplication
                pendingDeliveries.clear()

                // Populate the list with data from Firestore
                for (document in documents) {
                    val delivery = document.toObject(Delivery::class.java).apply {
                        deliveryId = document.id
                    }
                    pendingDeliveries.add(delivery)
                }

                // Notify adapter of data changes
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                // Show error message and log the error
                Toast.makeText(this, "Error loading deliveries: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("StockHubDashboard", "Error loading deliveries", e)
            }
    }

    /**
     * This method is called when the user clicks to continue with the delivery.
     * It opens the DeliveryConfirmationActivity and passes the relevant data.
     */
    private fun continueWithDelivery(delivery: Delivery) {
        val intent = Intent(this, DeliveryConfirmationActivity::class.java).apply {
            putExtra("deliveryId", delivery.deliveryId)
            putExtra("location", delivery.location)
            putExtra("quantity", delivery.quantity)
            putExtra("driverId", delivery.driverId)
            putExtra("image", delivery.image)
        }
        startActivity(intent)
    }

    /**
     * Sets up the BottomNavigationView with navigation actions for each menu item.
     */
    private fun setupBottomNavigation(bottomNavigationView: BottomNavigationView) {
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    // Current screen; no action needed
                    true
                }
                R.id.nav_start_delivery -> {
                    // Navigate to StockHubDriverActivity
                    startActivity(Intent(this, StockHubDriverActivity::class.java))
                    true
                }
                R.id.nav_logout -> {
                    // Navigate to LogInActivity and clear back stack
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
