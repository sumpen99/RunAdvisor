package com.example.runadvisor.overlay
import android.content.Context
import android.graphics.Point
import android.view.MotionEvent
import androidx.appcompat.content.res.AppCompatResources
import com.example.runadvisor.marker.MarkerMovable
import com.example.runadvisor.map.MapBuildTrack
import org.osmdroid.api.IMapView
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay

class OverlayMovableMarker(private val activityContext:Context,
                           private val markers:MutableList<MarkerMovable>,
                           private val pOnItemGestureListener:OnItemGestureListener<MarkerMovable>,
                           private val mapTrackPath: MapBuildTrack
):
    ItemizedIconOverlay<MarkerMovable>(activityContext,markers,pOnItemGestureListener) {

    var markerHasTouch: MarkerMovable? = null
    private val drawableDefault = AppCompatResources.getDrawable(activityContext,org.osmdroid.wms.R.drawable.marker_default)
    private val drawableSelected = AppCompatResources.getDrawable(activityContext,org.osmdroid.wms.R.drawable.center)

    private fun setMarkerWithTouch(marker: MarkerMovable){
        markerHasTouch = marker
        mapTrackPath.invalidate()
    }

    private fun resetMarkerWithTouch(){
        if(markerHasTouch!=null){
            markerHasTouch!!.lostTouch()
            markerHasTouch = null
        }
        mapTrackPath.invalidate()
    }

    fun addCustomItem(title:String,snippet:String,geoPoint:GeoPoint,index:Int){
        val marker = MarkerMovable(title,snippet,index,geoPoint,::setMarkerWithTouch,drawableDefault,drawableSelected)
        marker.setMarker(drawableDefault)
        super.addItem(marker)
    }

    override fun addItems(items: MutableList<MarkerMovable>?): Boolean {
        return super.addItems(items)
    }

    override fun createItem(i: Int): MarkerMovable {
        return markers[i]
    }

    override fun onSnapToItem(x: Int, y: Int, snapPoint: Point?, mapView: IMapView?): Boolean {
        return false
    }

    override fun size(): Int {
        if(markers == null){return 0}
        return markers.size
    }

    override fun onTouchEvent(event: MotionEvent, mapView: MapView?): Boolean {
        when(event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
            }
            MotionEvent.ACTION_MOVE -> {
                if(markerHasTouch!=null){
                    markerHasTouch!!.move(
                        GeoPoint(mapView!!.projection.fromPixels(event.x.toInt(),event.y.toInt())))
                    mapTrackPath.updateLinePoints(markerHasTouch!!.index,markerHasTouch!!.geoPoint)
                    mapTrackPath.invalidate()
                    mapTrackPath.getNewTrackLength()
                    return true
                }
            }
            MotionEvent.ACTION_UP -> {
                resetMarkerWithTouch()
            }
            MotionEvent.ACTION_CANCEL -> {
                resetMarkerWithTouch()
            }
            /*
            MotionEvent.ACTION_POINTER_DOWN -> {}
            MotionEvent.ACTION_POINTER_UP -> {}
            MotionEvent.ACTION_CANCEL -> {}
            */
        }
        return super.onTouchEvent(event, mapView)
    }
}