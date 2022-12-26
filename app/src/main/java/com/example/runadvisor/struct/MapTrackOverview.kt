package com.example.runadvisor.struct
import android.content.Context
import com.example.runadvisor.methods.getDoubleToGeoPoint
import com.example.runadvisor.widget.OverlayTrackOverview
import com.example.runadvisor.widget.TrackOverviewMarker
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay

class MapTrackOverview(val context: Context,val map: MapView):MapTrack(context,map) {
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