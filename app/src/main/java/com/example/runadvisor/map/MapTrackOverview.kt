package com.example.runadvisor.map
import android.content.Context
import com.example.runadvisor.overlay.OverlayTrackOverview
import com.example.runadvisor.marker.TrackOverviewMarker
import com.example.runadvisor.struct.RunItem
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay

class MapTrackOverview(val context: Context,val map: MapView): MapTrack(context,map) {
    val gestureListener = object: ItemizedIconOverlay.OnItemGestureListener<TrackOverviewMarker>{
        override fun onItemSingleTapUp(index:Int, item: TrackOverviewMarker):Boolean {
            item.hasTouch()
            return true
        }
        override fun onItemLongPress(index:Int, item: TrackOverviewMarker):Boolean {
            //item.hasTouch()
            return true
        }
    }

    fun setCurrentOverlay(){
        currentOverlay = OverlayTrackOverview(activityContext,ArrayList<TrackOverviewMarker>(),gestureListener,this)
    }

    fun addOverviewMarkers(runItems:List<RunItem>?){
        if(runItems==null){return}
        var i = 0
        while(i<runItems.size){
            val item = runItems[i]
            (currentOverlay as OverlayTrackOverview).addCustomItem(
                item,
                i,)
            i++
        }
        addMarkersToMap()
    }



}