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
import com.example.runadvisor.enums.FragmentInstance
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
    private lateinit var gpsBlinker: GpsBlinker
    protected lateinit var activityContext: Context
    protected lateinit var parentActivity: MainActivity
    protected lateinit var mapView: MapView
    protected var progressBar: ProgressBar? = null
    protected var mapData = MapData()
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

    override fun hasParentFragment(): FragmentInstance?{ return null }

    protected fun addProgressBar(){
        val layout = parentActivity.findViewById<RelativeLayout>(R.id.mapBaseLayout)
        progressBar = getRoundProgressbar(parentActivity,layout)
    }

    private fun addGpsBlinker(){
        val layout = parentActivity.findViewById<RelativeLayout>(R.id.mapBaseLayout)
        gpsBlinker = getGpsBlinker(parentActivity,layout)
        gpsBlinker.setAnimation()
    }

    protected fun showProgressbar(show:Boolean){
        if(show){progressBar!!.visibility = VISIBLE}
        else{progressBar!!.visibility = GONE}
    }

    private fun setActivityContext(){activityContext = requireContext()}

    private fun setParentActivity(){parentActivity = requireActivity() as MainActivity}

    private fun setLocationManager(){locationManager = activityContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager}

    private fun setUserAgent(){Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID}

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

    protected fun setZoom(zoom:Double){
        mapView.controller.setZoom(zoom)
    }

    protected fun setGeoPosition(geoPoint:GeoPoint){
        mapView.controller.setCenter(geoPoint)
    }

    /*
    *   ##########################################################################
    *               GPS BLINKER
    *   ##########################################################################
    * */

    protected fun activateGps(){
        if(getLocationUpdates(this)){
            val geoPoint = getUserLocation()
            zoomToPosition(geoPoint,18.0)
            updateGpsPosition(geoPoint)
            gpsBlinker.startBlinking()
        }
    }

    protected fun deActivateGps(){
        gpsBlinker.stopBlinking()
        cancelLocationUpdates(this)
    }

    protected fun resetGpsBlinker(){
        gpsBlinker.resetAndClear()
    }

    protected  fun refreshGpsBlinkerMeasureLength(){
        gpsBlinker.refreshMeasuredLength()
    }

    protected fun gpsBlinkerIsActive():Boolean{
        return gpsBlinker.isActive()
    }

    protected fun gpsBlinkerIsCollecting():Boolean{
        return gpsBlinker.isCollecting()
    }

    protected fun setGpsToStorePoints(callbackUpdateTrackLength:(args:String)->Unit){
        gpsBlinker.shouldStorePoints(true)
        gpsBlinker.setCallbackUpdateLength(callbackUpdateTrackLength)
    }

    protected fun getCollectedGpsPoints():List<GeoPoint>{
        return gpsBlinker.getGeoPoints()
    }

    protected fun getGpsMeasuredLength():Double{
        return gpsBlinker.getMeasuredLength()
    }

    private fun updateGpsPosition(location: IGeoPoint){
        val p = mapView.projection.toPixels(location,null)
        gpsBlinker.setPosition(p.x.toFloat(),p.y.toFloat())
        gpsBlinker.collectPoints(location as GeoPoint)
    }

    /*
    *   ##########################################################################
    *               GPS FUNCTIONS
    *   ##########################################################################
    * */

    override fun onLocationChanged(location: Location) {
        val geoPoint = GeoPoint(location.latitude,location.longitude)
        if(outsideMap(geoPoint)){mapView.controller.setCenter(geoPoint)}
        updateGpsPosition(geoPoint)
    }

    private fun setUserLocationIfAllowed(){
        val location = getUserLocation()
        zoomToPosition(location,getZoomBase())
    }

    protected fun zoomToArea(bbox:BoundingBox){
        zoomToPosition(getAreaGeoMiddle(bbox),getAreaZoomLevel(bbox))
    }

    protected fun zoomToPosition(geoPoint:GeoPoint,zoom:Double){
        mapView.controller.setZoom(zoom)
        mapView.controller.setCenter(geoPoint)
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