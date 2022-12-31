package com.example.runadvisor.map
import android.content.Context
import android.graphics.Bitmap
import androidx.core.view.drawToBitmap
import com.example.runadvisor.methods.*
import com.example.runadvisor.marker.TrackPathMarker
import com.example.runadvisor.overlay.OverlayTrackPath
import com.example.runadvisor.struct.SavedTrack
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.*
import kotlin.collections.ArrayList
import kotlin.math.cos
import kotlin.math.sin


class MapTrackPath(val context: Context,
                   val map:MapView,
                   val callbackUpdateTrackLength:(args:String)->Unit): MapTrack(context,map) {
    var points = ArrayList<GeoPoint>()
    var savedTracks = ArrayList<SavedTrack>()
    var trackLength:Double = 0.0
    val INCREASE_POINTS = 10
    val MAX_POINTS = 1280
    var currentPoints = 0
    val gestureListener = object:ItemizedIconOverlay.OnItemGestureListener<TrackPathMarker>{
        override fun onItemSingleTapUp(index:Int, item: TrackPathMarker):Boolean {
            //item.hasTouch()
            return true
        }
        override fun onItemLongPress(index:Int, item: TrackPathMarker):Boolean {
            item.hasTouch()
            return true
        }
    }

    fun setCurrentOverlay(){
        currentOverlay = OverlayTrackPath(activityContext,ArrayList<TrackPathMarker>(),gestureListener,this)
    }

    private fun buildLasso(pointsToAdd:Int){
        val bbox = mapView.boundingBox
        currentPoints += INCREASE_POINTS*pointsToAdd
        currentPoints  = Math.max(currentPoints,INCREASE_POINTS)
        val step = toRadians(360.0)/currentPoints
        var t = 0.0
        val cy = bbox.centerLatitude
        val cx = bbox.centerLongitude
        val r = bbox.lonEast-cx
        var i = 0
        while(i++<currentPoints){
            val lat = r * sin(t) + cy
            val lon = r * cos(t) + cx
            points.add(GeoPoint(lat,lon))
            t+=step
        }
        points.add(GeoPoint(points[0].latitude,points[0].longitude))
    }

    private fun addPolylineMarkers(){
        var i = 0
        currentOverlay = OverlayTrackPath(activityContext,ArrayList<TrackPathMarker>(),gestureListener,this)
        while(i<points.size){
            (currentOverlay as OverlayTrackPath).addCustomItem("","",points[i],i)
            i++
        }
        mapView.overlays.add((currentOverlay as OverlayTrackPath))
    }

    private fun resetTrackLength(){
        trackLength = 0.0
    }

    fun adjustPolyline(){
        removeOverlayAndPolyLine()
        resetTrackLength()
        buildPolyline(points)
    }

    fun setLasso(pointsToAdd:Int){
        buildLasso(pointsToAdd)
        buildPolyline(points)
    }

    fun updateLinePoints(index:Int,geoPoint:GeoPoint){
        //line.points[index] = geoPoint
        points[index] = geoPoint
        polyLine!!.setPoints(points)
    }

    fun addLassoOverlay(){
        addPolyLineToMap()
        addPolylineMarkers()
        getNewTrackLength()
        invalidate()
    }

    fun getNewTrackLength(){
        trackLength = calculateTrackLength(points)
        callbackUpdateTrackLength(trackLength.inKilometers())
    }

    fun adjustLasso(numPoints:Int):Boolean{
        val temPoints = ArrayList<GeoPoint>()
        var i = 0
        if(numPoints<0){
            if(currentPoints <= INCREASE_POINTS+1){return false}
            while(i<points.size){
                temPoints.add(points[i])
                i+=2
            }
            points = temPoints
        }
        else{
            if(currentPoints > MAX_POINTS){return false}
            if(currentPoints == 0){buildLasso(1);return true}
            i = 1
            var newPoint:GeoPoint = GeoPoint(0.0,0.0)
            var lastPoint:GeoPoint = points[0]
            var currentPoint:GeoPoint = GeoPoint(0.0,0.0)
            while(i<points.size){
                currentPoint = points[i]
                newPoint = getGeoMiddle(lastPoint,currentPoint)
                temPoints.add(lastPoint)
                temPoints.add(newPoint)
                lastPoint = currentPoint
                i++
            }
            temPoints.add(points[points.size-1])
            points = temPoints
        }
        currentPoints = points.size
        return true
    }

    fun resetMapTrackPath(){
        removeOverlayAndPolyLine()
        resetTrackLength()
        points.clear()
        currentPoints = 0
        getNewTrackLength()
        invalidate()
    }

    fun saveCurrentTrack(city:String,street:String){
        val centerGeoPoint = getCenterOfPoints(points)
        if(centerGeoPoint!=null){
            removeCurrentOverlay()
            savedTracks.add(
                SavedTrack(
                    mapView.drawToBitmap(),
                    ArrayList(points),
                    centerGeoPoint,
                    mapView.zoomLevel,
                    city,
                    street,
                    trackLength.inKilometers(),
                    getCurrentDate())
            )
            currentPoints = 0
            points.clear()
        }
    }

    fun trackIsOnMap():Boolean{
        return (points.size > 0 && currentOverlay != null && polyLine != null)
    }

}