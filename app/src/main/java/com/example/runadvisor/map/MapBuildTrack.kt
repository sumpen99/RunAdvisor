package com.example.runadvisor.map
import android.content.Context
import androidx.core.view.drawToBitmap
import com.example.runadvisor.io.printToTerminal
import com.example.runadvisor.methods.*
import com.example.runadvisor.marker.MarkerMovable
import com.example.runadvisor.overlay.OverlayMovableMarker
import com.example.runadvisor.struct.SavedTrack
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.*
import kotlin.collections.ArrayList
import kotlin.math.cos
import kotlin.math.sin


class MapBuildTrack(val context: Context,
                    val map:MapView): MapTrack(context,map) {
    private var callbackUpdateTrackLength:(args:String)->Unit = ::templateFunctionString
    var points = ArrayList<GeoPoint>()
    var savedTracks = ArrayList<SavedTrack>()
    var trackLength:Double = 0.0
    val INCREASE_POINTS = 10
    val MAX_POINTS = 1280
    var currentPoints = 0
    val gestureListener = object:ItemizedIconOverlay.OnItemGestureListener<MarkerMovable>{
        override fun onItemSingleTapUp(index:Int, item: MarkerMovable):Boolean {
            //item.hasTouch()
            return true
        }
        override fun onItemLongPress(index:Int, item: MarkerMovable):Boolean {
            item.hasTouch()
            return true
        }
    }

    fun setCurrentOverlay(){
        currentOverlay = OverlayMovableMarker(activityContext,ArrayList<MarkerMovable>(),gestureListener,this)
    }

    fun setCallbackUpdateTrackLength(callback:(args:String)->Unit){
        callbackUpdateTrackLength = callback
    }

    private fun buildTrack(pointsToAdd:Int){
        resetMapTrackPath()
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

    fun decreaseTrack():Boolean{
        if(currentPoints <= INCREASE_POINTS+1){printToTerminal("current points lesser");return false}
        val temPoints = ArrayList<GeoPoint>()
        var i = 0
        while(i<points.size){
            temPoints.add(points[i])
            i+=2
        }
        points = temPoints
        currentPoints = points.size
        return true
    }

    fun expandTrack():Boolean{
        if(currentPoints > MAX_POINTS){return false}
        if(currentPoints == 0){buildTrack(1);return true}
        val temPoints = ArrayList<GeoPoint>()
        var i = 1
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
        currentPoints = points.size
        return true
    }

    private fun addPolylineMarkers(){
        var i = 0
        currentOverlay = OverlayMovableMarker(activityContext,ArrayList<MarkerMovable>(),gestureListener,this)
        while(i<points.size){
            (currentOverlay as OverlayMovableMarker).addCustomItem("","",points[i],i)
            i++
        }
        mapView.overlays.add((currentOverlay as OverlayMovableMarker))
    }

    private fun resetTrackLength(){
        trackLength = 0.0
    }

    fun adjustPolyline(){
        removeOverlayAndPolyLine()
        resetTrackLength()
        buildPolyline(points)
    }

    fun setTrack(pointsToAdd:Int){
        buildTrack(pointsToAdd)
        buildPolyline(points)
    }

    fun updateLinePoints(index:Int,geoPoint:GeoPoint){
        //line.points[index] = geoPoint
        points[index] = geoPoint
        polyLine!!.setPoints(points)
    }

    fun addTrackOverlay(){
        addPolyLineToMap()
        addPolylineMarkers()
        getNewTrackLength()
        invalidate()
    }

    fun getNewTrackLength(){
        trackLength = calculateTrackLength(points)
        callbackUpdateTrackLength(trackLength.inKilometers())
    }

    fun resetMapTrackPath(){
        removeOverlayAndPolyLine()
        resetTrackLength()
        points.clear()
        currentPoints = 0
        getNewTrackLength()
        //invalidate()
    }

    fun resetMapGpsPath(){
        removeOverlayAndPolyLine()
    }

    fun saveCurrentTrack(city:String,street:String){
        val centerGeoPoint = points[0]
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

    fun saveGpsTrack(city:String,street:String,trackLen:String){
        savedTracks.add(
            SavedTrack(
                mapView.drawToBitmap(),
                ArrayList(polyLine!!.actualPoints),
                polyLine!!.actualPoints[0],
                mapView.zoomLevel,
                city,
                street,
                trackLen,
                getCurrentDate())
        )
    }

    fun trackIsOnMap():Boolean{
        return (polyLine != null)
    }

    fun pointsIsNotEmpty():Boolean{
        return (points.size > 0)
    }

}