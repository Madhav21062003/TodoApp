package com.example.todoapp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todoapp.adapters.WorksAdapter
import com.example.todoapp.databinding.FragmentWorkBinding
import com.example.todoapp.models.Works
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class WorkFragment : Fragment() {

    private lateinit var binding: FragmentWorkBinding
    private lateinit var worksAdapter: WorksAdapter
    val employeeDetail by navArgs<WorkFragmentArgs>()
    private lateinit var workRoom: String

    @SuppressLint("SuspiciousIndentation")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentWorkBinding.inflate(layoutInflater)
        val empDataToPass = employeeDetail.employeeData
        binding.fabAssignWork.setOnClickListener {
            val action =
                WorkFragmentDirections.actionWorkFragmentToAssignWorkFragment(employeeDetail.employeeData)
            findNavController().navigate(action)
        }

        val empName = employeeDetail.employeeData.userName
        // Set the title directly
        binding.tbEmpWorks.apply {
            title = empName
            setNavigationOnClickListener {
                activity?.onBackPressed()
            }
        }
        prepareForRvWorksAdapter()
        showAllWorks()
        return binding.root
    }

    private fun showAllWorks() {
        val bossId = FirebaseAuth.getInstance().currentUser?.uid
        workRoom = bossId + employeeDetail.employeeData.userId

        FirebaseDatabase.getInstance().getReference("Works").child(workRoom)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val workList = ArrayList<Works>()
                    for (allWorks in snapshot.children) {
                        val work = allWorks.getValue(Works::class.java)
                        workList.add(work!!)
                    }
                    worksAdapter.differ.submitList(workList)
                    com.example.todoapp.utils.Utils.hideDialog()
                    binding.tvText.visibility = if (workList.isEmpty()) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    @SuppressLint("SuspiciousIndentation")
    private fun prepareForRvWorksAdapter() {
        worksAdapter = WorksAdapter(::onUnassignedButtonClicked)

        binding.rvWorks.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = worksAdapter
        }
    }

    fun onUnassignedButtonClicked(works: Works) {
        val builder = AlertDialog.Builder(requireContext())
        val alertDialog = builder.create()
        alertDialog.setCancelable(false)
        builder.setTitle("Unassigned Work")
            .setMessage("Are you sure you want to Unassigned this work")
            .setPositiveButton("Yes") { _, _ ->
                unassignedWork(works)
            }
            .setNegativeButton("No") { _, _ ->
                alertDialog.dismiss()
            }
            .show()
    }

    private fun unassignedWork(works: Works) {

        works.expanded = !works.expanded
        FirebaseDatabase.getInstance().getReference("Works").child(workRoom)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (allWorks in snapshot.children) {
                        val currentWork = allWorks.getValue(Works::class.java)
                        if (currentWork == works) {
                            allWorks.ref.removeValue()
                            com.example.todoapp.utils.Utils.showToast(
                                requireContext(),
                                "Work has been Unassigned"
                            )
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }


}
