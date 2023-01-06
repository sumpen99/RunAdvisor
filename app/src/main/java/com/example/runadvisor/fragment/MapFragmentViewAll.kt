package com.example.runadvisor.fragment
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import com.example.runadvisor.R
import com.example.runadvisor.enums.FragmentInstance
import com.example.runadvisor.map.MapShowTrack
import com.example.runadvisor.marker.MarkerClickable
import com.example.runadvisor.methods.locationPermissionIsProvided

class MapFragmentViewAll:MapFragment() {
    private lateinit var mapShowTrack: MapShowTrack
    private var markerHasTouch: MarkerClickable? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTrackOverviewMenu()
        setMapTrackOverview()
    }


    override fun receivedData(parameter: Any?){}

    override fun isRemovable():Boolean{return false}

    override fun getFragmentID(): FragmentInstance { return FragmentInstance.FRAGMENT_MAP_TRACK_OVERVIEW }

    override fun hasParentFragment(): FragmentInstance?{ return null}

    private fun setMapTrackOverview(){
        mapShowTrack = MapShowTrack(activityContext,mapView,::markerHasTouch)
        mapShowTrack.setCurrentOverlay()
    }

    private fun markerHasTouch(marker:MarkerClickable){
        mapShowTrack.removePolyline()
        if(markerHasTouch != null && marker.index == markerHasTouch!!.index){
            markerHasTouch = null
        }
        else{
            markerHasTouch = marker
            mapShowTrack.buildPolyline(marker.trackPoints)
            mapShowTrack.addPolyLineToMap()
            zoomToPosition(marker.geoPoint,marker.zoom)
        }
    }

    private fun setTrackOverviewMenu(){
        binding.popupBtn.setOnClickListener{
            val popUpMenu =  PopupMenu(parentActivity,binding.popupBtn)
            popUpMenu.menuInflater.inflate(R.menu.popup_menu_base,popUpMenu.menu)
            popUpMenu.setOnMenuItemClickListener{it: MenuItem ->
                when(it.itemId){
                    R.id.popupGps-> moveToGpsPosition()
                    R.id.popupSearch->parentActivity.showUserMessage("To Be Implemented!")
                }
                true
            }
            popUpMenu.show()
        }
    }

    private fun moveToGpsPosition(){
        if(locationPermissionIsProvided()){
            if(!gpsBlinkerIsActive()){activateGps()}
            else{deActivateGps()}
        }
        else{parentActivity.showUserMessage("GpsPermission Is Not Granted")}

    }

    override fun setMapPosition() {
        if(parentActivity.runItemsIsNotNull()){
            mapShowTrack.addOverviewMarkers(parentActivity.getRunItems())
            if(mapData.geoPoint == null){
                zoomToArea(mapShowTrack.bbox)
            }
            else{
                resetLastPosition()
            }
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
        mapShowTrack.removeOverlayAndPolyLine()
    }

}