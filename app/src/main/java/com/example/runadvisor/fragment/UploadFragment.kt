package com.example.runadvisor.fragment
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.runadvisor.R
import com.example.runadvisor.databinding.FragmentUploadBinding
import com.example.runadvisor.enums.FragmentInstance
import com.example.runadvisor.interfaces.IFragment
import com.example.runadvisor.methods.*
import com.example.runadvisor.struct.PublicRunItem
import com.example.runadvisor.struct.UserRunItem
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.util.*
import com.example.runadvisor.io.printToTerminal

class UploadFragment:Fragment(R.layout.fragment_upload),IFragment {
    private lateinit var activityContext: Context
    private lateinit var parentActivity: Activity
    private var _binding: FragmentUploadBinding? = null
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
        setEventListener(view)
        return view
    }

    override fun getFragmentID(): FragmentInstance {
        return FragmentInstance.FRAGMENT_UPLOAD
    }

    override fun processWork(parameter: Any?){}

    override fun callbackDispatchTouchEvent(event: MotionEvent){}

    private fun setActivityContext() {
        activityContext = requireContext()
    }

    private fun setParentActivity() {
        parentActivity = requireActivity()
    }

    private fun clearLastUpload(){
        filePath = null
        fileUri = null
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setEventListener(view: View){
        binding.uploadItemBtn.setOnClickListener{ uploadUserRunItem()}
        binding.loadImageBtn.setOnClickListener{selectImageFromGallery(PICK_IMAGE)}
        view.setOnTouchListener { v, event ->
            when(event.actionMasked){
                MotionEvent.ACTION_DOWN -> {binding.cityText.hideKeyboard();binding.streetText.hideKeyboard()}
                //MotionEvent.ACTION_POINTER_DOWN -> {}
                //MotionEvent.ACTION_MOVE -> {}
                //MotionEvent.ACTION_UP -> {}
                //MotionEvent.ACTION_POINTER_UP -> {}
                //MotionEvent.ACTION_CANCEL -> {}
            }
            true
        }
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
            parentActivity.loadImageFromPhone(fileUri.toString(),binding.imageView)
        }
    }

    private fun uploadUserRunItem(){
        if(validUploadData()){
            val user = Firebase.auth.currentUser
            val city = binding.cityText.text.toString()
            val street = binding.streetText.text.toString()
            val urlId = UUID.randomUUID().toString()
            val downloadUrl = urlId + ".${filePath!!.split(".")[1]}"
            val item = UserRunItem(city,street,true, UUID.randomUUID().toString(),downloadUrl)
            Firebase.firestore
                .collection("UserData")
                .document(user!!.uid)
                .collection("RunItems").
                add(item).addOnCompleteListener { task ->
                    if(task.isSuccessful && item.shareWithPublic){uploadPublicRunItem(item)}
                    else{
                        Toast.makeText(parentActivity,"Upload Failed ${task.exception}", Toast.LENGTH_SHORT).show()}
                }
        }
    }

    private fun uploadPublicRunItem(userItem: UserRunItem){
        val publicItem = PublicRunItem(userItem.city,userItem.street,userItem.downloadUrl)
        Firebase.firestore
            .collection("PublicData")
            .document(userItem.runItemID)
            .set(publicItem).addOnCompleteListener { task ->
                if(task.isSuccessful) {uploadImageToFirebase(publicItem)}
                else{
                    Toast.makeText(parentActivity, task.exception.toString(), Toast.LENGTH_SHORT).show()}
            }

    }

    private fun uploadImageToFirebase(item: PublicRunItem) {
        val database = Firebase.storage.reference
        val path = "${getImagePath()}${item.downloadUrl}"
        val storageRef = database.child(path)
        storageRef.putFile(fileUri!!)
            .addOnCompleteListener { task ->
                var msg = "Item Uploaded"
                if(!task.isSuccessful){msg = task.exception.toString()}
                Toast.makeText(parentActivity,msg, Toast.LENGTH_SHORT).show()
            }
    }

    private fun validUploadData():Boolean{
        return (Firebase.auth.currentUser!=null &&
                binding.cityText.text.toString().isNotEmpty() &&
                binding.streetText.text.toString().isNotEmpty() &&
                filePath != null &&
                fileUri != null)
    }
}