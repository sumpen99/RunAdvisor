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
import com.example.runadvisor.activity.HomeActivity
import com.example.runadvisor.enums.FragmentInstance
import com.example.runadvisor.methods.downloadImage
import com.example.runadvisor.methods.format
import com.example.runadvisor.methods.getDoubleToGeoPoints
import com.example.runadvisor.struct.RunItem
import org.osmdroid.util.GeoPoint

class CustomDataAdapter(private val activity: Activity):RecyclerView.Adapter<CustomDataAdapter.ViewHolder>() {
    val serverData = ArrayList<RunItem>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.download_track_card, parent, false)

        return ViewHolder(view)
    }

    fun addRunItems(runItems:List<RunItem>){
        if(itemCount==0){}
        val pos = serverData.size
        serverData.addAll(runItems)
        notifyItemRangeChanged(pos,runItems.size)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(serverData.isEmpty()){return}
        val itemsViewModel = serverData[position]
        activity.downloadImage(itemsViewModel,holder.cardImageView)
        holder.cityTextView.text = itemsViewModel.city
        holder.streetTextView.text = itemsViewModel.street
        holder.trackLengthTextView.text = "${itemsViewModel.trackLength} km"
        holder.distTextView.text = itemsViewModel.range.format(0) +  " km"
        holder.zoom = itemsViewModel.zoom!!
        holder.centerPoint = GeoPoint(itemsViewModel.center!![0],itemsViewModel.center[1])
        holder.trackPoints = getDoubleToGeoPoints(itemsViewModel.coordinates!!)
    }

    override fun getItemCount(): Int {
        return serverData.size
    }

    inner class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val cardImageView: ImageView = itemView.findViewById(R.id.itemImageView)
        val cityTextView: TextView = itemView.findViewById(R.id.itemCityText)
        val streetTextView: TextView = itemView.findViewById(R.id.itemStreetText)
        val trackLengthTextView: TextView = itemView.findViewById(R.id.itemKmText)
        val distTextView: TextView = itemView.findViewById(R.id.distText)
        val showTrackOnMapBtn: CustomImageButton = itemView.findViewById(R.id.showTrackOnMap)
        var trackPoints:ArrayList<GeoPoint> = ArrayList()
        var centerPoint:GeoPoint = GeoPoint(0.0,0.0)
        var zoom:Int = 18
        init{
            showTrackOnMapBtn.setCallback(null,::launchMap)
        }

        private fun launchMap(parameter:Any?){
            (activity as HomeActivity).navigateFragment(FragmentInstance.FRAGMENT_MAP_TRACK_ITEM,this)
        }
    }
}