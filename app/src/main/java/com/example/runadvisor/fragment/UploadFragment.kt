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
import com.example.runadvisor.R
import com.example.runadvisor.activity.HomeActivity
import com.example.runadvisor.databinding.FragmentUploadBinding
import com.example.runadvisor.enums.FragmentInstance
import com.example.runadvisor.enums.ServerResult
import com.example.runadvisor.interfaces.IFragment
import com.example.runadvisor.methods.*
import com.example.runadvisor.struct.*
import java.util.*
import com.example.runadvisor.widget.CustomMapAdapter
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList

class UploadFragment():Fragment(R.layout.fragment_upload),IFragment {
    private lateinit var activityContext: Context
    private lateinit var parentActivity: HomeActivity
    private lateinit var recyclerView: RecyclerView
    private lateinit var customAdapter: CustomMapAdapter
    private lateinit var messageToUser: MessageToUser
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
        setInfoToUser()
        return uploadView!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(progressBar == null){addProgressBar()}
    }

    override fun isRemovable():Boolean{
        return false
    }

    override fun getFragmentID(): FragmentInstance {
        return FragmentInstance.FRAGMENT_UPLOAD
    }

    override fun receivedData(parameter: Any?){
        if(parameter!=null){
            customAdapter.addItems(parameter as ArrayList<SavedTrack>)
        }
    }

    override fun callbackDispatchTouchEvent(event: MotionEvent){}

    private fun setActivityContext() {
        activityContext = requireContext()
    }

    private fun setParentActivity() {
        parentActivity = requireActivity() as HomeActivity
    }

    private fun setRecyclerView(){
        recyclerView = binding.trackRecyclerview
        recyclerView.layoutManager = LinearLayoutManager(activityContext)
    }

    private fun setAdapter(){
        customAdapter = CustomMapAdapter(parentActivity)
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

    private fun setInfoToUser(){
        messageToUser = MessageToUser(parentActivity,null)
    }

    private fun showUserMessage(msg:String){
        messageToUser.setMessage(msg)
        messageToUser.showMessage()
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
            // DID NOT WORK SO GOOD
           /*if(result.serverResult == ServerResult.UPLOAD_OK){customAdapter.removeCard(result.pos)}
           else{msg+= "Item ${result.pos+1}: ${result.msg}\n"}*/
            if(result.serverResult != ServerResult.UPLOAD_OK){
                msg+= "Item ${result.pos+1}: ${result.msg}\n"
            }
        }
        clearAllItemsFromRecycleView()
        if(msg.isEmpty()){showUserMessage("Upload Successful!")}
        else{showUserMessage(msg)}
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
        val runItem = RunItem(
            savedTrack.city,
            savedTrack.street,
            savedTrack.trackLength,
            downloadUrl,
            getGeoPointsToDouble(savedTrack.geoPoints),
            getGeoPointToDouble(savedTrack.center),
            savedTrack.zoom,
            docId
        )
        // If Something went wrong, delete everything
        if(!(parentActivity.firestoreViewModel.savePublicRunItemToFirebase(pos,runItem)&&
            parentActivity.firestoreViewModel.saveImageToFirebase(pos,imageUri,downloadUrl)&&
            parentActivity.firestoreViewModel.saveUserRunItemToFirebase(pos,userItem))){

            parentActivity.firestoreViewModel.deletePublicRunItem(runItem)
            parentActivity.firestoreViewModel.deleteImage(runItem)
            parentActivity.firestoreViewModel.deleteUserRunItem(userItem)

        }

        parentActivity.deleteFile(imageUri)
        uploadPublicRunItem(pos+1)
    }

    private fun uploadIsPossible():Boolean{
        return (customAdapter.itemCount > 0 && progressBar!!.visibility != View.VISIBLE)
    }

    /*
    *   ##########################################################################
    *                                ON VIEW CHANGED
    *   ##########################################################################
    *
    * */

    override fun onResume() {
        super.onResume()
        //loadData()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}