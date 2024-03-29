package com.example.runadvisor.fragment
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.runadvisor.MainActivity
import com.example.runadvisor.R
import com.example.runadvisor.adapter.CustomUploadAdapter
import com.example.runadvisor.databinding.FragmentUploadBinding
import com.example.runadvisor.enums.FragmentInstance
import com.example.runadvisor.enums.ServerResult
import com.example.runadvisor.interfaces.IFragment
import com.example.runadvisor.methods.*
import com.example.runadvisor.struct.*
import kotlinx.coroutines.launch
import java.util.*


class UploadFragment:Fragment(R.layout.fragment_upload),IFragment {
    private lateinit var activityContext: Context
    private lateinit var parentActivity: MainActivity
    private lateinit var recyclerView: RecyclerView
    private lateinit var customAdapter: CustomUploadAdapter
    private var uploadView:View? = null
    private var progressBar:ProgressBar? = null
    private var _binding: FragmentUploadBinding? = null
    private val binding get() = _binding!!
    private val uploadResult = ArrayList<ServerDetails>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        if(uploadView!=null){return uploadView!!}
        _binding = FragmentUploadBinding.inflate(inflater, container, false)
        uploadView = binding.root
        setParentActivity()
        setActivityContext()
        setEventListener()
        setRecyclerView()
        setAdapter()
        return uploadView!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(progressBar == null){addProgressBar()}
    }

    override fun isRemovable():Boolean{ return false }

    override fun hasParentFragment(): FragmentInstance?{ return null}

    override fun needDispatch():Boolean{return false}

    override fun getFragmentID(): FragmentInstance { return FragmentInstance.FRAGMENT_UPLOAD }

    override fun receivedData(parameter: Any?){
        if(parameter!=null){
            customAdapter.addItems(parameter as ArrayList<SavedTrack>)
        }
    }

    override fun callbackDispatchTouchEvent(parameter:Any?){}

    private fun setActivityContext() { activityContext = requireContext() }

    private fun setParentActivity() { parentActivity = requireActivity() as MainActivity }

    private fun setRecyclerView(){
        recyclerView = binding.trackRecyclerview
        recyclerView.layoutManager = LinearLayoutManager(activityContext)
    }

    private fun setAdapter(){
        customAdapter = CustomUploadAdapter(parentActivity)
        recyclerView.adapter = customAdapter
    }

    private fun setEventListener(){
        binding.uploadItemBtn.setCallback(null,::uploadDataToServer)
        binding.drawPathBtn.setCallback(null,::drawPathOnMap)
    }

    private fun addProgressBar(){
        val layout = parentActivity.findViewById<ConstraintLayout>(R.id.uploadLayout)
        progressBar = getProgressbar(parentActivity,layout)
    }

    private fun setProgressbar(show:Boolean){
        if(show){progressBar!!.visibility = VISIBLE}
        else{progressBar!!.visibility = GONE}
    }

    /*
    *   ##########################################################################
    *                                RECYCLEVIEW
    *   ##########################################################################
    *
    * */

    private fun clearAllItemsFromRecycleView(){customAdapter.clearView()}

    private fun clearItemsFromRecycleView(){
        var msg = ""
        for(result:ServerDetails in uploadResult){
            if(result.serverResult != ServerResult.UPLOAD_OK){
                msg+= "Item ${result.pos+1}: ${result.msg}\n"
            }
        }
        clearAllItemsFromRecycleView()
        if(msg.isEmpty()){parentActivity.showUserMessage("Upload Successful!")}
        else{parentActivity.showUserMessage(msg)}
    }

    /*
    *   ##########################################################################
    *                                OPEN MAPVIEW CALLBACK
    *   ##########################################################################
    *
    * */

    private fun drawPathOnMap(parameters:Any?){
        parentActivity.navigateFragment(FragmentInstance.FRAGMENT_MAP_TRACK_PATH)
    }

    /*
    *   ##########################################################################
    *                                UPLOAD DATA
    *   ##########################################################################
    *
    * */

    private fun uploadDataToServer(parameter:Any?){
        if(!uploadIsPossible()){return}
        parentActivity.firestoreViewModel.clearServerDetails()
        viewLifecycleOwner.lifecycleScope.launch{
            setProgressbar(true)
            uploadPublicRunItem(0)
            clearItemsFromRecycleView()
            setProgressbar(false)
        }
    }

    private suspend fun uploadPublicRunItem(pos:Int){
        val savedTrack = customAdapter.getSavedTrack(pos)
        if(savedTrack==null){return}
        val downloadUrl = UUID.randomUUID().toString()
        val imageUri = parentActivity.getImageUri(savedTrack.bitmap, downloadUrl)
        if(imageUri==null){return}
        val docId = UUID.randomUUID().toString()
        val userItem = UserItem(docId,downloadUrl)
        val city = savedTrack.city.ifEmpty{ "Undefined" }
        val street = savedTrack.street.ifEmpty{ "Undefined" }
        val runItem = RunItem(
            city,
            street,
            savedTrack.trackLength,
            downloadUrl,
            getGeoPointsToDouble(savedTrack.geoPoints),
            getGeoPointToDouble(savedTrack.center),
            savedTrack.zoom,
            docId,
            0,
            savedTrack.date)

        if(!(parentActivity.firestoreViewModel.savePublicRunItemToFirebase(pos,runItem)&&
            parentActivity.firestoreViewModel.saveImageToFirebase(pos,imageUri,downloadUrl)&&
            parentActivity.firestoreViewModel.saveUserRunItemToFirebase(pos,userItem))){

            // If Something went wrong, delete everything
            parentActivity.firestoreViewModel.deletePublicRunItem(runItem.docID!!)
            parentActivity.firestoreViewModel.deleteImage(runItem.downloadUrl!!)
            parentActivity.firestoreViewModel.deleteUserRunItem(userItem.docId!!)

        }

        parentActivity.deleteFile(imageUri)
        uploadPublicRunItem(pos+1)
    }

    private fun uploadIsPossible():Boolean{
        if(customAdapter.itemCount == 0 || progressBar!!.visibility == VISIBLE){return false}
        if(!parentActivity.verifyStoragePermission()){
            parentActivity.showUserMessage("Storage Permission Not Granted\nUpload Failed")
            return false
        }
        return true
    }

    /*
    *   ##########################################################################
    *                                ON VIEW CHANGED
    *   ##########################################################################
    *
    * */


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}