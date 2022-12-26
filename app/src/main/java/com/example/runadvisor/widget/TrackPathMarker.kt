package com.example.runadvisor.widget
import android.graphics.drawable.Drawable
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.OverlayItem

class TrackPathMarker(
    title:String,
    snippet:String,
    val index:Int,
    var geoPoint:GeoPoint,
    val callbackSelectedMarker:(TrackPathMarker)->Unit,
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