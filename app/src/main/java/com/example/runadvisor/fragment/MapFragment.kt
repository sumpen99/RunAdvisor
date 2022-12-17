package com.example.runadvisor.fragment
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.*
import android.view.View.VISIBLE
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.runadvisor.BuildConfig
import com.example.runadvisor.R
import com.example.runadvisor.databinding.FragmentMapBinding
import com.example.runadvisor.enums.FragmentInstance
import com.example.runadvisor.enums.MenuType
import com.example.runadvisor.interfaces.IFragment
import com.example.runadvisor.io.printToTerminal
import com.example.runadvisor.methods.clearChildren
import com.example.runadvisor.struct.MapData
import com.example.runadvisor.struct.MapPath
import com.example.runadvisor.widget.TrackMenuBar
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView


class MapFragment(private val removable:Boolean,private var menuType:MenuType,private val fragmentId:FragmentInstance) : Fragment(R.layout.fragment_map), MapEventsReceiver, LocationListener, IFragment {
    private lateinit var mapView: MapView
    private lateinit var locationManager: LocationManager
    private var mapPath:MapPath? = null
    private lateinit var activityContext: Context
    private lateinit var parentActivity: Activity
    private  var mapData:MapData? = null
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentMapBinding.inflate(inflater,container,false)
        val view: View = binding.root
        setParentActivity()
        setActivityContext()
        setLocationManager()
        setMapView()
        setLocation()
        getLocation()
        setEventListener(view)
        setUserAgent()
        return view
    }

    override fun isRemovable():Boolean{
        return removable
    }

    override fun getFragmentID(): FragmentInstance {
        return fragmentId
    }

    override fun processWork(parameter: Any?){}

    override fun callbackDispatchTouchEvent(event: MotionEvent) {
        //printMotionEvent(event)
        //addMarker(getLatLon(event.rawX,event.rawY-parentActivity.getTitleBarHeight()))
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setEventListener(view:View){
        if(menuType == MenuType.MENU_ADD_PATH){setMenuAddPath()}
        if(menuType == MenuType.MENU_BASE){setMenuBase()}
    }

    private fun setUserAgent(){
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
    }

    private fun setMenuAddPath(){
        binding.popupBtn.setOnClickListener{
            val popUpMenu =  PopupMenu(parentActivity,binding.popupBtn)
            popUpMenu.menuInflater.inflate(R.menu.popup_menu_add_path,popUpMenu.menu)
            popUpMenu.setOnMenuItemClickListener{it: MenuItem ->
                when(it.itemId){
                    R.id.popupAdd->addBottomTrackMenu()
                    R.id.popupSave->printToTerminal("popupSave")
                    R.id.popupExit->printToTerminal("popupExit")
                }
                true
            }
            popUpMenu.show()
        }
    }

    private fun setMenuBase(){
        binding.popupBtn.setOnClickListener{
            val popUpMenu =  PopupMenu(parentActivity,binding.popupBtn)
            popUpMenu.menuInflater.inflate(R.menu.popup_menu_base,popUpMenu.menu)
            popUpMenu.setOnMenuItemClickListener{it: MenuItem ->
                when(it.itemId){
                    R.id.popupGps->printToTerminal("popupGps")
                    R.id.popupSearch->printToTerminal("popupSearch")
                }
                true
            }
            popUpMenu.show()
        }
    }

    private fun setActivityContext(){
        activityContext = requireContext()
    }

    private fun setParentActivity(){
        parentActivity = requireActivity()
    }

    private fun setLocationManager(){
        locationManager = activityContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    private fun getLocation() {
        if(checkGpsProviderStatus()){
            if(ContextCompat.checkSelfPermission(activityContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        //printToTerminal("OnLocationChanged line 140 MapFragment Latitude: " + location.latitude + " , Longitude: " + location.longitude)
    }

    private fun checkGpsProviderStatus():Boolean{
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun setMapView(){
        mapView = binding.map
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.setBuiltInZoomControls(false)
    }

    override fun longPressHelper(p: GeoPoint?): Boolean {
        return this.longPressHelper(p)
    }

    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
        return this.singleTapConfirmedHelper(p)
    }

    private fun addBottomTrackMenu(){
        binding.bottomMenuLayout.clearChildren(0)
        binding.bottomMenuLayout.visibility = VISIBLE
        val trackMenu = TrackMenuBar(
            parentActivity,
            null
        )
        binding.bottomMenuLayout.addView(TrackMenuBar(parentActivity,null))
        trackMenu.setEventListener(::adjustPointLasso,::adjustPointLasso,::adjustPointLasso)
    }

    private fun adjustPointLasso(parameter:Any?){
        val numPoints:Int = parameter as Int
        if(mapPath == null){
            if(numPoints <= 0){return}
            mapPath = MapPath(activityContext,mapView)
            mapPath!!.setLasso(numPoints)
        }
        else{
            printToTerminal("$numPoints")
            if(!mapPath!!.adjustLasso(numPoints)){return}
            mapPath!!.removeOverlayFromMap()
            mapPath!!.buildPolyLine()
        }
        mapPath!!.addLassoOverlay()
    }

    private fun getLatLon(x:Float,y:Float): GeoPoint {
        val proj = mapView.projection
        //printToTerminal("X:$x Y:$y")
        return GeoPoint(proj.fromPixels(x.toInt(),y.toInt()))
    }

    private fun setLocation(){
        val mapController = mapView.controller
        if(mapData!=null){
            mapController.setZoom(mapData!!.zoom)
            mapController.setCenter(mapData!!.geoPoint)
            return
        }

        val lat = 59.377172
        val lon = 13.489016
        mapController.setZoom(19)
        val startPoint = GeoPoint(lat,lon)
        mapController.setCenter(startPoint)

    }

    private fun setMapData(){
        mapData = MapData()
        mapData!!.geoPoint = mapView.mapCenter as GeoPoint
        mapData!!.zoom = mapView.zoomLevel
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroyView() {
        setMapData()
        super.onDestroyView()
        _binding = null
    }

}