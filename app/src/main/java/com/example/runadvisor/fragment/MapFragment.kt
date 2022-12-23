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
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.drawToBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.runadvisor.BuildConfig
import com.example.runadvisor.R
import com.example.runadvisor.activity.HomeActivity
import com.example.runadvisor.databinding.FragmentMapBinding
import com.example.runadvisor.enums.FragmentInstance
import com.example.runadvisor.enums.MenuType
import com.example.runadvisor.enums.ServerResult
import com.example.runadvisor.interfaces.IFragment
import com.example.runadvisor.io.printToTerminal
import com.example.runadvisor.methods.*
import com.example.runadvisor.struct.*
import com.example.runadvisor.widget.TrackMenuBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import java.net.URL
import kotlin.concurrent.thread


class MapFragment(private val removable:Boolean,private var menuType:MenuType,private val fragmentId:FragmentInstance) : Fragment(R.layout.fragment_map), MapEventsReceiver, LocationListener, IFragment {
    private lateinit var locationManager: LocationManager
    private lateinit var messageToUser: MessageToUser
    private lateinit var activityContext: Context
    private lateinit var parentActivity: Activity
    private lateinit var mapView: MapView
    private var mapBaseView:View? = null
    private var urlCallInProgress:Boolean = false
    private var progressBar:ProgressBar? = null
    private var trackMenu:TrackMenuBar?=null
    private var mapPath:MapPath? = null
    private var mapData:MapData? = null
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private val URL_TIMER:Long = 1000
    private var lastUrlCall:Long = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentMapBinding.inflate(inflater,container,false)
        if(mapBaseView!=null){return mapBaseView!!}
        mapBaseView = binding.root
        setParentActivity()
        setActivityContext()
        setLocationManager()
        setMapView()
        setLocation()
        getLocation()
        setMenuType()
        setUserAgent()
        setInfoToUser()
        return mapBaseView!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(progressBar == null){addProgressBar()}
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
        if(mapData == null){mapData = MapData()
        }
        mapData!!.geoPoint = mapView.mapCenter as GeoPoint
        mapData!!.zoom = mapView.zoomLevel
    }

    private fun addProgressBar(){
        val layout = parentActivity.findViewById<RelativeLayout>(R.id.mapBaseLayout)
        progressBar = getProgressbar(parentActivity,layout)
    }

    private fun setProgressbar(show:Boolean){
        if(show){progressBar!!.visibility = VISIBLE}
        else{progressBar!!.visibility = GONE}
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
                    R.id.popupDrawPoints->addBottomMenu()
                    R.id.popupGpsPoints->{}
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
        if(clearForUpload()){
            viewLifecycleOwner.lifecycleScope.launch{
                setProgressbar(true)
                val serverResult = ServerDetails()
                val addressInfo = AddressInfo()
                setUrlCallInProgress(true)
                setSearchTimer()
                getAddress2(addressInfo,serverResult)

                if(serverResult.serverResult != ServerResult.UPLOAD_ERROR){
                    if(saveTrackOnSuccess(addressInfo)){mapPath!!.removeOverlayMarkers()}
                    else{showUserMessage("UnExpected Error!")}
                }
                else{showUserMessage(serverResult.msg)}
                setProgressbar(false)
                setUrlCallInProgress(false)
            }
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
    *               GET ADDRESS FROM OSM SERVER AND SAVE
    *   ##########################################################################
    * */

    private suspend fun getAddress2(addressInfo:AddressInfo,serverResult:ServerDetails){
        withContext(Dispatchers.IO) {
            thread {
                serverResult.serverResult = ServerResult.UPLOAD_OK
                addressInfo.city = "Karlstad"
                addressInfo.street = "Muraregatan"
                Thread.sleep(2000)
            }.join()
        }
    }

    private suspend fun getAddress(addressInfo:AddressInfo,serverResult:ServerDetails){
        val geoPoint = mapPath!!.points[0]
        val requestString = "https://nominatim.openstreetmap.org/reverse?lat=" +
                geoPoint.latitude.toString() + "&lon=" + geoPoint.longitude.toString() + "&zoom=18&format=jsonv2"
        withContext(Dispatchers.IO) {
        thread {
                val json =  try {URL(requestString).readText()}
                catch (err: Exception){
                    serverResult.serverResult = ServerResult.UPLOAD_ERROR
                    serverResult.msg = err.message.toString()
                    return@thread
                }
                try{
                    val jsonObj = JSONObject(json)
                    val parser = jsonObj.toMap()
                    val address = parser.get("address") as Map<String,*>
                    addressInfo.city = address.get("city") as String
                    addressInfo.street = address.get("road") as String
                }
                catch(err:Exception){
                    serverResult.serverResult = ServerResult.UPLOAD_ERROR
                    serverResult.msg = err.message.toString()
            }}.join()
        }

        if(addressInfo.city.isEmpty()){addressInfo.city = "<Insert City>"}
        if(addressInfo.street.isEmpty()){addressInfo.street = "<Insert Street>"}
    }

    private fun saveTrackOnSuccess(addressInfo:AddressInfo):Boolean{
        val bmp = mapView.drawToBitmap()
        val zoom = mapView.zoomLevel
        return mapPath!!.saveCurrentTrack(bmp,zoom,addressInfo.city,addressInfo.street)
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
    *               GPS SPECIFIC FUNCTIONS
    *   ##########################################################################
    * */

    private fun getLocation():GeoPoint? {
        val location:Location?
        if(checkGpsProviderStatus() &&
            ContextCompat.checkSelfPermission(activityContext,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(activityContext,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            location =  locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if(location!=null){return GeoPoint(location.latitude,location.longitude)}
        }
        return null
    }

    private fun getLocationUpdates(){
        if(checkGpsProviderStatus() &&
            ContextCompat.checkSelfPermission(activityContext,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(activityContext,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
        }
    }

    override fun onLocationChanged(location: Location) {
        //printToTerminal("OnLocationChanged line 140 MapFragment Latitude: " + location.latitude + " , Longitude: " + location.longitude)
    }

    private fun checkGpsProviderStatus():Boolean{
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun setLocation(){
        val mapController = mapView.controller
        if(mapData!=null){
            mapController.setZoom(mapData!!.zoom)
            mapController.setCenter(mapData!!.geoPoint)
            return
        }
        var location = getLocation()
        location = getCenterOfHome()
        if(location == null){
            location = getCenterOfHome()
        }
        mapController.setZoom(17)
        mapController.setCenter(location)

    }

    /*
    *   ##########################################################################
    *               SOME USEFUL FUNCTIONS
    *   ##########################################################################
    * */

    private fun showUserMessage(msg:String){
        messageToUser.setMessage(msg)
        messageToUser.showMessage()
    }

    private fun clearForUpload():Boolean{
        return (mapPath!=null && mapPath!!.trackIsOnMap() && checkSearchTimer() && !urlCallInProgress)
    }

    private fun getLatLon(x:Float,y:Float): GeoPoint {
        val proj = mapView.projection
        //printToTerminal("X:$x Y:$y")
        return GeoPoint(proj.fromPixels(x.toInt(),y.toInt()))
    }
    /*
    *   ##########################################################################
    *               ON START STOP RESUME
    *   ##########################################################################
    * */

    override fun onResume() {
        super.onResume()
        setLocation()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        setMapData()
        mapView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}