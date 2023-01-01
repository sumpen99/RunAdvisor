package com.example.runadvisor.fragment
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.runadvisor.R
import com.example.runadvisor.MainActivity
import com.example.runadvisor.databinding.FragmentDataBinding
import com.example.runadvisor.enums.FragmentInstance
import com.example.runadvisor.enums.SortOperation
import com.example.runadvisor.interfaces.IFragment
import com.example.runadvisor.methods.*
import kotlin.collections.ArrayList


class DataFragment:
    Fragment(R.layout.fragment_data), IFragment {
    private lateinit var activityContext: Context
    private lateinit var parentActivity: MainActivity
    private lateinit var recyclerView:RecyclerView
    private var checkBoxes = ArrayList<CheckBox>()
    private var dataView:View? = null
    private var _binding: FragmentDataBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        if(dataView!=null){return dataView!!}
        _binding = FragmentDataBinding.inflate(inflater, container, false)
        dataView = binding.root
        setParentActivity()
        setActivityContext()
        setRecyclerView()
        setEventListener()
        return dataView!!
    }

    override fun getFragmentID(): FragmentInstance {
        return FragmentInstance.FRAGMENT_DATA
    }

    override fun isRemovable():Boolean{
        return false
    }

    override fun needDispatch():Boolean{return false}

    override fun receivedData(parameter: Any?){}

    override fun callbackDispatchTouchEvent(parameter:Any?){}

    private fun setEventListener(){
        if(checkBoxes.isNotEmpty()){return}
        val sortOnRange = binding.checkboxRange
        val sortOnCity = binding.checkboxCity
        checkBoxes.add(sortOnRange)
        checkBoxes.add(sortOnCity)
        sortOnRange.setOnClickListener{sortDataSet(0,sortOnRange.isChecked,SortOperation.SORT_RANGE)}
        sortOnCity.setOnClickListener{sortDataSet(1,sortOnCity.isChecked,SortOperation.SORT_CITY)}
    }

    private fun setActivityContext() {
        activityContext = requireContext()
    }

    private fun setParentActivity() {
        parentActivity = requireActivity() as MainActivity
    }

    private fun setRecyclerView(){
        recyclerView = binding.dataRecyclerview
        recyclerView.layoutManager = LinearLayoutManager(activityContext)
        recyclerView.adapter = parentActivity.getPublicAdapter()
    }

    /*
    *   ##########################################################################
    *                         UNCHECK CHECKBOXES AND SORT
    *   ##########################################################################
    * */

    private fun sortDataSet(pos:Int,isChecked:Boolean,op: SortOperation){
        if(!isChecked){return}
        uncheckCheckBoxes(pos,checkBoxes)

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