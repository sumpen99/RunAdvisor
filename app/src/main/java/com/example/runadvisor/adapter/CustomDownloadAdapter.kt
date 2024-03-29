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
import com.example.runadvisor.enums.FragmentInstance
import com.example.runadvisor.methods.*
import com.example.runadvisor.search.insertBetweenClosest
import com.example.runadvisor.search.searchForRunItems
import com.example.runadvisor.sort.qSortRunItems
import com.example.runadvisor.struct.RunItem
import com.example.runadvisor.struct.SearchInfo
import com.example.runadvisor.widget.CustomImageButton
import org.osmdroid.util.GeoPoint

class CustomDownloadAdapter(private val activity: MainActivity):RecyclerView.Adapter<CustomDownloadAdapter.ViewHolder>() {
    val serverData = ArrayList<RunItem>()
    var occupied:Boolean = false


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.download_track_card, parent, false)

        return ViewHolder(view)
    }

    fun isOccupied():Boolean{
        return occupied
    }

    private fun setIsOccupied(value:Boolean){
        occupied = value
    }

    fun sortRunItems(){
        if(itemCount>1){
            qSortRunItems(serverData,0,serverData.size-1,activity.getSearchAxis())
            notifyDataSetChanged()
        }
    }

    fun addRunItems(runItems:List<RunItem>){
        setIsOccupied(true)
        if(itemCount==0){
            qSortRunItems(runItems,0,runItems.size-1,activity.getSearchAxis())
            serverData.addAll(runItems)
        }
        else{insertBetweenClosest(serverData,runItems,activity.getSearchAxis())}
        notifyDataSetChanged()
        setIsOccupied(false)
    }

    private fun removeCard(pos:Int){
        setIsOccupied(true)
        if(pos>=itemCount || pos < 0){return}
        serverData.removeAt(pos)
        notifyItemRemoved(pos)
        setIsOccupied(false)
    }

    fun removeRunItem(runItem:RunItem){
        setIsOccupied(true)
        val axis = activity.getSearchAxis()
        val searchInfo = SearchInfo()
        searchForRunItems(serverData,runItem.compare(axis),axis,searchInfo)
        if(searchInfo.found){
            var i = searchInfo.leftMin
            var foundIndex = -1
            while(i<=searchInfo.rightMax){
                if(serverData[i].docID.equals(runItem.docID)){
                    foundIndex = i
                    break
                }
                i++
            }
            if(foundIndex!=-1){removeCard(foundIndex)}
        }
        setIsOccupied(false)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(serverData.isEmpty()){return}
        val itemsViewModel = serverData[position]
        activity.downloadImageFromStorage(activity.getImageStorageRef(itemsViewModel.downloadUrl),holder.cardImageView)
        holder.cityTextView.text = itemsViewModel.city
        holder.streetTextView.text = itemsViewModel.street
        holder.trackLengthTextView.text = "${itemsViewModel.trackLength} km"
        holder.distTextView.text = (itemsViewModel.range/1000.0).format(2) + " km"
        holder.zoom = itemsViewModel.zoom!!
        holder.centerPoint = GeoPoint(itemsViewModel.center!![0],itemsViewModel.center!![1])
        holder.trackPoints = getDoubleToGeoPoints(itemsViewModel.coordinates!!)
        holder.dateTextView.text = itemsViewModel.date
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
        val dateTextView:TextView = itemView.findViewById(R.id.itemDateText)
        val showTrackOnMapBtn: CustomImageButton = itemView.findViewById(R.id.showTrackOnMap)
        var trackPoints:ArrayList<GeoPoint> = ArrayList()
        var centerPoint:GeoPoint = GeoPoint(0.0,0.0)
        var zoom:Double = 18.0
        init{
            showTrackOnMapBtn.setCallback(null,::launchMap)
        }

        private fun launchMap(parameter:Any?){
            activity.navigateFragment(FragmentInstance.FRAGMENT_MAP_TRACK_ITEM,this)
        }
    }
}