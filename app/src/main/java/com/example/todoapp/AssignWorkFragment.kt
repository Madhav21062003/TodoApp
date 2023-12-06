package com.example.todoapp

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.todoapp.api.ApiUtilities
import com.example.todoapp.databinding.FragmentAssignWorkBinding
import com.example.todoapp.models.Notification
import com.example.todoapp.models.NotificationData
import com.example.todoapp.models.Users
import com.example.todoapp.models.Works
import com.example.todoapp.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class AssignWorkFragment : Fragment() {

    val employeeData by navArgs<AssignWorkFragmentArgs>()
    private lateinit var binding: FragmentAssignWorkBinding
    private var priority = "1"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAssignWorkBinding.inflate(layoutInflater)

        binding.tbAssignWorks.apply {
            setNavigationOnClickListener {
                activity?.onBackPressed()
            }
            setPriority()
            setDate()
            binding.btnOne.setOnClickListener {
                assignWork()
            }
            return binding.root

        }

    }

    @SuppressLint("SuspiciousIndentation")
    private fun assignWork() {
        Utils.showDialog(requireContext())

        val workTitle = binding.etTitle.text.toString()
        val workDescription = binding.workDesc.text.toString()
        val workLastDate = binding.tvDate.text.toString()

        if (workTitle.isEmpty()){
            Utils.apply {
                hideDialog()
                showToast(requireContext(), "Pleas Enter the work title")
            }
        }

        else if (workLastDate == "Last Date: "){
            Utils.apply {
                hideDialog()
                showToast(requireContext(), "Please choose last date ")
            }
        }
        else {
            val empId = employeeData.employeeDetail.userId
            val bossId = FirebaseAuth.getInstance().currentUser?.uid
            val workRoom =  bossId + empId
            val randomId = (1..20).map { (('A'..'Z' ) + ('a'..'z') + ('0' .. '9')).random() }.joinToString { "" }
            val work = Works(bossId = bossId,workId = randomId, workTitle = workTitle, workDesc = workDescription, workPriority = priority,
                workLastDate = workLastDate, workStatus = "1")

                FirebaseDatabase.getInstance().getReference("Works").child(workRoom).child(randomId).setValue(work)
                    .addOnSuccessListener {
                        sendNotification(empId, workTitle)
                        Utils.hideDialog()
                        binding.etTitle.text.clear()
                        binding.workDesc.text.clear()
                        Utils.showToast(requireContext(), "Work has been assigned to ${employeeData.employeeDetail.userName}")
                        val action = AssignWorkFragmentDirections.actionAssignWorkFragmentToWorkFragment(employeeData.employeeDetail)
                        findNavController().navigate(action)
                    }
        }
    }

    private fun sendNotification(empId: String?, workTitle: String) {
        val empDataSnapshot = FirebaseDatabase.getInstance().getReference("Users").child(empId!!).get()
        empDataSnapshot.addOnSuccessListener {
            val empDetail = it.getValue(Users::class.java)
            val empToken = empDetail?.userToken
            val notification = Notification(empToken, NotificationData("Work Assigned", workTitle))
            ApiUtilities.api.sendNotification(notification).enqueue(object : Callback<Notification>{
                override fun onResponse(
                    call: Call<Notification>,
                    response: Response<Notification>
                ) {
                    if (response.isSuccessful){
                        Log.d("notify", "Notification send")
                    }
                }

                override fun onFailure(call: Call<Notification>, t: Throwable) {
                    TODO("Not yet implemented")
                }

            })
        }

    }

    private fun setDate() {
        val myCalendar = Calendar.getInstance()
        val  datePicker = DatePickerDialog.OnDateSetListener{view, year, month,dayOfMonth ->
            myCalendar.apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateLable(myCalendar)
            }
        }

        binding.datePicker.setOnClickListener {
            DatePickerDialog(requireContext(), datePicker, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun updateLable(myCalender: Calendar){
        val myFormat = "dd-MM-yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.UK)
        binding.tvDate.text = sdf.format(myCalender.time)
    }



    private fun setPriority() {
        binding.apply {
            greenOval.setOnClickListener {
                Utils.showToast(requireContext(), "Priority: LOW")
                priority = "1"
                binding.greenOval.setImageResource(R.drawable.baseline_done_24)
                binding.redOval.setImageResource(0)
                binding.yellowOval.setImageResource(0)
            }

            yellowOval.setOnClickListener {
                Utils.showToast(requireContext(), "Priority: Medium")
                priority = "2"
                binding.greenOval.setImageResource(0)
                binding.redOval.setImageResource(0)
                binding.yellowOval.setImageResource(R.drawable.baseline_done_24)
            }

            redOval.setOnClickListener {
                Utils.showToast(requireContext(), "Priority: High")
                priority = "3"
                binding.greenOval.setImageResource(0)
                binding.redOval.setImageResource(R.drawable.baseline_done_24)
                binding.yellowOval.setImageResource(0)
            }
        }


    }

}