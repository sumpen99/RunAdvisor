package com.example.runadvisor.struct
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import com.example.runadvisor.methods.*
import com.example.runadvisor.widget.CustomMarker
import com.example.runadvisor.widget.OverlayTrackPath
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.*
import kotlin.collections.ArrayList
import kotlin.math.cos
import kotlin.math.sin


class MapTrackPath(val activityContext: Context,
                   val mapView:MapView,
                   val callbackUpdateTrackLength:(args:String)->Unit) {
    var currentOverlay:OverlayTrackPath? = null
    var points = ArrayList<GeoPoint>()
    var savedTracks = ArrayList<SavedTrack>()
    var trackLength:Double = 0.0
    var rgb = Color.rgb(0,191,255)
    val INCREASE_POINTS = 10
    val MAX_POINTS = 320
    var currentPoints = 0
    var polyLine:Polyline? = null
    val gestureListener = object:ItemizedIconOverlay.OnItemGestureListener<CustomMarker>{
        override fun onItemSingleTapUp(index:Int, item:CustomMarker):Boolean {
            //item.hasTouch()
            return true
        }
        override fun onItemLongPress(index:Int, item:CustomMarker):Boolean {
            item.hasTouch()
            return true
        }
    }

    fun setLasso(pointsToAdd:Int){
        buildLasso(pointsToAdd)
        buildPolyLine()
    }

    fun buildPolyLine(){
        buildPolyline()
        setLineColor()
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

    private fun buildPolyline(){
        polyLine = Polyline(mapView,false)
        polyLine!!.setPoints(points)
    }

    private fun setLineColor(){
        polyLine!!.infoWindow = null
        polyLine!!.color = rgb
    }

    fun updateLinePoints(index:Int,geoPoint:GeoPoint){
        //line.points[index] = geoPoint
        points[index] = geoPoint
        polyLine!!.setPoints(points)
    }

    fun addLassoOverlay(){
        addLassoLines()
        addLassoMarkers()
        drawLasso()
        getNewTrackLength()
    }

    fun invalidate(){
        mapView.postInvalidate()
    }

    fun getNewTrackLength(){
        trackLength = calculateTrackLength(points)
        callbackUpdateTrackLength(getStringTrackLength())
    }

    private fun getTrackLengthInKm():Double{
        return trackLength/1000
    }

    private fun getStringTrackLength():String{
        return getTrackLengthInKm().format(4)
    }

    fun drawLasso(){
        mapView.postInvalidate()
    }

    private fun addLassoLines(){
        mapView.overlays.add(mapView.overlays.size,polyLine!!)
    }

    private fun addLassoMarkers(){
        var i = 0
        currentOverlay = OverlayTrackPath(activityContext,ArrayList<CustomMarker>(),gestureListener,this)
        while(i<points.size){
            currentOverlay!!.addCustomItem("","",points[i],i)
            i++
        }
        mapView.overlays.add(currentOverlay)
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

    fun removeOverlayMarkers(){
        if(currentOverlay != null){
            mapView.overlays.remove(currentOverlay)
            invalidate()
        }
    }

    fun removeOverlaysFromMap(){
        if(currentOverlay != null){
            mapView.overlays.remove(currentOverlay)
            mapView.overlays.remove(polyLine)
            polyLine = null
            resetTrackLength()
        }
    }

    fun resetMapTrackPath(){
        removeOverlaysFromMap()
        points.clear()
        currentPoints = 0
        getNewTrackLength()
        invalidate()
    }

    private fun resetTrackLength(){
        trackLength = 0.0
    }

    fun saveCurrentTrack(bitmap:Bitmap,zoom:Int,city:String,street:String,):Boolean{
        val centerGeoPoint = getCenterOfPoints(points)
        if(centerGeoPoint!=null){
            savedTracks.add(
                SavedTrack(
                    bitmap,
                    ArrayList(points),
                    centerGeoPoint,
                    zoom,
                    city,
                    street,
                    getStringTrackLength()))
            currentPoints = 0
            points.clear()
            //invalidate()
            return true
        }
        return false
    }

    fun trackIsOnMap():Boolean{
        return (points.size > 0 && currentOverlay != null && polyLine != null)
    }

    /*
    *   ##########################################################################
    *                               OVERVIEW MAP
    *   ##########################################################################
    * */

    fun addOverviewMarkers(runItems:List<RunItem>?){
        if(runItems==null){return}
        if(currentOverlay==null){
            currentOverlay = OverlayTrackPath(activityContext,ArrayList<CustomMarker>(),gestureListener,this)
        }
        var i = 0
        while(i<runItems.size){
            val item = runItems[i]
            currentOverlay!!.addCustomItem("","", getDoubleToGeoPoint(item.coordinates!!),i)
            i++
        }
        mapView.overlays.add(currentOverlay)
        invalidate()
    }
}