package com.example.runadvisor.fragment
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.runadvisor.R
import com.example.runadvisor.databinding.FragmentDataBinding
import com.example.runadvisor.enums.FragmentInstance
import com.example.runadvisor.interfaces.IFragment
import com.example.runadvisor.io.printPublicRunItem
import com.example.runadvisor.io.printToTerminal
import com.example.runadvisor.methods.*
import com.example.runadvisor.struct.PublicRunItem
import com.example.runadvisor.widget.CustomAdapter
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.util.*

class DataFragment: Fragment(R.layout.fragment_data), IFragment {
    private lateinit var activityContext: Context
    private lateinit var parentActivity: Activity
    private lateinit var recyclerView:RecyclerView
    private lateinit var customAdapter:CustomAdapter
    private var _binding: FragmentDataBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        _binding = FragmentDataBinding.inflate(inflater, container, false)
        val view: View = binding.root
        setParentActivity()
        setActivityContext()
        setRecyclerView()
        setAdapter()
        //setEventListener(view)
        loadData()
        return view
    }

    override fun getFragmentID(): FragmentInstance {
        return FragmentInstance.FRAGMENT_DATA
    }

    override fun processWork(parameter: Any?){}

    override fun callbackDispatchTouchEvent(event: MotionEvent){}

    private fun setActivityContext() {
        activityContext = requireContext()
    }

    private fun setParentActivity() {
        parentActivity = requireActivity()
    }

    private fun setRecyclerView(){
        recyclerView = binding.dataRecyclerview
        recyclerView.layoutManager = LinearLayoutManager(activityContext)
    }

    private fun setAdapter(){
        customAdapter = CustomAdapter(parentActivity)
        recyclerView.adapter = customAdapter
    }

    //https://stackoverflow.com/questions/31367599/how-to-update-recyclerview-adapter-data
    private fun loadData(){
        val docRef = Firebase.firestore.collection("PublicData")
        docRef.get().addOnSuccessListener { documentSnapShot->
            for(document in documentSnapShot.documents){
                val runItem = document.toObject<PublicRunItem>()
                if(runItem!=null){
                    customAdapter.serverData.add(runItem)
                    customAdapter.notifyItemInserted(customAdapter.serverData.size-1)
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setEventListener(view: View){

    }

    private fun downloadImageFromFirebase(item: PublicRunItem){
        val database = Firebase.storage.reference
        val path = "${getImagePath()}${item.downloadUrl}"
        val storageRef = database.child(path)
        loadImageFromStorage(storageRef)
    }

    private fun loadImageFromStorage(storeRef: StorageReference){
        //parentActivity.loadImageFromStorage(storeRef,binding.imageView)
    }

    private fun loadImageFromPhone(imagePath:String){
        //parentActivity.loadImageFromPhone(imagePath,binding.imageView)
    }

}