package com.example.runadvisor.fragment
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.runadvisor.R
import com.example.runadvisor.enums.FragmentInstance
import com.example.runadvisor.enums.ServerResult
import com.example.runadvisor.map.MapBuildTrack
import com.example.runadvisor.methods.*
import com.example.runadvisor.struct.*
import com.example.runadvisor.widget.GpsMenuBar
import com.example.runadvisor.widget.TrackMenuBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.osmdroid.util.GeoPoint
import java.net.URL
import kotlin.concurrent.thread

class MapFragmentUpload:MapFragment() {
    private var urlCallInProgress:Boolean = false
    private var trackMenu:TrackMenuBar?=null
    private var gpsMenu: GpsMenuBar?=null
    private lateinit var askForGpsReset: MessageToUser
    private lateinit var mapBuildTrack: MapBuildTrack
    private var lastUrlCall:Long = 0
    private var lastMenu:Int = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTrackPathMenu()
        if(progressBar == null){addProgressBar()}
        initMapBuildTrack()
        setAskForGpsResetMessage()
    }

    override fun receivedData(parameter: Any?){}

    override fun isRemovable():Boolean{
        return !gpsBlinkerIsActive()
    }

    override fun hasParentFragment(): FragmentInstance?{
        if(gpsBlinkerIsActive()){parentActivity.showUserMessage("Gps Is Running");return null}
        return FragmentInstance.FRAGMENT_UPLOAD
    }

    override fun getFragmentID(): FragmentInstance { return FragmentInstance.FRAGMENT_MAP_TRACK_PATH }

    private fun setAskForGpsResetMessage(){
        askForGpsReset = MessageToUser(parentActivity,null)
        askForGpsReset.setMessage("Do You Want To Start Over?")
        askForGpsReset.setTwoButtons()
        askForGpsReset.setPositiveCallback(::startGps)
    }

    private fun setUrlCallInProgress(value:Boolean){urlCallInProgress = value}

    private fun initMapBuildTrack(){
        mapBuildTrack = MapBuildTrack(activityContext,mapView)
        mapBuildTrack.setCurrentOverlay()
    }

    private fun setTrackPathMenu(){
        binding.popupBtn.setOnClickListener{
            val popUpMenu =  PopupMenu(parentActivity,binding.popupBtn)
            popUpMenu.menuInflater.inflate(R.menu.popup_menu_add_path,popUpMenu.menu)
            popUpMenu.setOnMenuItemClickListener{it: MenuItem ->
                when(it.itemId){
                    R.id.popupDrawPoints->addBottomMenu(0)
                    R.id.popupGpsPoints->addBottomMenu(1)
                    //R.id.popupExit->exitBackToUpload()
                }
                true
            }
            popUpMenu.show()
        }
    }

    private fun addBottomMenu(menuType:Int){
        if(binding.bottomMenuLayout.visibility == View.VISIBLE && menuType == lastMenu){return}
        makeBottomMenuVisible()
        clearPreviousTrack()
        if(menuType==0){addTrackMenu()}
        else if(menuType==1){addGpsMenu()}
        lastMenu = menuType
    }

    private fun addTrackMenu(){
        clearGpsTrack(null)
        trackMenu = TrackMenuBar(parentActivity,null)
        mapBuildTrack.setCallbackUpdateTrackLength(::updateBuildTrackLength)
        binding.bottomMenuLayout.addView(trackMenu)
        trackMenu!!.setEventListener(
            ::addTrackPoints,
            ::subTrackPoints,
            ::saveBuildTrack,
            ::clearBuildTrack)
    }

    private fun addGpsMenu(){
        if(locationPermissionIsProvided()){
            gpsMenu = GpsMenuBar(parentActivity,null)
            binding.bottomMenuLayout.addView(gpsMenu)
            gpsMenu!!.setEventListener(
                ::askUserForReset,
                ::stopGps,
                ::saveGpsTrack,
                ::clearGpsTrack)
            startGps(null)
        }
        else{
            parentActivity.showUserMessage("GpsPermission Is Not Granted")
            removeBottomMenu()
        }
    }

    private fun storeSavedTracks(){
        if(mapBuildTrack.savedTracks.isNotEmpty()){
            parentActivity.pushDataToFragment(
                FragmentInstance.FRAGMENT_UPLOAD,
                mapBuildTrack.savedTracks)
            mapBuildTrack.removeOverlayAndPolyLine()
        }
        removeBottomMenu()
        clearGpsTrack(null)
    }

    private fun makeBottomMenuVisible(){
        binding.bottomMenuLayout.visibility = View.VISIBLE
        binding.bottomMenuLayout.clearChildren(0)
    }

    private fun clearPreviousTrack(){
        clearBuildTrack(null)
        clearGpsTrack(null)
    }

    private fun removeBottomMenu(){
        binding.bottomMenuLayout.visibility = View.GONE
        binding.bottomMenuLayout.clearChildren(0)
        lastMenu = -1
    }

    /*
    *   ##########################################################################
    *               POPUP BOTTOM MENU GPS FUNCTIONS
    *   ##########################################################################
    * */

    private fun askUserForReset(parameter:Any?){
        if(gpsBlinkerIsActive()){askForGpsReset.showMessage()}
        else{startGps(null)}
    }

    private fun startGps(parameter:Any?){
        clearGpsTrack(null)
        setGpsToStorePoints(::updateGpsTrackLength)
        refreshGpsBlinkerMeasureLength()
        activateGps()
    }

    private fun stopGps(parameter:Any?){
        if(!gpsBlinkerIsActive()){return}
        val geoPoints = getCollectedGpsPoints()
        if(geoPoints.isNotEmpty()){
            mapBuildTrack.buildPolyline(geoPoints)
            mapBuildTrack.addPolyLineToMap()
            mapBuildTrack.getBoundaryBox(geoPoints)
            zoomToArea(mapBuildTrack.bbox)
        }
        deActivateGps()
    }

    private fun saveGpsTrack(parameter:Any?){
        if(gpsBlinkerIsActive()){parentActivity.showUserMessage("Stop Gps To View Track Before Saving");return}
        if(allSetForUpload()){
            if(getGpsMeasuredLength() < MIN_TRACK_LENGTH){dismissSaveOnLowTrackLength();return}
            viewLifecycleOwner.lifecycleScope.launch{
                showProgressbar(true)
                val serverResult = ServerDetails()
                val addressInfo = AddressInfo()
                val trackLen = getGpsMeasuredLength().inKilometers()
                val geoPoint = mapBuildTrack.getTrackFirstPoint()
                setUrlCallInProgress(true)
                setSearchTimer()
                getAddress(addressInfo,serverResult,geoPoint)
                mapBuildTrack.saveCurrentTrack(addressInfo.city,addressInfo.street,trackLen,geoPoint)
                showProgressbar(false)
                setUrlCallInProgress(false)
                parentActivity.showUserMessage("Track Saved Successfully!")
                clearGpsTrack(null)
            }
        }
    }

    private fun updateGpsTrackLength(trackLength:String){
        gpsMenu!!.setTrackLength(trackLength)
    }

    private fun clearGpsTrack(parameter: Any?){
        resetGpsBlinker()
        deActivateGps()
        mapBuildTrack.resetMapGpsPath()

    }

    /*
    *   ##########################################################################
    *               POPUP BOTTOM MENU TRACK FUNCTIONS
    *   ##########################################################################
    * */

    private fun addTrackPoints(parameter:Any?){
        if(!mapBuildTrack.trackIsOnMap()){
            mapBuildTrack.setTrack(1)
            mapBuildTrack.addTrackOverlay()
            return
        }
        else if(!mapBuildTrack.expandTrack()){return}
        updateMapBuildTrack()
    }

    private fun subTrackPoints(parameter:Any?){
        if(!mapBuildTrack.trackIsOnMap()){return}
        if(!mapBuildTrack.decreaseTrack()){return}
        updateMapBuildTrack()
    }

    private fun updateMapBuildTrack(){
        mapBuildTrack.adjustPolyline()
        mapBuildTrack.addTrackOverlay()
    }

    private fun saveBuildTrack(parameter:Any?){
        if(allSetForUpload()){
            if(mapBuildTrack.trackLength<MIN_TRACK_LENGTH ){dismissSaveOnLowTrackLength();return;}
            viewLifecycleOwner.lifecycleScope.launch{
                showProgressbar(true)
                val serverResult = ServerDetails()
                val addressInfo = AddressInfo()
                val geoPoint = mapBuildTrack.getTrackFirstPoint()
                val trackLen = mapBuildTrack.getTrackLengthInKilometers()
                setUrlCallInProgress(true)
                setSearchTimer()
                getAddress(addressInfo,serverResult,geoPoint)
                mapBuildTrack.saveCurrentTrack(addressInfo.city,addressInfo.street,trackLen,geoPoint)
                showProgressbar(false)
                setUrlCallInProgress(false)
                parentActivity.showUserMessage("Track Saved Successfully!")
                clearBuildTrack(null)
            }
        }
    }

    private fun updateBuildTrackLength(trackLength:String){
        trackMenu!!.setTrackLength(trackLength)
    }

    private fun clearBuildTrack(parameter: Any?){
        mapBuildTrack.resetMapTrackPath()
    }

    /*
    *   ##########################################################################
    *               CHECK BEFORE SAVE
    *   ##########################################################################
    * */

    private fun allSetForUpload():Boolean{
        return (mapBuildTrack.trackIsOnMap()
                && checkSearchTimer() &&
                !urlCallInProgress)
    }

    private fun dismissSaveOnLowTrackLength(){
        parentActivity.showUserMessage("Track shorter then 1000m does not fill the criteria for upload")
    }

    /*
    *   ##########################################################################
    *               GET ADDRESS FROM OSM SERVER AND SAVE
    *   ##########################################################################
    * */

    private suspend fun getAddress2(addressInfo: AddressInfo, serverResult: ServerDetails){
        withContext(Dispatchers.IO) {
            thread {
                serverResult.serverResult = ServerResult.UPLOAD_OK
                addressInfo.city = "City"
                addressInfo.street = "Street"
                Thread.sleep(500)
            }.join()
        }
    }

    private suspend fun getAddress(addressInfo: AddressInfo, serverResult: ServerDetails,geoPoint:GeoPoint){
        val requestString = "https://nominatim.openstreetmap.org/reverse?lat=" +
                geoPoint.latitude.toString() + "&lon=" + geoPoint.longitude.toString() + "&zoom=18&format=jsonv2"
        withContext(Dispatchers.IO) {
            thread {
                val json =  try {
                    URL(requestString).readText()}
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

        if(addressInfo.city.isEmpty()){addressInfo.city = "City"}
        if(addressInfo.street.isEmpty()){addressInfo.street = "Street"}
    }

    /*
    *   ##########################################################################
    *               RESPECT NOMINATIM 1/SEC POLICY
    *   ##########################################################################
    * */

    private fun checkSearchTimer():Boolean{
        if(System.currentTimeMillis()-lastUrlCall <= URL_TIMER){
            parentActivity.showMessage("Please wait and try again...", Toast.LENGTH_SHORT)
            return false
        }
        return true
    }

    private fun setSearchTimer(){
        lastUrlCall = System.currentTimeMillis()
    }

    override fun onDestroyView() {
        storeSavedTracks()
        super.onDestroyView()
    }
}