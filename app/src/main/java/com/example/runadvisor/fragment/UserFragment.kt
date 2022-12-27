package com.example.runadvisor.fragment
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.runadvisor.R
import com.example.runadvisor.activity.HomeActivity
import com.example.runadvisor.databinding.FragmentUserBinding
import com.example.runadvisor.enums.FragmentInstance
import com.example.runadvisor.interfaces.IFragment
import com.example.runadvisor.methods.*
import com.example.runadvisor.struct.MessageToUser
import com.example.runadvisor.widget.CustomDataAdapter
import com.example.runadvisor.widget.CustomImageButton
import com.google.firebase.storage.StorageReference

class UserFragment():
    Fragment(R.layout.fragment_user), IFragment {
    private lateinit var activityContext: Context
    private lateinit var parentActivity: HomeActivity
    private lateinit var recyclerView: RecyclerView
    private lateinit var customAdapter: CustomDataAdapter
    private lateinit var messageToUser: MessageToUser
    private var userView: View? = null
    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!
    private val GALLERY_REQUEST_CODE = 102
    private val PICK_IMAGE = 1
    private var fileUri: Uri? = null
    private var filePath:String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        if(userView!=null){return userView!!}
        _binding = FragmentUserBinding.inflate(inflater, container, false)
        userView = binding.root
        setParentActivity()
        setActivityContext()
        setRecyclerView()
        setAdapter()
        setEventListener(view)
        setInfoToUser()
        loadImageFromPhone()
        return userView!!
    }

    override fun getFragmentID(): FragmentInstance {
        return FragmentInstance.FRAGMENT_USER
    }

    override fun isRemovable():Boolean{
        return false
    }

    override fun receivedData(parameter: Any?){}

    override fun callbackDispatchTouchEvent(event: MotionEvent){}

    private fun setInfoToUser(){
        messageToUser = MessageToUser(parentActivity,null)
        messageToUser.setMessage("Do You Want To Sign Out?")
        messageToUser.setTwoButtons()
        messageToUser.setPositiveCallback(::signOut)
    }

    private fun setEventListener(view: View?){
        val userPicBtn = binding.userImageView
        val userSignOutBtn = binding.userSignOutBtn
        userSignOutBtn.setOnClickListener{messageToUser.showMessage()}
        userPicBtn.setCallback(null,::setProfilePicture)
    }

    private fun signOut(parameters:Any?){
        signOutUser()
    }

    private fun setProfilePicture(parameters:Any?){
        selectImageFromGallery(PICK_IMAGE)
    }

    private fun setActivityContext() {
        activityContext = requireContext()
    }

    private fun setParentActivity() {
        parentActivity = requireActivity() as HomeActivity
    }

    private fun setRecyclerView(){
        recyclerView = binding.userRecyclerview
        recyclerView.layoutManager = GridLayoutManager(activityContext,2)
        recyclerView.adapter = parentActivity.getAdapter()
        //recyclerView!!.adapter!!.notifyDataSetChanged()
    }

    private fun setAdapter(){
        //customAdapter = CustomDataAdapter(parentActivity)
        //recyclerView.adapter = customAdapter
    }

    /*
    *   ##########################################################################
    *                               LOAD DATA
    *   ##########################################################################
    * */

    /*private fun loadData(){
        parentActivity.firestoreViewModel.getRunItems().observe(parentActivity, Observer { it->
            if(it!=null){
                customAdapter.addRunItems(it)
            }
        })
    }*/



    /*
    *   ##########################################################################
    *                               LOAD IMAGES
    *   ##########################################################################
    * */

    private fun loadImageFromStorage(storeRef: StorageReference){
        //parentActivity.loadImageFromStorage(storeRef,binding.imageView)
    }

    private fun loadImageFromPhone(){
        val imgPath = parentActivity.retriveFromSharedPreference(getString(R.string.user_icon))
        if(imgPath!=null){
            parentActivity.loadUserIconFromPhone(imgPath,binding.userImageView)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE
            && resultCode == Activity.RESULT_OK
            && data != null
            && data.data != null
        ){
            filePath = parentActivity.getFilePathFromIntent(data)
            fileUri = data.data!!
            parentActivity.loadUserIconFromPhone(fileUri.toString(),binding.userImageView)
            parentActivity.writeToSharedPreference(getString(R.string.user_icon),fileUri.toString())
        }
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