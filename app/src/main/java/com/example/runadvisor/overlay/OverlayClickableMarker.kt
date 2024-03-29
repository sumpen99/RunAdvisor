package com.example.runadvisor.overlay
import android.content.Context
import android.graphics.Point
import androidx.appcompat.content.res.AppCompatResources
import com.example.runadvisor.marker.MarkerClickable
import com.example.runadvisor.methods.getDoubleToGeoPoint
import com.example.runadvisor.methods.getDoubleToGeoPoints
import com.example.runadvisor.struct.RunItem
import org.osmdroid.api.IMapView
import org.osmdroid.views.overlay.ItemizedIconOverlay

class OverlayClickableMarker(private val activityContext: Context,
                             private val markers:MutableList<MarkerClickable>,
                             private val pOnItemGestureListener: OnItemGestureListener<MarkerClickable>,
):
    ItemizedIconOverlay<MarkerClickable>(activityContext,markers,pOnItemGestureListener) {

    private val drawableDefault = AppCompatResources.getDrawable(activityContext,org.osmdroid.wms.R.drawable.marker_default)

    fun addCustomItem(runItem:RunItem, index:Int){
        val marker = MarkerClickable(
            runItem.city!!,
            runItem.street!!,
            getDoubleToGeoPoint(runItem.center!!),
            getDoubleToGeoPoints(runItem.coordinates!!),
            runItem.zoom!!,
            index,)
        marker.setMarker(drawableDefault)
        super.addItem(marker)
    }

    override fun addItems(items: MutableList<MarkerClickable>?): Boolean {
        return super.addItems(items)
    }

    override fun createItem(i: Int): MarkerClickable {
        return markers[i]
    }

    override fun onSnapToItem(x: Int, y: Int, snapPoint: Point?, mapView: IMapView?): Boolean {
        return false
    }

    override fun size(): Int {
        if(markers == null){return 0}
        return markers.size
    }


}