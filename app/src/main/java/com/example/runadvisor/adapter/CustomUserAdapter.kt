package com.example.runadvisor.adapter
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.runadvisor.MainActivity
import com.example.runadvisor.R
import com.example.runadvisor.methods.downloadImageFromStorage
import com.example.runadvisor.struct.MessageToUser
import com.example.runadvisor.struct.RunItem
import com.example.runadvisor.widget.CustomImageButton

class CustomUserAdapter(private val activity: MainActivity): RecyclerView.Adapter<CustomUserAdapter.ViewHolder>() {
    private lateinit var messageToUser: MessageToUser
    val userData = ArrayList<RunItem>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_track_card, parent, false)
        setInfoToUser()
        return ViewHolder(view)
    }

    private fun setInfoToUser(){
        messageToUser = MessageToUser(activity,null)
        messageToUser.setMessage("All data will be removed")
        messageToUser.setTwoButtons()
    }

    fun removeCard(pos:Int){
        if(pos>=itemCount || pos < 0){return}
        activity.removeRunItemFromFirebase(userData[pos])
        userData.removeAt(pos)
        notifyItemRemoved(pos)
    }

    fun addUserItem(item: RunItem){
        userData.add(item)
        notifyItemInserted(itemCount)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(userData.isEmpty()){return}
        val itemsViewModel = userData[position]
        activity.downloadImageFromStorage(activity.getImageStorageRef(itemsViewModel.downloadUrl),holder.cardImageView)
        holder.cityTextView.text = itemsViewModel.city
        holder.streetTextView.text = itemsViewModel.street
        holder.trackLengthTextView.text = itemsViewModel.trackLength + " km"
        holder.dateTextView.text = itemsViewModel.date
    }

    override fun getItemCount(): Int {
        return userData.size
    }

    inner class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val cardImageView: ImageView = itemView.findViewById(R.id.userTrackImageView)
        val cityTextView: TextView = itemView.findViewById(R.id.userTrackCityText)
        val streetTextView: TextView = itemView.findViewById(R.id.userTrackStreetText)
        val trackLengthTextView: TextView = itemView.findViewById(R.id.userTrackKmText)
        val removeCardBtn: CustomImageButton = itemView.findViewById(R.id.userTrackRemove)
        val dateTextView: TextView = itemView.findViewById(R.id.userTrackDateText)

        init{
            removeCardBtn.setCallback(null,::askBeforeRemove)
        }

        private fun askBeforeRemove(parameter:Any?){
            messageToUser.setPositiveCallback(::removeSelf)
            messageToUser.showMessage()
        }

        private fun removeSelf(parameter:Any?){
            removeCard(bindingAdapterPosition)
        }
    }
}