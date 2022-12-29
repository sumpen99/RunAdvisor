package com.example.runadvisor.struct
import android.graphics.Bitmap
import org.osmdroid.util.GeoPoint

class SavedTrack(
    val bitmap:Bitmap,
    val geoPoints:ArrayList<GeoPoint>,
    val center:GeoPoint,
    val zoom:Int,
    var city:String,
    var street:String,
    val trackLength:String,
    val date:String)