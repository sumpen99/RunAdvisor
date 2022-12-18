package com.example.runadvisor.widget
import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.runadvisor.R
import com.example.runadvisor.io.printToTerminal
import com.example.runadvisor.methods.loadImageFromBitmap
import com.example.runadvisor.struct.SavedTrack

class CustomMapAdapter(private val activity: Activity): RecyclerView.Adapter<CustomMapAdapter.ViewHolder>() {
    private val userData = ArrayList<SavedTrack>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.upload_track_card, parent, false)
        return ViewHolder(view)
    }

    fun clearView(){
        if(userData.isNotEmpty()){
            val lastIndex = itemCount
            userData.clear()
            notifyItemRangeRemoved(1,lastIndex)
        }
    }

    fun addItem(item:SavedTrack){
        userData.add(item)
        notifyItemInserted(itemCount)
    }

    fun addItems(items:ArrayList<SavedTrack>){
        printToTerminal("${items.size}")
        val start = itemCount
        userData.addAll(items)
        notifyItemRangeInserted(start,items.size)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(userData.isEmpty()){return}
        val itemsViewModel = userData[position]
        activity.loadImageFromBitmap(itemsViewModel.bitmap,holder.cardImageView)
        holder.cityTextView.text = itemsViewModel.city
        holder.streetTextView.text = itemsViewModel.street
        holder.trackLengthTextView.text = itemsViewModel.trackLength + " km"
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