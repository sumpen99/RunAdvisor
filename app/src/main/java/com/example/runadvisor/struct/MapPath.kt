package com.example.runadvisor.struct

import android.content.Context
import android.graphics.Color
import androidx.appcompat.content.res.AppCompatResources
import com.example.runadvisor.widget.CustomMarker
import org.mapsforge.map.rendertheme.renderinstruction.Line
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.OverlayItem
import org.osmdroid.views.overlay.Polyline
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class MapPath(val activityContext: Context,val mapView:MapView) {
    var points = ArrayList<GeoPoint>()
    var rgb = Color.rgb(0,191,255)
    lateinit var line:Polyline
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

    private fun drawLassoPoints(){
        var i = 0
        val markers = ArrayList<OverlayItem>()
        while(i<points.size-1){
            val marker = CustomMarker("","",points[i])
            marker.setMarker(AppCompatResources.getDrawable(activityContext,org.osmdroid.wms.R.drawable.marker_default))
            markers.add(marker)
            i++
        }
        val items = ItemizedIconOverlay(activityContext,markers,null)
        mapView.overlays.add(items)
    }
}