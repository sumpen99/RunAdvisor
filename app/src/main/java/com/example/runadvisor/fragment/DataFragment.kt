package com.example.runadvisor.fragment
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.runadvisor.R
import com.example.runadvisor.activity.HomeActivity
import com.example.runadvisor.databinding.FragmentDataBinding
import com.example.runadvisor.enums.FragmentInstance
import com.example.runadvisor.enums.SortOperation
import com.example.runadvisor.interfaces.IFragment
import com.example.runadvisor.methods.*
import com.example.runadvisor.widget.CustomDataAdapter
import com.google.firebase.storage.StorageReference
import kotlin.collections.ArrayList


class DataFragment():
    Fragment(R.layout.fragment_data), IFragment {
    private lateinit var activityContext: Context
    private lateinit var parentActivity: HomeActivity
    private lateinit var recyclerView:RecyclerView
    private lateinit var customAdapter:CustomDataAdapter
    private var checkBoxes = ArrayList<CheckBox>()
    private var dataView:View? = null
    private var _binding: FragmentDataBinding? = null
    private val binding get() = _binding!!
    private var firstInit = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        if(dataView!=null){return dataView!!}
        _binding = FragmentDataBinding.inflate(inflater, container, false)
        dataView = binding.root
        setParentActivity()
        setActivityContext()
        setRecyclerView()
        setAdapter()
        return dataView!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(firstInit){
            firstInit = false
            setEventListener(view)
        }
    }

    override fun getFragmentID(): FragmentInstance {
        return FragmentInstance.FRAGMENT_DATA
    }

    override fun isRemovable():Boolean{
        return false
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

    private fun setActivityContext() {
        activityContext = requireContext()
    }

    private fun setParentActivity() {
        parentActivity = requireActivity() as HomeActivity
    }

    private fun setRecyclerView(){
        recyclerView = binding.dataRecyclerview
        recyclerView.layoutManager = LinearLayoutManager(activityContext)
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
    *                               SORT DATA BY CLOSEST TO USER
    *   ##########################################################################
    * */

    private fun sortDataSet(pos:Int,isChecked:Boolean,op: SortOperation){
        if(!isChecked){return}
        uncheckCheckBoxes(pos,checkBoxes)

    }

    /*
    *   ##########################################################################
    *                               LOAD IMAGES
    *   ##########################################################################
    * */

    private fun loadImageFromStorage(storeRef: StorageReference){
        //parentActivity.loadImageFromStorage(storeRef,binding.imageView)
    }

    private fun loadImageFromPhone(imagePath:String){
        //parentActivity.loadImageFromPhone(imagePath,binding.imageView)
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