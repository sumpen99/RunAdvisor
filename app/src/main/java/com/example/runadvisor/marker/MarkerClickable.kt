package com.example.runadvisor.marker
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.OverlayItem

class MarkerClickable(
    title:String,
    snippet:String,
    var geoPoint:GeoPoint,
    var trackPoints:List<GeoPoint>,
    val callbackSelectedMarker:(MarkerClickable)->Unit,
    val index:Int,):
    OverlayItem(title,snippet,geoPoint){


    fun hasTouch(){
        callbackSelectedMarker(this)
    }

}