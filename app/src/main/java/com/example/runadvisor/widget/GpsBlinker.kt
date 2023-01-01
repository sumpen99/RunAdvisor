package com.example.runadvisor.widget
import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import com.example.runadvisor.MainActivity
import com.example.runadvisor.R
import com.example.runadvisor.methods.*
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

class GpsBlinker(context:Context,
                 attrs: AttributeSet?=null,
                 defStyleAttr:Int=0):LinearLayout(context, attrs,defStyleAttr){
    private lateinit var imgView:ImageView
    private lateinit var frameAnimation:AnimationDrawable
    private var callbackUpdateLength:(args:String)->Unit = ::templateFunctionString
    private var geoPoints = ArrayList<GeoPoint>()
    private var lastGeoPoint:GeoPoint? = null
    private var measuredLength:Double = 0.0
    private var active:Boolean = false
    private var storePoints:Boolean = false
    private val offsetY:Int = convertDpToPixel(25)
    private val offsetX:Int = convertDpToPixel(25)

    var roundTripInCalifornia = listOf<GeoPoint>(
        GeoPoint(37.421998,-122.084000),
        GeoPoint(37.421951,-122.083146),
        GeoPoint(37.421781,-122.082546),
        GeoPoint(37.420826,-122.082546),
        GeoPoint(37.420486,-122.077997),
        GeoPoint(37.418202,-122.078039),
        GeoPoint(37.416464,-122.078125),
        GeoPoint(37.414725,-122.078039),
        GeoPoint(37.413157,-122.078125),
        GeoPoint(37.410771,-122.078125),
        GeoPoint(37.409237,-122.078168),
        GeoPoint(37.407942,-122.078125),
        GeoPoint(37.406510,-122.078082),
        GeoPoint(37.405385,-122.078125),
        GeoPoint(37.404362,-122.078211),
        GeoPoint(37.403408,-122.078168),
        GeoPoint(37.402147,-122.078383),
        GeoPoint(37.401362,-122.079155),
        GeoPoint(37.400646,-122.080185),
        GeoPoint(37.399487,-122.080958),
        GeoPoint(37.397510,-122.081816),
        GeoPoint(37.396214,-122.082674),
        GeoPoint(37.394885,-122.083790),
        GeoPoint(37.393180,-122.084777),
        GeoPoint(37.393044,-122.084820),
        GeoPoint(37.393248,-122.085593),
        GeoPoint(37.394339,-122.088339),
        GeoPoint(37.395089,-122.090356),
        GeoPoint(37.396248,-122.093103),
        GeoPoint(37.397305,-122.095806),
        GeoPoint(37.398158,-122.097823),
        GeoPoint(37.398737,-122.099411),
        GeoPoint(37.398942,-122.099969),
        GeoPoint(37.399965,-122.099325),
        GeoPoint(37.401294,-122.098553),
        GeoPoint(37.403169,-122.097351),
        GeoPoint(37.404737,-122.096493),
        GeoPoint(37.406101,-122.095635),
        GeoPoint(37.407737,-122.094734),
        GeoPoint(37.409033,-122.093961),
        GeoPoint(37.410464,-122.093189),
        GeoPoint(37.411487,-122.092631),
        GeoPoint(37.412578,-122.092502),
        GeoPoint(37.413669,-122.092674),
        GeoPoint(37.414521,-122.092717),
        GeoPoint(37.414344,-122.089308),
        GeoPoint(37.415190,-122.089461),
        GeoPoint(37.415769,-122.089525),
        GeoPoint(37.415684,-122.088044),
        GeoPoint(37.415633,-122.086993),
        GeoPoint(37.415650,-122.086671),
        GeoPoint(37.416519,-122.086650),
        GeoPoint(37.417263,-122.086630),
        GeoPoint(37.417211,-122.086244),
        GeoPoint(37.417041,-122.085837),
        GeoPoint(37.416905,-122.085686),
        GeoPoint(37.416785,-122.085343),
        GeoPoint(37.416802,-122.084678),
        GeoPoint(37.416711,-122.082964),
        GeoPoint(37.417603,-122.082912),
        GeoPoint(37.418668,-122.082933),
        GeoPoint(37.419528,-122.082912),
        GeoPoint(37.420329,-122.082912),
        GeoPoint(37.421028,-122.082890),
        GeoPoint(37.421352,-122.082858),
        GeoPoint(37.421607,-122.082708),
        GeoPoint(37.421786,-122.082858),
        GeoPoint(37.421863,-122.083362))



    init{
        inflate(context, R.layout.gps_blinker,this)
    }


    fun setCallbackUpdateLength(callback:(args:String)->Unit){
        callbackUpdateLength = callback
    }

    fun shouldStorePoints(value:Boolean){
        storePoints = value
    }

    fun clearCollectedPoints(){
        measuredLength = 0.0
        geoPoints.clear()
    }

    fun resetAndClear(){
        clearCollectedPoints()
        callbackUpdateLength(measuredLength.inKilometers())
    }

    fun getGeoPoints():List<GeoPoint>{
        return geoPoints
    }

    fun getMeasuredLength():Double{
        return measuredLength
    }

    fun collectPoints(geoPoint:GeoPoint){
        lastGeoPoint = geoPoint
        if(storePoints){
            if(geoPoints.size > 0){
                measuredLength+= latLonToMeter(geoPoint,geoPoints[geoPoints.size-1])
                callbackUpdateLength(measuredLength.inKilometers())
            }
            geoPoints.add(geoPoint)
        }
    }

    fun resetPosition(mapView:MapView){
        if(lastGeoPoint!=null){
            val p = mapView.projection.toPixels(lastGeoPoint,null)
            setPosition(p.x.toFloat(),p.y.toFloat())
        }
    }
    fun setPosition(x:Float,y:Float){
        this.x = x-offsetX
        this.y = y-offsetY
    }

    fun setAnimation(){
        imgView = (context as MainActivity).findViewById(R.id.gpsBlinkerImageView)
        frameAnimation = imgView.drawable as AnimationDrawable
    }

    fun startBlinking(){
        active = true
        visibility = VISIBLE
        frameAnimation.start()
    }

    fun stopBlinking(){
        active = false
        visibility = GONE
        frameAnimation.stop()
    }

    fun isNotActive():Boolean{
        return !active
    }

    fun isActive():Boolean{
        return active
    }

    fun isCollecting():Boolean{
        return storePoints
    }
}