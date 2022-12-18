package com.example.runadvisor.widget
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.runadvisor.R
import com.example.runadvisor.methods.downloadImage
import com.example.runadvisor.struct.PublicRunItem

class CustomDataAdapter(private val activity: Activity):RecyclerView.Adapter<CustomDataAdapter.ViewHolder>() {
    val serverData = ArrayList<PublicRunItem>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_view_design, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(serverData.isEmpty()){return}
        val itemsViewModel = serverData[position]
        activity.downloadImage(itemsViewModel,holder.cardImageView)
        holder.cityTextView.text = itemsViewModel.city
        holder.streetTextView.text = itemsViewModel.street
    }

    override fun getItemCount(): Int {
        return serverData.size
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val cardImageView: ImageView = itemView.findViewById(R.id.cardImageView)
        val cityTextView: TextView = itemView.findViewById(R.id.cardCityText)
        val streetTextView: TextView = itemView.findViewById(R.id.cardStreetText)
    }
}