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
import com.example.runadvisor.BuildConfig
import com.example.runadvisor.MainActivity
import com.example.runadvisor.R
import com.example.runadvisor.databinding.FragmentMapBinding
import com.example.runadvisor.interfaces.IFragment
import com.example.runadvisor.methods.*
import com.example.runadvisor.struct.*
import com.example.runadvisor.widget.GpsBlinker
import org.osmdroid.api.IGeoPoint
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView


abstract class MapFragment : Fragment(R.layout.fragment_map), MapEventsReceiver, LocationListener, IFragment {
    private lateinit var locationManager: LocationManager
    private lateinit var messageToUser: MessageToUser
    protected lateinit var activityContext: Context
    protected lateinit var parentActivity: MainActivity
    protected lateinit var mapView: MapView
    protected var progressBar: ProgressBar? = null
    protected var gpsBlinker: GpsBlinker? = null
    private var mapData = MapData()
    private var _binding: FragmentMapBinding? = null
    protected val binding get() = _binding!!

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addGpsBlinker()
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
        gpsBlinker!!.setAnimation()
    }

    protected fun activateGps(){
        if(gpsBlinker!!.isNotActive()){
            if(getLocationUpdates(this)){
                updateGpsPosition(getUserLocation())
                gpsBlinker!!.startBlinking()
            }
        }
        else{ deActivateGps() }
    }

    protected fun deActivateGps(){
       gpsBlinker!!.stopBlinking()
       cancelLocationUpdates(this)
    }

    protected fun updateGpsPosition(location: IGeoPoint){
        val p = mapView.projection.toPixels(location,null)
        gpsBlinker!!.setPosition(p.x.toFloat(),p.y.toFloat())
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
        mapData.zoom = mapView.zoomLevel
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
        //var location = getUserLocation()
        val location = getCenterOfHome()
        mapView.controller.setZoom(17)
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
        val lon: Double = (bbox.lonEast + ((bbox.lonWest-bbox.lonEast)/2))
        val lat = centerY

        mapView.controller.setZoom(zoom)
        mapView.controller.setCenter(GeoPoint(lat,lon))

    }

    private fun outsideMap(g:GeoPoint):Boolean{
        val b = mapView.boundingBox
        return ((g.longitude < b.lonWest || g.longitude > b.lonEast) ||
           (g.latitude < b.latSouth || g.latitude > b.latNorth))
    }

    open fun resetLastPosition(){
        if(mapData.geoPoint == null){setUserLocationIfAllowed()}
        else{
            mapView.controller.setZoom(mapData.zoom)
            mapView.controller.setCenter(mapData.geoPoint)
        }
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
        resetLastPosition()
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