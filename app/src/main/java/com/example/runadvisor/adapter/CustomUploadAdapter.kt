package com.example.runadvisor.adapter
import android.annotation.SuppressLint
import android.app.Activity
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.runadvisor.R
import com.example.runadvisor.methods.hideKeyboard
import com.example.runadvisor.methods.loadImageFromBitmap
import com.example.runadvisor.struct.SavedTrack
import com.example.runadvisor.widget.CustomImageButton

class CustomUploadAdapter(private val activity: Activity): RecyclerView.Adapter<CustomUploadAdapter.ViewHolder>() {
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
        if(pos>=itemCount || pos < 0){return}
        userData.removeAt(pos)
        notifyItemRemoved(pos)
    }

    fun updateCityText(pos:Int,str:String){
        if(pos>=itemCount){return}
        userData[pos].city = str
    }

    fun updateStreetText(pos:Int,str:String){
        if(pos>=itemCount || pos < 0){return}
        userData[pos].street = str
    }

    fun clearView(){
        if(userData.isNotEmpty()){
            val lastIndex = itemCount
            userData.clear()
            notifyItemRangeRemoved(0,lastIndex)
        }
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
        holder.cityTextView.hint = itemsViewModel.city
        holder.streetTextView.hint = itemsViewModel.street
        holder.trackLengthTextView.text = itemsViewModel.trackLength + " km"
        holder.dateTextView.text = itemsViewModel.date
    }

    override fun getItemCount(): Int {
        return userData.size
    }
    @SuppressLint("ClickableViewAccessibility")
    inner class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val cardImageView: ImageView = itemView.findViewById(R.id.trackImageView)
        val cityTextView: EditText = itemView.findViewById(R.id.trackCityText)
        val streetTextView: EditText = itemView.findViewById(R.id.trackStreetText)
        val trackLengthTextView: TextView = itemView.findViewById(R.id.trackKmText)
        val removeCardBtn: CustomImageButton = itemView.findViewById(R.id.trackRemove)
        val dateTextView:TextView = itemView.findViewById(R.id.trackDateText)

        init{
            removeCardBtn.setCallback(null,::removeSelf)
            ItemView.setOnTouchListener { v, event ->
                when(event.actionMasked){
                    MotionEvent.ACTION_DOWN -> {cityTextView.hideKeyboard();streetTextView.hideKeyboard()}
                }
                true
            }
            cityTextView.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?){
                    updateCityText(bindingAdapterPosition,s.toString())
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }
            })
            streetTextView.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?){
                    updateStreetText(bindingAdapterPosition,s.toString())
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }
            })

        }

        private fun removeSelf(parameter:Any?){
            removeCard(bindingAdapterPosition)
        }
    }
}