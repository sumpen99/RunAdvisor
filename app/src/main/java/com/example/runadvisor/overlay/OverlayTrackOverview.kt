package com.example.runadvisor.overlay
import android.content.Context
import android.graphics.Point
import androidx.appcompat.content.res.AppCompatResources
import com.example.runadvisor.marker.TrackOverviewMarker
import com.example.runadvisor.methods.getDoubleToGeoPoint
import com.example.runadvisor.methods.getDoubleToGeoPoints
import com.example.runadvisor.map.MapShowTrack
import com.example.runadvisor.struct.RunItem
import org.osmdroid.api.IMapView
import org.osmdroid.views.overlay.ItemizedIconOverlay

class OverlayTrackOverview(private val activityContext: Context,
                           private val markers:MutableList<TrackOverviewMarker>,
                           private val pOnItemGestureListener: OnItemGestureListener<TrackOverviewMarker>,
                           private val mapTrackOverview: MapShowTrack
):
    ItemizedIconOverlay<TrackOverviewMarker>(activityContext,markers,pOnItemGestureListener) {

    var markerHasTouch: TrackOverviewMarker? = null
    private val drawableDefault = AppCompatResources.getDrawable(activityContext,org.osmdroid.wms.R.drawable.marker_default)

    private fun callbackMarkerWithTouch(marker: TrackOverviewMarker){
        mapTrackOverview.removePolyline()
        if(markerHasTouch != null && marker.index == markerHasTouch!!.index){
            markerHasTouch = null
        }
        else{
            markerHasTouch = marker
            mapTrackOverview.buildPolyline(marker.trackPoints)
            mapTrackOverview.addPolyLineToMap()
            mapTrackOverview.invalidate()
        }
    }

    fun addCustomItem(runItem:RunItem, index:Int){
        val marker = TrackOverviewMarker(
            runItem.city!!,
            runItem.street!!,
            getDoubleToGeoPoint(runItem.center!!),
            getDoubleToGeoPoints(runItem.coordinates!!),
            ::callbackMarkerWithTouch,
            index,)
        marker.setMarker(drawableDefault)
        super.addItem(marker)
    }

    override fun addItems(items: MutableList<TrackOverviewMarker>?): Boolean {
        return super.addItems(items)
    }

    override fun createItem(i: Int): TrackOverviewMarker {
        return markers[i]
    }

    override fun onSnapToItem(x: Int, y: Int, snapPoint: Point?, mapView: IMapView?): Boolean {
        return false
    }

    override fun size(): Int {
        if(markers == null){return 0}
        return markers.size
    }

    /*override fun onTouchEvent(event: MotionEvent, mapView: MapView?): Boolean {
        when(event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {}
            MotionEvent.ACTION_MOVE -> {}
            MotionEvent.ACTION_UP -> {}
            MotionEvent.ACTION_CANCEL -> {}
            /*
            MotionEvent.ACTION_POINTER_DOWN -> {}
            MotionEvent.ACTION_POINTER_UP -> {}
            MotionEvent.ACTION_CANCEL -> {}
            */
        }
        return super.onTouchEvent(event, mapView)
    }*/
}