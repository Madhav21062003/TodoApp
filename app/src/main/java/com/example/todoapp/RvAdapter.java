package com.example.todoapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todoapp.databinding.EachRvBinding;

public class RvAdapter extends ListAdapter<Note, RvAdapter.MyViewHolder> {

    public RvAdapter(){
        super(CALLBACK);
    }
private static final DiffUtil.ItemCallback<Note> CALLBACK = new DiffUtil.ItemCallback<Note>() {
    @Override
    public boolean areItemsTheSame(@NonNull Note oldItem, @NonNull Note newItem) {
        return oldItem.getId() == newItem.getId();
    }

    @Override
    public boolean areContentsTheSame(@NonNull Note oldItem, @NonNull Note newItem) {
        return oldItem.getTitle().equals(newItem.getTitle()) && oldItem.getDescription().equals(newItem.getDescription());
    }
};
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.each_rv,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Note note = getItem(position);
        holder.binding.noteTitle.setText(note.getTitle());
        holder.binding.noteDescription.setText(note.getDescription());
    }

    public Note getNote(int position){
            return getItem(position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        EachRvBinding binding ;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            binding = EachRvBinding.bind(itemView);
        }
    }
}
