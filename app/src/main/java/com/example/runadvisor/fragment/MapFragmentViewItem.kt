package com.example.runadvisor.fragment
import android.view.View
import com.example.runadvisor.enums.FragmentInstance
import com.example.runadvisor.map.MapShowTrack
import com.example.runadvisor.adapter.CustomDownloadAdapter
import com.example.runadvisor.methods.templateFunctionMarker

class MapFragmentViewItem:MapFragment() {
    lateinit var viewHolder: CustomDownloadAdapter.ViewHolder

    override fun onResume() {
        super.onResume()
        setViewHolderPos()
        removePopUpButton()
        drawTrackOnMap()
    }


    override fun receivedData(parameter: Any?){ viewHolder = parameter as CustomDownloadAdapter.ViewHolder }

    override fun isRemovable():Boolean{return true}

    override fun getFragmentID(): FragmentInstance { return FragmentInstance.FRAGMENT_MAP_TRACK_ITEM }

    override fun hasParentFragment(): FragmentInstance?{ return FragmentInstance.FRAGMENT_DATA}

    private fun setViewHolderPos() {
        zoomToPosition(viewHolder.centerPoint,viewHolder.zoom)
    }

    private fun removePopUpButton(){ binding.popupBtn.visibility = View.GONE }

    private fun drawTrackOnMap(){
        val mapShowTrack = MapShowTrack(activityContext,mapView,::templateFunctionMarker)
        mapShowTrack.setCurrentOverlay()
        mapShowTrack.buildPolyline(viewHolder.trackPoints)
        mapShowTrack.addPolyLineToMap()
        mapShowTrack.invalidate()
    }
}