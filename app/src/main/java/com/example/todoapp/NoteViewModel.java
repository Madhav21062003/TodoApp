package com.example.todoapp;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class NoteViewModel extends AndroidViewModel {

    private NoteRepository noteRepository ;
    private LiveData<List<Note>> noteList ;

    public NoteViewModel(@NonNull Application application) {
        super(application);

        noteRepository = new NoteRepository(application);
        noteList = noteRepository.getAllData();
    }

    public void insert(Note note){
        noteRepository.insertData(note);
    }
    public void update(Note note){
        noteRepository.updateData(note);
    }
    public void delete(Note note){
        noteRepository.deleteData(note);
    }

    public LiveData<List<Note>> getAllNotes(){
        return noteList;
    }
}
