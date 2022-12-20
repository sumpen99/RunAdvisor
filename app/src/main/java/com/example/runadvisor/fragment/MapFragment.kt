package com.example.runadvisor.fragment
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.drawToBitmap
import androidx.fragment.app.Fragment
import com.example.runadvisor.BuildConfig
import com.example.runadvisor.R
import com.example.runadvisor.activity.HomeActivity
import com.example.runadvisor.databinding.FragmentMapBinding
import com.example.runadvisor.enums.FragmentInstance
import com.example.runadvisor.enums.MenuType
import com.example.runadvisor.interfaces.IFragment
import com.example.runadvisor.io.printToTerminal
import com.example.runadvisor.methods.clearChildren
import com.example.runadvisor.methods.showMessage
import com.example.runadvisor.methods.toMap
import com.example.runadvisor.struct.MapData
import com.example.runadvisor.struct.MapPath
import com.example.runadvisor.widget.TrackMenuBar
import com.squareup.okhttp.OkHttpClient
import io.grpc.internal.JsonParser
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import java.net.URL
import kotlin.concurrent.thread


class MapFragment(private val removable:Boolean,private var menuType:MenuType,private val fragmentId:FragmentInstance) : Fragment(R.layout.fragment_map), MapEventsReceiver, LocationListener, IFragment {
    private lateinit var mapView: MapView
    private lateinit var locationManager: LocationManager
    private var mapPath:MapPath? = null
    private var trackMenu:TrackMenuBar?=null
    private lateinit var activityContext: Context
    private lateinit var parentActivity: Activity
    private var urlCallInProgress:Boolean = false
    private var mapData:MapData? = null
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private val URL_TIMER:Long = 1000
    private var lastUrlCall:Long = 0

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
        setMenuType()
        setUserAgent()
        return view
    }

    override fun longPressHelper(p: GeoPoint?): Boolean {
        return this.longPressHelper(p)
    }

    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
        return this.singleTapConfirmedHelper(p)
    }

    override fun isRemovable():Boolean{return removable}

    override fun getFragmentID(): FragmentInstance {return fragmentId}

    override fun receivedData(parameter: Any?){}

    override fun callbackDispatchTouchEvent(event: MotionEvent) {}

    private fun setActivityContext(){activityContext = requireContext()}

    private fun setParentActivity(){parentActivity = requireActivity()}

    private fun setLocationManager(){locationManager = activityContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager}

    private fun setUserAgent(){Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID}

    private fun setUrlCallInProgress(value:Boolean){urlCallInProgress = value}

    private fun setMapView(){
        mapView = binding.map
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.setBuiltInZoomControls(false)
    }

    private fun setMapData(){
        mapData = MapData()
        mapData!!.geoPoint = mapView.mapCenter as GeoPoint
        mapData!!.zoom = mapView.zoomLevel
    }

    /*
    *   ##########################################################################
    *               POPUP BOTTOM MENU
    *   ##########################################################################
    * */

    private fun setMenuType(){
        if(menuType == MenuType.MENU_TRACK){
            setTrackMenu()
        }
        if(menuType == MenuType.MENU_BASE){setMenuBase()}
    }

    private fun setTrackMenu(){
        binding.popupBtn.setOnClickListener{
            val popUpMenu =  PopupMenu(parentActivity,binding.popupBtn)
            popUpMenu.menuInflater.inflate(R.menu.popup_menu_add_path,popUpMenu.menu)
            popUpMenu.setOnMenuItemClickListener{it: MenuItem ->
                when(it.itemId){
                    R.id.popupAdd->addBottomMenu()
                    R.id.popupExit->exitBackToUpload()
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

    private fun addBottomMenu(){
        if(binding.bottomMenuLayout.visibility == VISIBLE){return}
        binding.bottomMenuLayout.visibility = VISIBLE
        binding.bottomMenuLayout.clearChildren(0)

        if(menuType== MenuType.MENU_TRACK){
            trackMenu = TrackMenuBar(parentActivity,null)
            binding.bottomMenuLayout.addView(trackMenu)
            trackMenu!!.setEventListener(
                ::adjustPointLasso,
                ::adjustPointLasso,
                ::saveTrack,
                ::clearMapPath)
        }
    }

    private fun exitBackToUpload(){
        if(mapPath!=null && mapPath!!.savedTracks.isNotEmpty()){
            (parentActivity as HomeActivity).pushDataToFragment(
                FragmentInstance.FRAGMENT_UPLOAD,
                mapPath!!.savedTracks)
            mapPath!!.removeOverlaysFromMap()
        }
        removeBottomMenu()
        (parentActivity as HomeActivity).navigateFragment(FragmentInstance.FRAGMENT_UPLOAD)
    }

    private fun removeBottomMenu(){
        binding.bottomMenuLayout.visibility = GONE
        binding.bottomMenuLayout.clearChildren(0)
    }

    /*
    *   ##########################################################################
    *               POPUP BOTTOM MENU FUNCTIONS
    *   ##########################################################################
    * */

    private fun adjustPointLasso(parameter:Any?){
        val numPoints:Int = parameter as Int
        if(mapPath == null){
            if(numPoints <= 0){return}
            mapPath = MapPath(activityContext,mapView,::updateTrackLength)
            mapPath!!.setLasso(numPoints)
        }
        else{
            if(!mapPath!!.adjustLasso(numPoints)){return}
            mapPath!!.removeOverlaysFromMap()
            mapPath!!.buildPolyLine()
        }
        mapPath!!.addLassoOverlay()
    }

    private fun saveTrack(parameter:Any?){
        if(mapPath!=null && mapPath!!.trackIsOnMap() && checkSearchTimer() && !urlCallInProgress){
            setUrlCallInProgress(true)
            setSearchTimer()
            getCurrentAddressInfo(mapPath!!.points[0])
        }
    }

    private fun clearMapPath(parameter: Any?){
        if(mapPath!=null){
            mapPath!!.resetMapPath()
            //mapPath = null
        }
    }

    private fun updateTrackLength(trackLength:String){
        trackMenu!!.setTrackLength(trackLength)
    }

    /*
    *   ##########################################################################
    *               GPS SPECIFIC FUNCTIONS
    *   ##########################################################################
    * */

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

    /*
    *   ##########################################################################
    *               GET ADDRESS FROM OSM SERVER
    *   ##########################################################################
    * */

    private fun getCurrentAddressInfo(geoPoint:GeoPoint){
        val requestString = "https://nominatim.openstreetmap.org/reverse?lat=" +
                geoPoint.latitude.toString() + "&lon=" + geoPoint.longitude.toString() + "&zoom=18&format=jsonv2"
        var city:Any? = null
        var street:Any? = null
        thread {
            /*val json =  try {URL(requestString).readText()}
                        catch (err: Exception){return@thread}
            try{
                val jsonObj = JSONObject(json)
                val parser = jsonObj.toMap()
                val address = parser.get("address") as Map<String,*>
                city = address.get("city")
                street = address.get("road")
            }
            catch(err:Exception){
                //this.parentActivity.runOnUiThread{parentActivity.showMessage(err.message.toString(),Toast.LENGTH_LONG)}
            }*/
            city = "Karlstad"
            street = "Muraregatan"
            this.parentActivity.runOnUiThread{
                onReceivedAddressInfo(city,street)
                setUrlCallInProgress(false)
            }
        }
    }

    private fun onReceivedAddressInfo(city:Any?,street:Any?){
        var msg = ""
        if(city == null || street == null){msg = "No Address Info..."}
        else{
            mapPath!!.removeOverlayMarkers()
            val bmp = mapView.drawToBitmap()
            val zoom = mapView.zoomLevel
            if(mapPath!!.saveCurrentTrack(bmp,zoom,city as String,street as String)){msg = "Track Saved!"}
            else{msg = "Error Occurred..."}
        }
        parentActivity.showMessage(msg,Toast.LENGTH_SHORT)
    }

    /*
    *   ##########################################################################
    *               RESPECT NOMINATIM 1/SEC POLICY
    *   ##########################################################################
    * */

    private fun checkSearchTimer():Boolean{
        if(System.currentTimeMillis()-lastUrlCall < URL_TIMER){
            parentActivity.showMessage("Please wait and try again...",Toast.LENGTH_SHORT)
            return false
        }
        return true
    }

    private fun setSearchTimer(){
        lastUrlCall = System.currentTimeMillis()
    }

    /*
    *   ##########################################################################
    *               ON START STOP RESUME
    *   ##########################################################################
    * */

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