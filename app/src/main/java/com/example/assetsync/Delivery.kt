package com.example.assetsync

// Data class to represent a delivery
data class Delivery(
    var deliveryId: String="",
    val driverId: String="",
    val location: String="",
    val quantity: Long=0,
    val image: String="",
    val status: String ="pending",
    val timestamp: Long = System.currentTimeMillis(), // Example additional field

) {
    // Required empty constructor for Firestore deserialization
    constructor() : this("", "", "", 0, "", "pending ")
}

