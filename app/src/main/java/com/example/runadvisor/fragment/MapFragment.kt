package com.example.runadvisor.fragment
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.example.runadvisor.BuildConfig
import com.example.runadvisor.R
import com.example.runadvisor.activity.HomeActivity
import com.example.runadvisor.databinding.FragmentMapBinding
import com.example.runadvisor.interfaces.IFragment
import com.example.runadvisor.methods.*
import com.example.runadvisor.struct.*
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

abstract class MapFragment : Fragment(R.layout.fragment_map), MapEventsReceiver, LocationListener, IFragment {
    private lateinit var locationManager: LocationManager
    private lateinit var messageToUser: MessageToUser
    protected lateinit var activityContext: Context
    protected lateinit var parentActivity: HomeActivity
    protected lateinit var mapView: MapView
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


    override fun longPressHelper(p: GeoPoint?): Boolean {
        return this.longPressHelper(p)
    }

    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
        return this.singleTapConfirmedHelper(p)
    }

    private fun setActivityContext(){activityContext = requireContext()}

    private fun setParentActivity(){parentActivity = requireActivity() as HomeActivity}

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
        //printToTerminal("OnLocationChanged line 140 MapFragment Latitude: " + location.latitude + " , Longitude: " + location.longitude)
    }

    private fun setUserLocationIfAllowed(){
        //var location = getLocation()
        val location = getCenterOfHome()
        /*if(location == null){
            location = getCenterOfHome()
        }*/
        mapView.controller.setZoom(17)
        mapView.controller.setCenter(location)

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
        _binding = null
    }

}