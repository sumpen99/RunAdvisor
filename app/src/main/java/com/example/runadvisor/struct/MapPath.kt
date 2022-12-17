package com.example.runadvisor.struct
import android.content.Context
import android.graphics.Color
import com.example.runadvisor.io.printToTerminal
import com.example.runadvisor.methods.calculateTrackLength
import com.example.runadvisor.methods.getGeoMiddle
import com.example.runadvisor.methods.toRadians
import com.example.runadvisor.widget.CustomMarker
import com.example.runadvisor.widget.CustomOverlay
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.*
import kotlin.math.cos
import kotlin.math.sin


class MapPath(val activityContext: Context,val mapView:MapView) {
    var currentOverlay:CustomOverlay? = null
    var points = ArrayList<GeoPoint>()
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

    private fun printTrackLength(){
        printToTerminal("$trackLength")
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
        addLassoPoints()
        drawLasso()
    }

    fun invalidate(){
        mapView.postInvalidate()
    }

    fun drawLasso(){
        trackLength = calculateTrackLength(points)
        //printTrackLength()
        mapView.postInvalidate()
    }

    private fun addLassoLines(){
        mapView.overlays.add(mapView.overlays.size,polyLine!!)
    }

    private fun addLassoPoints(){
        var i = 0
        currentOverlay = CustomOverlay(activityContext,ArrayList<CustomMarker>(),gestureListener,this)
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

    fun removeOverlayFromMap(){
        if(currentOverlay != null){
            mapView.overlays.remove(currentOverlay)
            mapView.overlays.remove(polyLine)
            polyLine = null
            resetTrackLength()
        }
    }

    fun resetMapPath(){
        removeOverlayFromMap()
        points.clear()
        currentPoints = 0
        invalidate()
    }

    private fun resetTrackLength(){
        trackLength = 0.0
    }
}