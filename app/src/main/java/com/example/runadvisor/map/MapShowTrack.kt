package com.example.runadvisor.map
import android.content.Context
import com.example.runadvisor.overlay.OverlayClickableMarker
import com.example.runadvisor.marker.TrackOverviewMarker
import com.example.runadvisor.struct.RunItem
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay

class MapShowTrack(val context: Context, val map: MapView): MapTrack(context,map) {

    val bbox = BoundingBox()
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
        currentOverlay = OverlayClickableMarker(activityContext,ArrayList<TrackOverviewMarker>(),gestureListener,this)
    }

    fun addOverviewMarkers(runItems:List<RunItem>?){
        if(runItems==null){return}

        var i = 0
        var latNorth = runItems[0].center!![0]
        var latSouth = runItems[0].center!![0]
        var lonEast = runItems[0].center!![1]
        var lonWest = runItems[0].center!![1]

        while(i<runItems.size){
            val item = runItems[i]
            latNorth = Math.max(latNorth,item.center!![0])
            latSouth = Math.min(latSouth,item.center!![0])

            lonEast = Math.max(lonEast,item.center!![1])
            lonWest = Math.min(lonWest,item.center!![1])
            (currentOverlay as OverlayClickableMarker).addCustomItem(item,i,)
            i++
        }
        addMarkersToMap()

        bbox.lonWest = lonWest
        bbox.lonEast = lonEast
        bbox.latNorth = latNorth
        bbox.latSouth = latSouth

    }



}