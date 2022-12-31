package com.example.runadvisor.fragment
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.runadvisor.BuildConfig
import com.example.runadvisor.MainActivity
import com.example.runadvisor.R
import com.example.runadvisor.databinding.FragmentMapBinding
import com.example.runadvisor.enums.ServerResult
import com.example.runadvisor.interfaces.IFragment
import com.example.runadvisor.io.printToTerminal
import com.example.runadvisor.methods.*
import com.example.runadvisor.struct.*
import com.example.runadvisor.widget.GpsBlinker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.api.IGeoPoint
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import kotlin.concurrent.thread


abstract class MapFragment : Fragment(R.layout.fragment_map), MapEventsReceiver, LocationListener, IFragment {
    private lateinit var locationManager: LocationManager
    private lateinit var messageToUser: MessageToUser
    private lateinit var gpsBlinker: GpsBlinker
    protected lateinit var activityContext: Context
    protected lateinit var parentActivity: MainActivity
    protected lateinit var mapView: MapView
    protected var progressBar: ProgressBar? = null
    protected var mapData = MapData()
    private var _binding: FragmentMapBinding? = null
    protected val binding get() = _binding!!

    protected val MIN_GEO_POINTS:Int = 5

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentMapBinding.inflate(inflater,container,false)
        val view = binding.root
        setParentActivity()
        setActivityContext()
        setLocationManager()
        setMapView()
        setUserAgent()
        setInfoToUser()
        return view
    }

    override fun needDispatch():Boolean{return true}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addGpsBlinker()
    }

    override fun callbackDispatchTouchEvent(parameter:Any?) {
        gpsBlinker.resetPosition(mapView)
   }

    override fun longPressHelper(p: GeoPoint?): Boolean {
        return this.longPressHelper(p)
    }

    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
        return this.singleTapConfirmedHelper(p)
    }

    protected fun addProgressBar(){
        val layout = parentActivity.findViewById<RelativeLayout>(R.id.mapBaseLayout)
        progressBar = getProgressbar(parentActivity,layout)
    }

    private fun addGpsBlinker(){
        val layout = parentActivity.findViewById<RelativeLayout>(R.id.mapBaseLayout)
        gpsBlinker = getGpsBlinker(parentActivity,layout)
        gpsBlinker.setAnimation()
    }

    protected fun setGpsToStorePoints(callbackUpdateTrackLength:(args:String)->Unit){
        gpsBlinker.shouldStorePoints(true)
        gpsBlinker.clearCollectedPoints()
        gpsBlinker.setCallbackUpdateLength(callbackUpdateTrackLength)
        if(gpsBlinker.isNotActive()){activateGps()}
    }

    protected fun activateGps(){
        if(gpsBlinker.isNotActive()){
            if(getLocationUpdates(this)){
                val geoPoint = getUserLocation()
                gpsBlinker.startBlinking()
                mapView.controller.setCenter(geoPoint)
                updateGpsPosition(getUserLocation())
            }
        }
        else{
            deActivateGps()
        }
    }

    protected fun setZoom(zoom:Double){
        mapView.controller.setZoom(zoom)
    }

    protected fun deActivateGps(){
       gpsBlinker.stopBlinking()
       cancelLocationUpdates(this)
    }

    protected fun getCollectedGpsPoints():List<GeoPoint>{
        return gpsBlinker.getGeoPoints()
    }

    private fun updateGpsPosition(location: IGeoPoint){
        val p = mapView.projection.toPixels(location,null)
        gpsBlinker.setPosition(p.x.toFloat(),p.y.toFloat())
        gpsBlinker.collectPoints(location as GeoPoint)
    }

    protected fun takeMeAroundGoogle(){
        setZoom(15.0)
        gpsBlinker.resetPosition(mapView)
        viewLifecycleOwner.lifecycleScope.launch{
            withContext(Dispatchers.IO){
                var i = 0
                while(i<gpsBlinker.roundTripInCalifornia.size){
                    val loc = gpsBlinker.roundTripInCalifornia[i]
                    thread{
                        Thread.sleep(500)
                        Thread.currentThread().apply{
                            parentActivity.runOnUiThread(java.lang.Runnable{
                                if(outsideMap(loc)){mapView.controller.setCenter(loc)}
                                updateGpsPosition(loc)
                            })
                        }
                        i++
                    }.join()
               }
            }
        }
    }

    protected fun showProgressbar(show:Boolean){
        if(show){progressBar!!.visibility = VISIBLE}
        else{progressBar!!.visibility = GONE}
    }

    private fun setActivityContext(){activityContext = requireContext()}

    private fun setParentActivity(){parentActivity = requireActivity() as MainActivity}

    private fun setLocationManager(){locationManager = activityContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager}

    private fun setUserAgent(){Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID}

    private fun setInfoToUser(){
        messageToUser = MessageToUser(parentActivity,null)
    }

    private fun setMapView(){
        mapView = binding.map
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.setBuiltInZoomControls(false)
    }

    private fun setMapData(){
        mapData.geoPoint = mapView.mapCenter as GeoPoint
        mapData.zoom = mapView.projection.zoomLevel
        mapData.gpsWasActive = gpsBlinker.isActive()
    }

    /*
    *   ##########################################################################
    *               GPS SPECIFIC FUNCTIONS
    *   ##########################################################################
    * */

    override fun onLocationChanged(location: Location) {
        val geoPoint = GeoPoint(location.latitude,location.longitude)
        if(outsideMap(geoPoint)){mapView.controller.setCenter(geoPoint)}
        updateGpsPosition(geoPoint)
        //printToTerminal("OnLocationChanged line 140 MapFragment Latitude: " + location.latitude + " , Longitude: " + location.longitude)
    }

    private fun setUserLocationIfAllowed(){
        var location = getUserLocation()
        mapView.controller.setZoom(17.0)
        mapView.controller.setCenter(location)

    }

    protected fun zoomToArea(bbox:BoundingBox,paddingFactor:Double=1.0){
        val ry1: Double = Math.log((Math.sin(toRadians(bbox.latSouth)) + 1) / Math.cos(toRadians(bbox.latSouth)))
        val ry2: Double = Math.log(((Math.sin(toRadians(bbox.latNorth)) + 1) / Math.cos(toRadians(bbox.latNorth))))

        val ryc = (ry1 + ry2) / 2

        val centerY: Double = toDegrees(Math.atan(Math.sinh(ryc)))

        val resolutionHorizontal: Double = (bbox.lonEast/bbox.lonWest) / getScreenWidth()

        val vy0: Double = Math.log(Math.tan(Math.PI * (0.25 + centerY / 360)))
        val vy1: Double = Math.log(Math.tan(Math.PI * (0.25 + bbox.latNorth / 360)))
        val viewHeightHalf: Double = getScreenHeight() / 2.0
        val zoomFactorPowered = (viewHeightHalf / (40.7436654315252 * (vy1 - vy0)))
        val resolutionVertical = 360.0 / (zoomFactorPowered * 256)

        val resolution: Double = (Math.max(resolutionHorizontal, resolutionVertical) * paddingFactor)
        val zoom: Double = Math.log(360 / (resolution * 256))
        val lon: Double = (bbox.lonWest + ((bbox.lonWest-bbox.lonEast)/2))
        val lat = centerY

        mapView.controller.setZoom(zoom)
        mapView.controller.setCenter(GeoPoint(lat,lon))

    }

    private fun outsideMap(g:GeoPoint):Boolean{
        val b = mapView.boundingBox
        return ((g.longitude < b.lonWest || g.longitude > b.lonEast) ||
           (g.latitude < b.latSouth || g.latitude > b.latNorth))
    }

    protected open fun setMapPosition(){
        if(mapData.geoPoint == null){setUserLocationIfAllowed()}
        else{resetLastPosition()}
    }

    protected open fun resetLastPosition(){
        if(mapData.gpsWasActive){activateGps()}
        else{mapView.controller.setCenter(mapData.geoPoint)}
        mapView.controller.setZoom(mapData.zoom)
    }

    protected fun getLatLon(x:Float,y:Float): GeoPoint {
        val proj = mapView.projection
        //printToTerminal("X:$x Y:$y")
        return GeoPoint(proj.fromPixels(x.toInt(),y.toInt()))
    }

    /*
    *   ##########################################################################
    *               SHOW MESSAGE TO USER
    *   ##########################################################################
    * */

    protected fun showUserMessage(msg:String){
        messageToUser.setMessage(msg)
        messageToUser.showMessage()
    }

    /*
    *   ##########################################################################
    *                                ON VIEW CHANGED
    *   ##########################################################################
    *
    * */

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        setMapPosition()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
        setMapData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        deActivateGps()
        _binding = null
    }

}