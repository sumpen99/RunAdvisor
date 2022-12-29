package com.example.runadvisor.fragment
import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.runadvisor.R
import com.example.runadvisor.enums.FragmentInstance
import com.example.runadvisor.enums.ServerResult
import com.example.runadvisor.map.MapTrackPath
import com.example.runadvisor.methods.clearChildren
import com.example.runadvisor.methods.getProgressbar
import com.example.runadvisor.methods.showMessage
import com.example.runadvisor.methods.toMap
import com.example.runadvisor.struct.*
import com.example.runadvisor.widget.TrackMenuBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import kotlin.concurrent.thread

class MapFragmentUpload
    :MapFragment() {
    private var urlCallInProgress:Boolean = false
    private var progressBar: ProgressBar? = null
    private var trackMenu:TrackMenuBar?=null
    private var mapTrackPath: MapTrackPath? = null
    private val URL_TIMER:Long = 1000
    private var lastUrlCall:Long = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTrackPathMenu()
        if(progressBar == null){addProgressBar()}
    }

    override fun callbackDispatchTouchEvent(event: MotionEvent) {}

    override fun receivedData(parameter: Any?){}

    override fun isRemovable():Boolean{return true}

    override fun getFragmentID(): FragmentInstance {
        return FragmentInstance.FRAGMENT_MAP_TRACK_PATH
    }

    private fun setUrlCallInProgress(value:Boolean){urlCallInProgress = value}

    private fun setMapTrackPath(){
        mapTrackPath = MapTrackPath(activityContext,mapView,::updateTrackLength)
        mapTrackPath!!.setCurrentOverlay()
    }

    private fun addProgressBar(){
        val layout = parentActivity.findViewById<RelativeLayout>(R.id.mapBaseLayout)
        progressBar = getProgressbar(parentActivity,layout)
    }

    private fun setProgressbar(show:Boolean){
        if(show){progressBar!!.visibility = View.VISIBLE}
        else{progressBar!!.visibility = View.GONE}
    }

    private fun setTrackPathMenu(){
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

    private fun addBottomMenu(){
        if(binding.bottomMenuLayout.visibility == View.VISIBLE){return}
        binding.bottomMenuLayout.visibility = View.VISIBLE
        binding.bottomMenuLayout.clearChildren(0)

        trackMenu = TrackMenuBar(parentActivity,null)
        binding.bottomMenuLayout.addView(trackMenu)
        trackMenu!!.setEventListener(
            ::adjustPointLasso,
            ::adjustPointLasso,
            ::saveTrack,
            ::clearMapTrackPath)
    }

    private fun exitBackToUpload(){
        if(mapTrackPath!=null && mapTrackPath!!.savedTracks.isNotEmpty()){
            parentActivity.pushDataToFragment(
                FragmentInstance.FRAGMENT_UPLOAD,
                mapTrackPath!!.savedTracks)
            mapTrackPath!!.removeOverlayAndPolyLine()
        }
        removeBottomMenu()
        parentActivity.navigateFragment(FragmentInstance.FRAGMENT_UPLOAD)
    }

    private fun removeBottomMenu(){
        binding.bottomMenuLayout.visibility = View.GONE
        binding.bottomMenuLayout.clearChildren(0)
    }

    /*
    *   ##########################################################################
    *               POPUP BOTTOM MENU FUNCTIONS
    *   ##########################################################################
    * */

    private fun adjustPointLasso(parameter:Any?){
        val numPoints:Int = parameter as Int
        if(mapTrackPath == null){
            if(numPoints <= 0){return}
            setMapTrackPath()
            mapTrackPath!!.setLasso(numPoints)
        }
        else{
            if(!mapTrackPath!!.adjustLasso(numPoints)){return}
            mapTrackPath!!.adjustPolyline()
        }
        mapTrackPath!!.addLassoOverlay()
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
                mapTrackPath!!.saveCurrentTrack(addressInfo.city,addressInfo.street)
                setProgressbar(false)
                setUrlCallInProgress(false)
            }
        }
    }

    private fun clearMapTrackPath(parameter: Any?){
        if(mapTrackPath!=null){
            mapTrackPath!!.resetMapTrackPath()
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

    private suspend fun getAddress2(addressInfo: AddressInfo, serverResult: ServerDetails){
        withContext(Dispatchers.IO) {
            thread {
                serverResult.serverResult = ServerResult.UPLOAD_OK
                addressInfo.city = "<Insert City>"
                addressInfo.street = "<Insert Street>"
                Thread.sleep(500)
            }.join()
        }
    }

    private suspend fun getAddress(addressInfo: AddressInfo, serverResult: ServerDetails){
        val geoPoint = mapTrackPath!!.points[0]
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

        if(addressInfo.city.isEmpty()){addressInfo.city = "<Insert City>"}
        if(addressInfo.street.isEmpty()){addressInfo.street = "<Insert Street>"}
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

    private fun clearForUpload():Boolean{
        return (mapTrackPath!=null &&
                mapTrackPath!!.trackIsOnMap() &&
                checkSearchTimer() && !urlCallInProgress
                )
    }
}