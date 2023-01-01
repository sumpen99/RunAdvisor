package com.example.runadvisor.fragment
import android.view.View
import androidx.fragment.app.Fragment
import com.example.runadvisor.enums.FragmentInstance
import com.example.runadvisor.map.MapShowTrack
import com.example.runadvisor.adapter.CustomDownloadAdapter
import com.example.runadvisor.io.printToTerminal

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
        mapView.controller.setZoom(viewHolder.zoom)
        mapView.controller.setCenter(viewHolder.centerPoint)
    }

    private fun removePopUpButton(){ binding.popupBtn.visibility = View.GONE }

    private fun drawTrackOnMap(){
        val mapShowTrack = MapShowTrack(activityContext,mapView)
        mapShowTrack.setCurrentOverlay()
        mapShowTrack.buildPolyline(viewHolder.trackPoints)
        mapShowTrack.addPolyLineToMap()
        mapShowTrack.invalidate()
    }
}