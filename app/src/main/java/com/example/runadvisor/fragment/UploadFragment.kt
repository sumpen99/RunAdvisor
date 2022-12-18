package com.example.runadvisor.fragment
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.runadvisor.R
import com.example.runadvisor.activity.HomeActivity
import com.example.runadvisor.databinding.FragmentUploadBinding
import com.example.runadvisor.enums.FragmentInstance
import com.example.runadvisor.interfaces.IFragment
import com.example.runadvisor.methods.*
import com.example.runadvisor.struct.PublicRunItem
import com.example.runadvisor.struct.UserRunItem
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.*
import com.example.runadvisor.struct.SavedTrack
import com.example.runadvisor.widget.CustomMapAdapter

class UploadFragment(val removable:Boolean,val fragmentId:FragmentInstance):Fragment(R.layout.fragment_upload),IFragment {
    private lateinit var activityContext: Context
    private lateinit var parentActivity: Activity
    private lateinit var recyclerView: RecyclerView
    private lateinit var customAdapter: CustomMapAdapter
    private var _binding: FragmentUploadBinding? = null
    var savedTracks = ArrayList<SavedTrack>()
    private val binding get() = _binding!!
    private val GALLERY_REQUEST_CODE = 102
    private val PICK_IMAGE = 1
    private var fileUri: Uri? = null
    private var filePath:String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        _binding = FragmentUploadBinding.inflate(inflater, container, false)
        val view: View = binding.root
        setParentActivity()
        setActivityContext()
        setEventListener()
        if(checkForSavedTracks()){
            setRecyclerView()
            setAdapter()
        }
        return view
    }

    override fun isRemovable():Boolean{
        return removable
    }

    override fun getFragmentID(): FragmentInstance {
        return fragmentId
    }

    override fun receivedData(parameter: Any?){
        if(parameter!=null){savedTracks = parameter as ArrayList<SavedTrack>}
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
        customAdapter = CustomMapAdapter(parentActivity,savedTracks)
        recyclerView.adapter = customAdapter
    }

    private fun checkForSavedTracks():Boolean{
        return savedTracks.isNotEmpty()
    }

    private fun setEventListener(){
        binding.uploadItemBtn.setCallback(null,::uploadUserRunItem)
        binding.drawPathBtn.setCallback(null,::drawPathOnMap)
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

    private fun drawPathOnMap(parameters:Any?){
        (parentActivity as HomeActivity).navigateFragment(FragmentInstance.FRAGMENT_MAP_CHILD)
    }

    private fun uploadUserRunItem(parameters:Any?){
        parentActivity.showMessage("Not a valid form...",Toast.LENGTH_SHORT)
        /*if(validUploadData()){
            val user = Firebase.auth.currentUser
            val city = binding.cityText.text.toString()
            val street = binding.streetText.text.toString()
            val runKm = binding.runKmText.text.toString()
            val shareWithPublic = (binding.checkboxShare as CheckBox).isChecked
            val urlId = UUID.randomUUID().toString()
            val downloadUrl = urlId + ".${filePath!!.split(".")[1]}"
            val item = UserRunItem(city,street,runKm,shareWithPublic, UUID.randomUUID().toString(),downloadUrl)
            Firebase.firestore
                .collection(getUserCollection())
                .document(user!!.uid)
                .collection(getUserItemCollection()).
                add(item).addOnCompleteListener { task ->
                    if(task.isSuccessful && item.shareWithPublic){uploadPublicRunItem(item)}
                    else{parentActivity.showMessage("Upload Failed ${task.exception}", Toast.LENGTH_SHORT)}}
        }
        else{parentActivity.showMessage("Not a valid form...",Toast.LENGTH_SHORT)}*/
    }



    private fun uploadPublicRunItem(userItem: UserRunItem){
        val publicItem = PublicRunItem(userItem.city,userItem.street,userItem.runKm,userItem.downloadUrl)
        Firebase.firestore
            .collection(getPublicCollection())
            .document(userItem.runItemID)
            .set(publicItem).addOnCompleteListener { task ->
                if(task.isSuccessful) {uploadImageToFirebase(publicItem)}
                else{parentActivity.showMessage(task.exception.toString(), Toast.LENGTH_SHORT)}}
    }

    private fun uploadImageToFirebase(item: PublicRunItem) {
        val database = Firebase.storage.reference
        val path = "${getImagePath()}${item.downloadUrl}"
        val storageRef = database.child(path)
        storageRef.putFile(fileUri!!)
            .addOnCompleteListener { task ->
                var msg = "Item Uploaded"
                if(!task.isSuccessful){msg = task.exception.toString()}
                parentActivity.showMessage(msg,Toast.LENGTH_SHORT)
            }
    }

    private fun validUploadData():Boolean{
        return (Firebase.auth.currentUser!=null &&
                //binding.cityText.text.toString().isNotEmpty() &&
                //binding.streetText.text.toString().isNotEmpty() &&
                //binding.runKmText.text.toString().isNotEmpty() &&
                filePath != null &&
                fileUri != null)
    }
}