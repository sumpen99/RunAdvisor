package com.example.runadvisor.fragment
import android.view.MotionEvent
import android.view.View
import com.example.runadvisor.enums.FragmentInstance
import com.example.runadvisor.map.MapTrackOverview
import com.example.runadvisor.adapter.CustomDownloadAdapter

class MapFragmentViewItem():MapFragment() {
    lateinit var viewHolder: CustomDownloadAdapter.ViewHolder

    override fun onResume() {
        super.onResume()
        setViewHolderPos()
        removePopUpButton()
        drawTrackOnMap()
    }

    override fun callbackDispatchTouchEvent(event: MotionEvent) {}

    override fun receivedData(parameter: Any?){
        viewHolder = parameter as CustomDownloadAdapter.ViewHolder
    }

    override fun isRemovable():Boolean{return true}

    override fun getFragmentID(): FragmentInstance {
        return FragmentInstance.FRAGMENT_MAP_TRACK_ITEM
    }

    private fun setViewHolderPos() {
        mapView.controller.setZoom(viewHolder.zoom)
        mapView.controller.setCenter(viewHolder.centerPoint)
    }

    private fun removePopUpButton(){
        binding.popupBtn.visibility = View.GONE
    }

    private fun drawTrackOnMap(){
        val mapTrackOverview = MapTrackOverview(activityContext,mapView)
        mapTrackOverview.setCurrentOverlay()
        mapTrackOverview.buildPolyline(viewHolder.trackPoints)
        mapTrackOverview.addPolyLineToMap()
        mapTrackOverview.invalidate()
    }











}