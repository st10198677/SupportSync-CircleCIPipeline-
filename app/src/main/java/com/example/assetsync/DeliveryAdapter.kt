package com.example.assetsync

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.assetsync.databinding.ItemDeliveryBinding  // Import the generated binding class for the layout

class DeliveryAdapter(
    private val deliveries: List<Delivery>,
    private val onContinueClick: (Delivery) -> Unit  // Passing a lambda function for the click handler
) : RecyclerView.Adapter<DeliveryAdapter.DeliveryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeliveryViewHolder {
        val binding = ItemDeliveryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeliveryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeliveryViewHolder, position: Int) {
        val delivery = deliveries[position]
        holder.bind(delivery)
    }

    override fun getItemCount(): Int = deliveries.size

    inner class DeliveryViewHolder(private val binding: ItemDeliveryBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(delivery: Delivery) {
            // Set the text for each field
            binding.textViewLocation.text = "Location: ${delivery.location}"
            binding.textViewQuantity.text = "Quantity: ${delivery.quantity}"
            binding.textViewStatus.text = "Status: ${delivery.status}"
            binding.textViewDriverId.text = "Driver ID: ${delivery.driverId}"

            // Load the captured image (if available) into the ImageView
            if (delivery.image.isNotEmpty()) {
                Glide.with(binding.root.context)
                    .load(delivery.image) // Replace with the actual image URL
                    .into(binding.imageViewCapturedImage)
            } else {
                // If no image is available, set a placeholder
                binding.imageViewCapturedImage.setImageResource(R.drawable.placeholder_image)
            }

            // Set up the "Continue with Delivery" button click listener
            binding.buttonContinueDelivery.setOnClickListener {
                onContinueClick(delivery)  // Invoke the lambda function passed into the adapter
            }
        }
    }
}
