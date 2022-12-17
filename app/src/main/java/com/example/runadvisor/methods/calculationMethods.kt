package com.example.runadvisor.methods
import org.osmdroid.util.GeoPoint
import java.lang.Math.pow
import kotlin.math.*


fun calculateTrackLength(p:ArrayList<GeoPoint>):Double{
    var i = 1
    var l = 0.0
    while(i<p.size){
        l+= latLonToMeter(p[i-1],p[i])
        i++
    }
    return l
}

fun latLonToMeter(p1:GeoPoint,p2:GeoPoint):Double{
    val r = 6371e3
    val d1 = p1.latitude * Math.PI/180
    val d2 = p2.latitude * Math.PI/180
    val z1 = p1.altitude
    val z2 = p2.altitude
    val dlat = (p2.latitude-p1.latitude) * Math.PI/180
    val dlon = (p2.longitude-p1.longitude) * Math.PI/180

    val a = (sin(dlat/2) * sin(dlat/2) +
            cos(d1) * cos(d2) *
            sin(dlon/2) * sin(dlon/2))
    val c = 2 * atan2(sqrt(a),sqrt(1-a))

    val d = pow(r*c,2.0)
    val e = pow(abs(z1-z2),2.0)
    //return r*c
    return sqrt(d+e)
}



fun getGeoMiddle(p1:GeoPoint,p2:GeoPoint):GeoPoint{
    val dlon = Math.toRadians(p2.longitude-p1.longitude)

    val lat1 = Math.toRadians(p1.latitude)
    val lat2 = Math.toRadians(p2.latitude)
    val lon1 = Math.toRadians(p1.longitude)

    val bx = Math.cos(lat2) * Math.cos(dlon)
    val by = Math.cos(lat2) * Math.sin(dlon)
    val clat = Math.toDegrees(Math.atan2(
        Math.sin(lat1) + Math.sin(lat2),
        Math.sqrt((Math.cos(lat1) + bx) * (Math.cos(lat1) + bx) + by * by)
    ))
    val clon = Math.toDegrees(lon1 + Math.atan2(by, Math.cos(lat1) + bx))
    return GeoPoint(clat,clon)
}