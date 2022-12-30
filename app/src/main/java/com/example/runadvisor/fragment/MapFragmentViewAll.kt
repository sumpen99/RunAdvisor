package com.example.runadvisor.fragment
import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.PopupMenu
import com.example.runadvisor.R
import com.example.runadvisor.enums.FragmentInstance
import com.example.runadvisor.io.printToTerminal
import com.example.runadvisor.map.MapTrackOverview
import com.example.runadvisor.methods.getCenterOfHome
import com.example.runadvisor.methods.getLocationUpdates

class MapFragmentViewAll()
    :MapFragment() {
    private lateinit var mapTrackOverview: MapTrackOverview

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTrackOverviewMenu()
        setMapTrackOverview()
        loadRunItems()
    }

    override fun callbackDispatchTouchEvent(event: MotionEvent) {}

    override fun receivedData(parameter: Any?){}

    override fun isRemovable():Boolean{return false}

    override fun getFragmentID(): FragmentInstance {
        return FragmentInstance.FRAGMENT_MAP_TRACK_OVERVIEW
    }

    private fun setMapTrackOverview(){
        mapTrackOverview = MapTrackOverview(activityContext,mapView)
        mapTrackOverview.setCurrentOverlay()
    }

    private fun setTrackOverviewMenu(){
        binding.popupBtn.setOnClickListener{
            val popUpMenu =  PopupMenu(parentActivity,binding.popupBtn)
            popUpMenu.menuInflater.inflate(R.menu.popup_menu_base,popUpMenu.menu)
            popUpMenu.setOnMenuItemClickListener{it: MenuItem ->
                when(it.itemId){
                    R.id.popupGps-> activateGps()
                    R.id.popupSearch-> printToTerminal("popupSearch")
                }
                true
            }
            popUpMenu.show()
        }
    }

    private fun loadRunItems(){
        if(parentActivity.runItemsIsNotNull()){
            mapTrackOverview.addOverviewMarkers(parentActivity.getRunItems())
        }
    }

    /*
    *   ##########################################################################
    *                                ON VIEW CHANGED
    *   ##########################################################################
    *
    * */

    override fun onDestroyView() {
        super.onDestroyView()
        mapTrackOverview.removeOverlayAndPolyLine()
    }

}