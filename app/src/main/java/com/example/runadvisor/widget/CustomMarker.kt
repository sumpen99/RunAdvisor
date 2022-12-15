package com.example.runadvisor.widget
import android.graphics.drawable.Drawable
import androidx.core.graphics.scaleMatrix
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.OverlayItem

class CustomMarker(
    title:String,
    snippet:String,
    var geoPoint:GeoPoint,
    val callbackSelectedMarker:(CustomMarker)->Unit,
    val drawableDefault: Drawable?,
    val drawableSelected:Drawable?):
    OverlayItem(title,snippet,geoPoint){

    var lastGeoPoint:GeoPoint = geoPoint

    fun hasTouch(){
        lastGeoPoint = geoPoint
        callbackSelectedMarker(this)
        setMarker(drawableSelected)
    }

    fun lostTouch(){
        setMarker(drawableDefault)
    }

    fun move(p:GeoPoint){
        val mlat = p.latitude - lastGeoPoint.latitude
        val mlon = p.longitude - lastGeoPoint.longitude
        geoPoint.latitude+=mlat
        geoPoint.longitude+=mlon
        lastGeoPoint = p
    }

}