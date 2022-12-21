package com.example.runadvisor.widget
import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.runadvisor.R
import com.example.runadvisor.methods.loadImageFromBitmap
import com.example.runadvisor.struct.SavedTrack

class CustomMapAdapter(private val activity: Activity): RecyclerView.Adapter<CustomMapAdapter.ViewHolder>() {
    private val userData = ArrayList<SavedTrack>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.upload_track_card, parent, false)
        return ViewHolder(view)
    }

    fun getSavedTrack(pos:Int):SavedTrack?{
        if(pos>=itemCount){return null}
        return userData[pos]
    }

    fun removeCard(pos:Int){
        if(pos>=itemCount){return}
        userData.removeAt(pos)
        notifyItemRemoved(pos)
    }

    fun clearView(){
        if(userData.isNotEmpty()){
            val lastIndex = itemCount
            userData.clear()
            notifyItemRangeRemoved(0,lastIndex)
        }
    }

    fun addItem(item:SavedTrack){
        userData.add(item)
        notifyItemInserted(itemCount)
    }

    fun addItems(items:ArrayList<SavedTrack>){
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
    }

    override fun getItemCount(): Int {
        return userData.size
    }

    inner class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val cardImageView: ImageView = itemView.findViewById(R.id.trackImageView)
        val cityTextView: TextView = itemView.findViewById(R.id.trackCityText)
        val streetTextView: TextView = itemView.findViewById(R.id.trackStreetText)
        val trackLengthTextView: TextView = itemView.findViewById(R.id.trackKmText)
        val removeCardBtn: CustomImageButton = itemView.findViewById(R.id.trackRemove)

        init{
            removeCardBtn.setCallback(null,::removeSelf)
        }

        private fun removeSelf(parameter:Any?){
            removeCard(bindingAdapterPosition)
        }
    }
}