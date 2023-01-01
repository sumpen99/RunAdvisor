package com.example.runadvisor.fragment
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.runadvisor.R
import com.example.runadvisor.MainActivity
import com.example.runadvisor.databinding.FragmentUserBinding
import com.example.runadvisor.enums.FragmentInstance
import com.example.runadvisor.interfaces.IFragment
import com.example.runadvisor.methods.*
import com.example.runadvisor.struct.MessageToUser


class UserFragment:Fragment(R.layout.fragment_user), IFragment {
    private lateinit var activityContext: Context
    private lateinit var parentActivity: MainActivity
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageToUser: MessageToUser
    private var userView: View? = null
    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!
    private val GALLERY_REQUEST_CODE = 102
    private val PICK_IMAGE = 1
    private var fileUri: Uri? = null
    private var filePath:String? = null
    private var userNameTag:String = ""
    private var userIconTag:String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        if(userView!=null){return userView!!}
        _binding = FragmentUserBinding.inflate(inflater, container, false)
        userView = binding.root
        setParentActivity()
        setActivityContext()
        setRecyclerView()
        setInfoToUser()
        setUserTag()
        loadImageFromPhone()
        loadUserName()
        return userView!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setEventListener(view)
    }

    override fun needDispatch():Boolean{return false}

    override fun getFragmentID(): FragmentInstance { return FragmentInstance.FRAGMENT_USER }

    override fun isRemovable():Boolean{ return false }

    override fun hasParentFragment(): FragmentInstance?{ return null}

    override fun receivedData(parameter: Any?){}

    override fun callbackDispatchTouchEvent(parameter:Any?){}

    private fun setActivityContext() { activityContext = requireContext() }

    private fun setParentActivity() { parentActivity = requireActivity() as MainActivity }

    private fun setUserTag(){
        userNameTag = parentActivity.getUserNameTag()
        userIconTag = parentActivity.getUserIconTag()
    }

    private fun setRecyclerView(){
        recyclerView = binding.userRecyclerview
        recyclerView.layoutManager = GridLayoutManager(activityContext,2)
        recyclerView.adapter = parentActivity.getUserAdapter()
    }

    private fun setInfoToUser(){
        messageToUser = MessageToUser(parentActivity,null)
        messageToUser.setMessage("Do You Want To Sign Out?")
        messageToUser.setTwoButtons()
        messageToUser.setPositiveCallback(::signOut)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setEventListener(view: View?){
        val userPicBtn = binding.userImageView
        val userSignOutBtn = binding.userSignOutBtn
        val userNameTextView = binding.userNameTextView
        //userNameTestView.hideKeyboard()
        userSignOutBtn.setOnClickListener{messageToUser.showMessage()}
        userPicBtn.setCallback(null,::setProfilePicture)
        requireView().setOnTouchListener { v, event ->
            when(event.actionMasked){
                MotionEvent.ACTION_DOWN -> {userNameTextView.hideKeyboard()}
            }
            true
        }
    }

    private fun signOut(parameters:Any?){
        parentActivity.cancelObservableUserData()
        parentActivity.cancelObservablePublicData()
        signOutUser()
    }

    private fun setProfilePicture(parameters:Any?){
        selectImageFromGallery(PICK_IMAGE)
    }

    private fun setUserName(){
        if(binding.userNameTextView.text.isEmpty()){return}
        parentActivity.writeToSharedPreference(userNameTag,binding.userNameTextView.text.toString())
    }

    private fun loadUserName(){
        binding.userNameTextView.hint = parentActivity.retrieveFromSharedPreference(userNameTag,"UserName").toString()
    }

    /*
    *   ##########################################################################
    *                               LOAD IMAGES
    *   ##########################################################################
    * */

    private fun loadImageFromPhone(){
        val imgPath = parentActivity.retrieveFromSharedPreference(userIconTag)
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
            parentActivity.writeToSharedPreference(userIconTag,fileUri.toString())
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
    }

    override fun onPause() {
        super.onPause()
        setUserName()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //_binding = null
    }

}