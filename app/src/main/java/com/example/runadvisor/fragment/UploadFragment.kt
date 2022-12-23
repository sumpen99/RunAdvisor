package com.example.runadvisor.fragment
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
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
import com.example.runadvisor.io.printToTerminal
import com.example.runadvisor.methods.*
import com.example.runadvisor.struct.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.*
import com.example.runadvisor.widget.CustomMapAdapter
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.collections.ArrayList

class UploadFragment(val removable:Boolean,val fragmentId:FragmentInstance):Fragment(R.layout.fragment_upload),IFragment {
    private lateinit var activityContext: Context
    private lateinit var parentActivity: HomeActivity
    private lateinit var recyclerView: RecyclerView
    private lateinit var customAdapter: CustomMapAdapter
    private lateinit var messageToUser: MessageToUser
    private var uploadView:View? = null
    private var progressBar:ProgressBar? = null
    private var _binding: FragmentUploadBinding? = null
    private val binding get() = _binding!!
    private val GALLERY_REQUEST_CODE = 102
    private val PICK_IMAGE = 1
    private var fileUri: Uri? = null
    private var filePath:String? = null
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

    private fun drawPathOnMap(parameters:Any?){
        (parentActivity as HomeActivity).navigateFragment(FragmentInstance.FRAGMENT_MAP_CHILD)
    }

    private fun uploadDataToServer(parameter:Any?){
        if(customAdapter.itemCount <=0){return}
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



    private suspend fun removeDocument(docId:String){
        val eventsRef: CollectionReference = Firebase.firestore.collection(getUserRunItemsCollection())
        val docIdQuery: Query = eventsRef.whereEqualTo("docId", docId)
        docIdQuery.get().addOnCompleteListener{task->
                if(task.isSuccessful){
                    for (document in task.result){
                        document.reference.delete().addOnSuccessListener{
                            printToTerminal("Document successfully deleted!")
                        }.addOnFailureListener{
                            printToTerminal("Error deleting document ${it.message.toString()}")}
                    }
                }
                else{printToTerminal("Error getting documents: ${task.exception}")}
            }.await()

    }

}