package com.example.runadvisor.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.runadvisor.R
import com.example.runadvisor.databinding.FragmentDataBinding
import com.example.runadvisor.enums.FragmentInstance
import com.example.runadvisor.enums.SortOperation
import com.example.runadvisor.interfaces.IFragment
import com.example.runadvisor.io.printToTerminal
import com.example.runadvisor.methods.*
import com.example.runadvisor.struct.RunItem
import com.example.runadvisor.widget.CustomDataAdapter
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.collections.ArrayList


class DataFragment(val removable:Boolean,val fragmentId:FragmentInstance):
    Fragment(R.layout.fragment_data), IFragment {
    private lateinit var activityContext: Context
    private lateinit var parentActivity: Activity
    private lateinit var recyclerView:RecyclerView
    private lateinit var customAdapter:CustomDataAdapter
    private var checkBoxes = ArrayList<CheckBox>()
    private var progressBar: ProgressBar? = null
    private var dataView:View? = null
    private var _binding: FragmentDataBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        //if(dataView!=null){loadData();return dataView!!}
        if(dataView!=null){return dataView!!}
        _binding = FragmentDataBinding.inflate(inflater, container, false)
        dataView = binding.root
        setParentActivity()
        setActivityContext()
        setRecyclerView()
        setAdapter()
        //setEventListener(view)
        //loadData()
        return dataView!!
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(progressBar == null){
            setEventListener(view)
            addProgressBar()
            //loadData()
        }
    }

    override fun getFragmentID(): FragmentInstance {
        return fragmentId
    }

    override fun isRemovable():Boolean{
        return removable
    }

    override fun receivedData(parameter: Any?){}

    override fun callbackDispatchTouchEvent(event: MotionEvent){}

    private fun setEventListener(view:View?){
        val sortOnRange = parentActivity.findViewById<CheckBox>(R.id.checkboxRange)
        val sortOnTrackLength = parentActivity.findViewById<CheckBox>(R.id.checkboxTrackLength)
        val sortOnCity = parentActivity.findViewById<CheckBox>(R.id.checkboxCity)
        checkBoxes.add(sortOnRange)
        checkBoxes.add(sortOnTrackLength)
        checkBoxes.add(sortOnCity)
        sortOnRange.setOnClickListener{sortDataSet(0,sortOnRange.isChecked,SortOperation.SORT_RANGE)}
        sortOnTrackLength.setOnClickListener{sortDataSet(1,sortOnTrackLength.isChecked,SortOperation.SORT_TRACK_LENGTH)}
        sortOnCity.setOnClickListener{sortDataSet(2,sortOnCity.isChecked,SortOperation.SORT_CITY)}
    }

    private fun sortDataSet(pos:Int,isChecked:Boolean,op: SortOperation){
        if(!isChecked){return}
        uncheckCheckBoxes(pos,checkBoxes)

    }

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
        customAdapter = CustomDataAdapter(parentActivity)
        recyclerView.adapter = customAdapter
    }

    private fun addProgressBar(){
        val layout = parentActivity.findViewById<ConstraintLayout>(R.id.dataLayout)
        progressBar = getProgressbar(parentActivity,layout)
    }

    private fun setProgressbar(show:Boolean){
        if(show){progressBar!!.visibility = View.VISIBLE
        }
        else{progressBar!!.visibility = View.GONE
        }
    }

    /*
    *   ##########################################################################
    *                               LOAD DATA
    *   ##########################################################################
    * */

    //https://medium.com/@deepak140596/firebase-firestore-using-view-models-and-livedata-f9a012233917

    private fun loadData(){
        viewLifecycleOwner.lifecycleScope.launch{
            setProgressbar(true)
            getDocument()
            setProgressbar(false)
        }
    }

    private suspend fun getDocument(){
        Firebase.firestore.collection(getItemCollection())
        .get()
        .addOnSuccessListener{documentSnapShot ->
            for(document in documentSnapShot.documents){
                if(!documentSnapShot.metadata.isFromCache){
                    val runItem = document.toObject<RunItem>()
                    if(runItem!=null){
                        customAdapter.serverData.add(runItem)
                        customAdapter.notifyItemInserted(customAdapter.serverData.size-1)
                    }
                }
            }
        }.await()
    }

    private fun loadImageFromStorage(storeRef: StorageReference){
        //parentActivity.loadImageFromStorage(storeRef,binding.imageView)
    }

    private fun loadImageFromPhone(imagePath:String){
        //parentActivity.loadImageFromPhone(imagePath,binding.imageView)
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

}