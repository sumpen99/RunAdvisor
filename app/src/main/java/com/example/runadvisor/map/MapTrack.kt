package com.example.runadvisor.map
import android.content.Context
import android.graphics.Color
import com.example.runadvisor.overlay.OverlayClickableMarker
import com.example.runadvisor.struct.RunItem
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.Polyline


abstract class MapTrack(val activityContext: Context,
                   val mapView: MapView,) {
    var rgb = Color.rgb(0,191,255)
    var polyLine: Polyline? = null
    var bbox = BoundingBox()
    lateinit var currentOverlay:Overlay

    open fun setZoomLevel(zoom:Double){
        mapView.controller.setZoom(zoom)
    }

    open fun setPosition(zoom:Double){
        mapView.controller.setZoom(zoom)
    }

    open fun setBbox(latSouth:Double,latNorth:Double,lonWest:Double,lonEast:Double){
        bbox.latSouth = latSouth
        bbox.latNorth = latNorth
        bbox.lonWest = lonWest
        bbox.lonEast = lonEast
    }

    open fun setBbox(){
        setBbox(90.0,-90.0,180.0,-180.0)
    }

    open fun buildPolyline(points:List<GeoPoint>){
        polyLine = Polyline(mapView,false)
        polyLine!!.setPoints(points)
        setLineColor()
    }

    private fun setLineColor(){
        polyLine!!.infoWindow = null
        polyLine!!.color = rgb
    }

    protected fun addMarkersToMap(){
        mapView.overlays.add(currentOverlay)
        invalidate()
    }

    open fun addPolyLineToMap(){
        mapView.overlays.add(mapView.overlays.size,polyLine!!)
    }

    open fun invalidate(){
        mapView.postInvalidate()
    }

    fun removeCurrentOverlay(){
        mapView.overlays.remove(currentOverlay)
        invalidate()
    }

    open fun removePolyline(){
        mapView.overlays.remove(polyLine)
        invalidate()
    }

    fun removeOverlayAndPolyLine(){
        mapView.overlays.remove(currentOverlay)
        mapView.overlays.remove(polyLine)
        polyLine = null
        invalidate()
    }

}