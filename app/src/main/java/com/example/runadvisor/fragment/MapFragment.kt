package com.example.runadvisor.fragment
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.runadvisor.R
import com.example.runadvisor.databinding.FragmentMapBinding
import com.example.runadvisor.enums.FragmentInstance
import com.example.runadvisor.interfaces.IFragment
import com.example.runadvisor.io.printMotionEvent
import com.example.runadvisor.io.printToTerminal
import com.example.runadvisor.methods.getTitleBarHeight
import com.example.runadvisor.methods.hideKeyboard
import com.example.runadvisor.methods.selectImageFromGallery
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.OverlayItem
import org.osmdroid.views.overlay.Polyline

class MapFragment(private var showMenu:Boolean=false) : Fragment(R.layout.fragment_map), MapEventsReceiver, LocationListener, IFragment {
    private lateinit var mapView: MapView
    private lateinit var locationManager: LocationManager
    private lateinit var activityContext: Context
    private lateinit var parentActivity: Activity
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private val LOCATION_PERMISSION_CODE = 2
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
        //drawLineOnMap()
        getLocation()
        activateMenu()
        setEventListener(view)
        //setButtons()
        //setFragmentID()
        return view
    }

    override fun getFragmentID(): FragmentInstance {return FragmentInstance.FRAGMENT_MAP}

    override fun processWork(parameter: Any?){}

    override fun callbackDispatchTouchEvent(event: MotionEvent) {
        //printMotionEvent(event)
        //addMarker(getLatLon(event.rawX,event.rawY-parentActivity.getTitleBarHeight()))
    }

    fun activateMenu(){
        if(showMenu){
            binding.popupBtn.visibility = View.VISIBLE
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setEventListener(view:View){
        binding.popupBtn.setOnClickListener{
            val popUpMenu = PopupMenu(parentActivity,binding.popupBtn)
            popUpMenu.menuInflater.inflate(R.menu.popup_menu,popUpMenu.menu)
            popUpMenu.setOnMenuItemClickListener{it: MenuItem ->
                when(it.itemId){
                    R.id.popupPath->printToTerminal("popupPath")
                    R.id.popupSearch->printToTerminal("popupSearch")
                    R.id.popupGps->printToTerminal("popupGps")
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
        //val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        locationManager = activityContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    private fun getLocation() {
        if(checkGpsStatus()){
            if(ContextCompat.checkSelfPermission(activityContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(parentActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_CODE)
            }
            else{
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
                //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, this)
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        printToTerminal("Latitude: " + location.latitude + " , Longitude: " + location.longitude)
    }

    private fun checkGpsStatus():Boolean{
        //val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        /*if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            printToTerminal("Gps Is Enabled")
        }
        else{
            printToTerminal("Gps Is Disabled")
        }
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)*/
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

    /*override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        super.dispatchTouchEvent(event)
        when(event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                //addMarker(getLatLon(event.rawX,event.rawY-removeTitleBarHeight()))
                //printToTerminal("${getLatLon(event.x,event.y)}")
            }
            MotionEvent.ACTION_MOVE -> {}
            MotionEvent.ACTION_UP -> {}
            /*
            MotionEvent.ACTION_POINTER_DOWN -> {}
            MotionEvent.ACTION_POINTER_UP -> {}
            MotionEvent.ACTION_CANCEL -> {}
            MotionEvent.ACTION_CANCEL -> {}
            */
        }
        return true
    }*/

    private fun addMarker(p: GeoPoint){
        //printToTerminal("${p.latitude} ${p.longitude}")
        val marker = OverlayItem("","",p)
        val markers = ArrayList<OverlayItem>()
        marker.setMarker(AppCompatResources.getDrawable(activityContext,org.osmdroid.wms.R.drawable.marker_default))
        markers.add(marker)
        val items = ItemizedIconOverlay(activityContext,markers,null)
        mapView.overlays.add(items)
        mapView.invalidate()
    }

    private fun drawLineOnMap(){
        val points = ArrayList<GeoPoint>()
        val line: Polyline = Polyline(mapView)
        points.add(GeoPoint(59.37854636091821,13.48925862947408))
        points.add(GeoPoint(59.3785874631917,13.489140279120193))
        points.add(GeoPoint(59.37855321130057,13.489005790081734))
        points.add(GeoPoint(59.378458675901584,13.488941235343248))
        points.add(GeoPoint(59.37827097440079,13.488820195208632))
        points.add(GeoPoint(59.37816273729669,13.488715293758617))
        points.add(GeoPoint(59.37811615413291,13.48859694340473))
        points.add(GeoPoint(59.37803531849096,13.48837100182007))
        points.add(GeoPoint(59.377975034157984,13.488212304754654))
        points.add(GeoPoint(59.37790104869377,13.48814506023541))
        points.add(GeoPoint(59.37739547703529,13.488158509139282))
        points.add(GeoPoint(59.377240652770716,13.488064366812324))
        points.add(GeoPoint(59.37648707324941,13.487243983677558))
        points.add(GeoPoint(59.37611027720979,13.486832447219825))
        points.add(GeoPoint(59.37598970159303,13.487268191704516))
        points.add(GeoPoint(59.37600340338926,13.48750758219299))
        points.add(GeoPoint(59.37604998945499,13.487851874131508))
        points.add(GeoPoint(59.37610342633397,13.488198855850811))
        points.add(GeoPoint(59.37612397895731,13.488411348531628))
        points.add(GeoPoint(59.376554211013456,13.488887439727847))
        points.add(GeoPoint(59.37681728015519,13.489167176927907))
        points.add(GeoPoint(59.37695155424308,13.489379669608724))
        points.add(GeoPoint(59.37703239246818,13.489565264481826))
        points.add(GeoPoint(59.3772886072645,13.489656717027998))
        points.add(GeoPoint(59.37740232765013,13.489457673251053))
        points.add(GeoPoint(59.37790104869377,13.489325873993323))
        points.add(GeoPoint(59.37852580976278,13.489272078377951))
        line.setPoints(points)
        line.infoWindow = null
        line.color = (Color.rgb(0,191,255))
        mapView.overlays.add(mapView.overlays.size,line)
        mapView.invalidate()
    }

    private fun getLatLon(x:Float,y:Float): GeoPoint {
        val proj = mapView.projection
        //printToTerminal("X:$x Y:$y")
        return GeoPoint(proj.fromPixels(x.toInt(),y.toInt()))
    }

    private fun setLocation(){
        val lat = 59.377172
        val lon = 13.489016
        val mapController = mapView.controller
        mapController.setZoom(20)
        val startPoint = GeoPoint(lat,lon)
        mapController.setCenter(startPoint)

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
        super.onDestroyView()
        _binding = null
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val permissionsToRequest = ArrayList<String>()
        var i = 0
        while (i < grantResults.size) {
            permissionsToRequest.add(permissions[i])
            i++
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                parentActivity,
                permissionsToRequest.toTypedArray(),
                REQUEST_PERMISSIONS_REQUEST_CODE)
        }
    }

    /*override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }*/

}