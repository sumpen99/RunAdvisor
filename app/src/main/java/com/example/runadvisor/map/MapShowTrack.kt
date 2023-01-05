package com.example.runadvisor.map
import android.content.Context
import com.example.runadvisor.io.printToTerminal
import com.example.runadvisor.overlay.OverlayClickableMarker
import com.example.runadvisor.marker.MarkerClickable
import com.example.runadvisor.struct.RunItem
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay

class MapShowTrack(val context: Context,
                   val map: MapView,
                   val callBackMarkerHasTouch:(args:MarkerClickable)->Unit): MapTrack(context,map) {
    val gestureListener = object: ItemizedIconOverlay.OnItemGestureListener<MarkerClickable>{
        override fun onItemSingleTapUp(index:Int, item: MarkerClickable):Boolean {
            callBackMarkerHasTouch(item)
            return true
        }

        override fun onItemLongPress(index:Int, item: MarkerClickable):Boolean {
            return true
        }

    }

    fun setCurrentOverlay(){
        currentOverlay = OverlayClickableMarker(activityContext,ArrayList<MarkerClickable>(),gestureListener)
    }

    fun addOverviewMarkers(runItems:List<RunItem>?){
        if(runItems==null){return}
        setBbox()
        var i = 0
        while(i<runItems.size){
            val item = runItems[i]
            if(item.center!![0] < bbox.latSouth) {
                bbox.latSouth = item.center!![0]
            }

            if(item.center!![0] > bbox.latNorth) {
                bbox.latNorth = item.center!![0]
            }

            if(item.center!![1] < bbox.lonWest) {
                bbox.lonWest = item.center!![1]
            }

            if(item.center!![1] > bbox.lonEast) {
                bbox.lonEast = item.center!![1]
            }

            (currentOverlay as OverlayClickableMarker).addCustomItem(item,i)
            i++
        }
        addMarkersToMap()
    }



}