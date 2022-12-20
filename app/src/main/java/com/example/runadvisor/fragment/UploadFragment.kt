package com.example.runadvisor.fragment
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
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
import com.example.runadvisor.interfaces.IFragment
import com.example.runadvisor.methods.*
import com.example.runadvisor.struct.RunItem
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.*
import com.example.runadvisor.struct.SavedTrack
import com.example.runadvisor.struct.ServerDetails
import com.example.runadvisor.widget.CustomMapAdapter
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.collections.ArrayList

class UploadFragment(val removable:Boolean,val fragmentId:FragmentInstance):Fragment(R.layout.fragment_upload),IFragment {
    private lateinit var activityContext: Context
    private lateinit var parentActivity: Activity
    private var uploadView:View? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar:ProgressBar
    private lateinit var customAdapter: CustomMapAdapter
    private var _binding: FragmentUploadBinding? = null
    private val binding get() = _binding!!
    private val GALLERY_REQUEST_CODE = 102
    private val PICK_IMAGE = 1
    private var fileUri: Uri? = null
    private var filePath:String? = null
    private val errorMessages = ArrayList<ServerDetails>()

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

    override fun isRemovable():Boolean{
        return removable
    }

    override fun getFragmentID(): FragmentInstance {
        return fragmentId
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
        parentActivity = requireActivity()
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
        progressBar = ProgressBar(parentActivity)
        progressBar.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        progressBar.visibility = View.GONE
        val layout = parentActivity.findViewById<ConstraintLayout>(R.id.uploadLayout)
        progressBar.x = (getScreenWidth()/2 - progressBar.width/2).toFloat()
        progressBar.y = (getScreenHeight()/2 - parentActivity.removeActionBarHeight()).toFloat()
        layout.addView(progressBar)
    }

    private fun setProgressbar(show:Boolean){
        if(show){progressBar.visibility = View.VISIBLE}
        else{progressBar.visibility = View.GONE}
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE
            && resultCode == Activity.RESULT_OK
            && data != null
            && data.data != null
        ) {
            filePath = parentActivity.getFilePathFromIntent(data)
            fileUri = data.data!!
            //parentActivity.loadImageFromPhone(fileUri.toString(),binding.imageView)
        }
    }

    private fun clearItemsFromRecycleView(){customAdapter.clearView()}

    private fun drawPathOnMap(parameters:Any?){
        (parentActivity as HomeActivity).navigateFragment(FragmentInstance.FRAGMENT_MAP_CHILD)
    }

    private fun uploadDataToServer(parameter:Any?){
        if(customAdapter.itemCount <=0){return}
        errorMessages.clear()
        viewLifecycleOwner.lifecycleScope.launch{
            setProgressbar(true)
            uploadUserRunItem(0)
            //if(errorMessages.size>0){printToTerminal(errorMessages[0].msg)}
            // dont remove not uploaded items
            clearItemsFromRecycleView()
            setProgressbar(false)
        }
    }

    //https://stackoverflow.com/questions/71692116/how-to-initiate-a-truely-asynchronous-function-with-coroutines
    //https://betterprogramming.pub/how-to-use-kotlin-coroutines-with-firebase-6f8577a3e00f
    private suspend fun uploadUserRunItem(pos:Int){
        val savedTrack = customAdapter.getSavedTrack(pos)
        if(savedTrack==null){return}
        val downloadUrl = UUID.randomUUID().toString()
        val user = Firebase.auth.currentUser
        var uploadImage = false
        val runItem = RunItem(
            savedTrack.city,
            savedTrack.street,
            savedTrack.trackLength,
            downloadUrl,
            getGeoPointsToDouble(savedTrack.geoPoints),
            getGeoPointToDouble(savedTrack.center)
        )
        Firebase.firestore
            .collection(getRunItemsCollection())
            .document(user!!.uid)
            .collection(getUserItemCollection())
            .add(runItem).addOnCompleteListener { task ->
                if(task.isSuccessful){uploadImage = true}
                else{errorMessages.add(ServerDetails(pos,task.exception.toString()))}
            }.await()
        if(uploadImage){uploadImageToFirebase(downloadUrl,savedTrack.bitmap)}
        uploadUserRunItem(pos+1)
    }

    private suspend fun uploadImageToFirebase(downloadUrl:String,bitmap:Bitmap) {
        val path = "${getImagePath()}$downloadUrl"
        val imageUri = parentActivity.getImageUri(bitmap, downloadUrl)
        if (imageUri != null) {
            val database = Firebase.storage.reference
            val storageRef = database.child(path)
            storageRef.putFile(imageUri)
                .addOnCompleteListener { task ->
                    parentActivity.deleteFile(imageUri)
                }.await()
        }
    }
}