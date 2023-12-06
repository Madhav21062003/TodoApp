package com.example.todoapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.todoapp.EmployessFragmentDirections
import com.example.todoapp.models.Users
import com.example.todoapp.databinding.ItemViewEmployeesProfileBinding

class EmployeesAdapter : RecyclerView.Adapter<EmployeesAdapter.EmployeesVieholder>() {

    class  EmployeesVieholder(val binding: ItemViewEmployeesProfileBinding) : ViewHolder(binding.root)

    val diffUtil = object : DiffUtil.ItemCallback<Users>(){
        override fun areItemsTheSame(oldItem: Users, newItem: Users): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Users, newItem: Users): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployeesAdapter.EmployeesVieholder {
        return EmployeesVieholder(ItemViewEmployeesProfileBinding.inflate(LayoutInflater
            .from(parent.context),parent, false))
    }

    override fun onBindViewHolder(holder: EmployeesAdapter.EmployeesVieholder, position: Int) {
        val empData = differ.currentList[position]
        holder.binding.apply {

            Glide.with(holder.itemView).load(empData.userImage).into(ivEmployeeProfile)
            tvEmployeeName.text = empData.userName
        }
        holder.itemView.setOnClickListener {
            val action = EmployessFragmentDirections.actionEmployessFragmentToWorkFragment(empData)
            Navigation.findNavController(it).navigate(action)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }


}