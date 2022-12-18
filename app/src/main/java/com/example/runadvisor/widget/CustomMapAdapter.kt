package com.example.runadvisor.widget
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.runadvisor.R
import com.example.runadvisor.methods.loadImageFromBitmap
import com.example.runadvisor.struct.SavedTrack

class CustomMapAdapter(private val activity: Activity): RecyclerView.Adapter<CustomMapAdapter.ViewHolder>() {
    val userData = ArrayList<SavedTrack>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomMapAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.upload_track_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(userData.isEmpty()){return}
        val itemsViewModel = userData[position]
        //activity.downloadImage(itemsViewModel,holder.cardImageView)
        activity.loadImageFromBitmap(itemsViewModel.bitmap,holder.cardImageView)
        holder.cityTextView.text = itemsViewModel.city
        holder.streetTextView.text = itemsViewModel.street
        holder.trackLengthTextView.text = itemsViewModel.trackLength
        holder.shareWithPublic.isChecked = true
    }

    override fun getItemCount(): Int {
        return userData.size
    }
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val cardImageView: ImageView = itemView.findViewById(R.id.trackImageView)
        val cityTextView: TextView = itemView.findViewById(R.id.trackCityText)
        val streetTextView: TextView = itemView.findViewById(R.id.trackStreetText)
        val trackLengthTextView: TextView = itemView.findViewById(R.id.trackKmText)
        val shareWithPublic: CheckBox = itemView.findViewById(R.id.trackShare)
    }
}