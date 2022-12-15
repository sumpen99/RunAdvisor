package com.example.runadvisor.struct

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import androidx.appcompat.content.res.AppCompatResources
import com.example.runadvisor.R
import com.example.runadvisor.io.printToTerminal
import com.example.runadvisor.widget.CustomMarker
import com.example.runadvisor.widget.CustomOverlay
import org.mapsforge.map.rendertheme.renderinstruction.Line
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.*
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

//https://gist.github.com/jaydee2991/1306961/88c8d5ffadceb0a8df0eff0a889206069b236989
//https://stackoverflow.com/questions/23108709/show-marker-details-with-image-onclick-marker-openstreetmap

class MapPath(val activityContext: Context,val mapView:MapView) {
    var points = ArrayList<GeoPoint>()
    var rgb = Color.rgb(0,191,255)
    lateinit var line:Polyline
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

    init{
        buildLasso()
        buildPolyline()
        setLineColor()
    }

    private fun buildLasso(){
        val bbox = mapView.boundingBox
        val p = 10.0
        val step = 360.0*PI/180.0/p
        var t = 0.0
        val cy = bbox.centerLatitude
        val cx = bbox.centerLongitude
        val r = bbox.lonEast-cx
        var i = 0
        while(i++<p){
            val lat = r * sin(t) + cy
            val lon = r * cos(t) + cx
            points.add(GeoPoint(lat,lon))
            t+=step
        }
        points.add(points[0])
    }

    private fun buildPolyline(){
        line = Polyline(mapView)
        line.setPoints(points)
    }

    private fun setLineColor(){
        line.infoWindow = null
        line.color = rgb
    }

    fun drawSelf(){
        drawLasso()
        drawLassoPoints()
        mapView.invalidate()
    }

    private fun drawLasso(){
        mapView.overlays.add(mapView.overlays.size,line)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun drawLassoPoints(){
        var i = 0
        val overlay = CustomOverlay(activityContext,ArrayList<CustomMarker>(),gestureListener)
        while(i<points.size-1){
            overlay.addCustomItem("","",points[i])
            i++
        }
        mapView.overlays.add(overlay)
    }
}