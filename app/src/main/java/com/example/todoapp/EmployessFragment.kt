package com.example.todoapp

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todoapp.adapters.EmployeesAdapter
import com.example.todoapp.auth.LoginActivity
import com.example.todoapp.databinding.FragmentEmployessBinding
import com.example.todoapp.models.Users
import com.example.todoapp.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters,

class EmployessFragment : Fragment() {

    private lateinit var binding: FragmentEmployessBinding
    private lateinit var employeeAdapter: EmployeesAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentEmployessBinding.inflate(layoutInflater)


        binding.apply {
            tbEmployees.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.logout -> {
                        showLogoutDialog()
                        true
                    }

                    else  -> false

                }
            }

        }

       prepareRvForEmployeeAdapter()
        showAllEmployees()
        return binding.root
    }

    private fun showAllEmployees() {
        Utils.showDialog(requireContext())
        FirebaseDatabase.getInstance().getReference("Users").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val empList = arrayListOf<Users>()

                for (employees in snapshot.children){
                    val currentUser = employees.getValue(Users::class.java)
                    if (currentUser?.userType == "Employee"){
                        empList.add(currentUser)
                    }
                }
                employeeAdapter.differ.submitList(empList)
                Utils.hideDialog()
            }

            override fun onCancelled(error: DatabaseError) {
                Utils.apply {
                    hideDialog()
                    showToast(requireContext(), error.message)
                }
            }

        })
    }

    private fun prepareRvForEmployeeAdapter() {
        employeeAdapter = EmployeesAdapter()
        binding.rvEmployees.apply {
            layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
            adapter = employeeAdapter
        }
    }

    private fun showLogoutDialog() {
        val  builder = AlertDialog.Builder(requireContext())
        val alertDialog = builder.create()
        alertDialog.setCancelable(false)
        builder.setTitle("Log Out")
            .setMessage("Are you sure you want to logout")
            .setPositiveButton("Yes"){_, _->
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(requireContext(), LoginActivity::class.java))
                requireActivity().finish()
            }
            .setNegativeButton("No"){_, _->
                alertDialog.dismiss()
            }
            .show()
    }


}
