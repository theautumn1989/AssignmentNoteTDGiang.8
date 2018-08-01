package com.example.tomato.assignmentnotetdgiang.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;

import com.example.tomato.assignmentnotetdgiang.R;
import com.example.tomato.assignmentnotetdgiang.adapter.RecyclerMainAdapter;
import com.example.tomato.assignmentnotetdgiang.database.DBManager;
import com.example.tomato.assignmentnotetdgiang.model.Note;
import com.example.tomato.assignmentnotetdgiang.myInterface.OnCallBack;
import com.example.tomato.assignmentnotetdgiang.utils.AlarmReceiver;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class MainActivity extends AppCompatActivity implements OnCallBack, View.OnClickListener {



    RecyclerView rvNote;
    ImageButton ibtnAdd;

    ArrayList<Note> arrNote;
    RecyclerMainAdapter adapterNote;
    private AlarmReceiver alarmReceiver;
    private DBManager db;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initAdapter();
        initEvent();

        alarmReceiver = new AlarmReceiver();
        db = new DBManager(this);

        updateData();

    }
    @Override
    public void onResume() {
        super.onResume();
        updateData();
    }
    private void updateData(){
        arrNote = (ArrayList<Note>) db.getAllData();
        adapterNote = new RecyclerMainAdapter(arrNote, getApplicationContext(), this);
        rvNote.setAdapter(adapterNote);

    }

    private void initEvent() {
        ibtnAdd.setOnClickListener(this);
    }

    private void initAdapter() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        rvNote.setLayoutManager(gridLayoutManager);

        rvNote.setHasFixedSize(true);

        arrNote = new ArrayList<>();
        adapterNote = new RecyclerMainAdapter(arrNote, getApplicationContext(), this);
        rvNote.setAdapter(adapterNote);
    }

    private void initView() {
        rvNote = findViewById(R.id.rv_note);
        ibtnAdd = findViewById(R.id.ibtn_add);
    }



    // my interface
    @Override
    public void onItemClicked(int position, boolean isLongClick) {
        if (isLongClick) {    // longClick

        } else {
            Note note = arrNote.get(position);
            Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
            intent.putExtra(EditNoteActivity.EXTRA_NOTE_ID, note.getID());
            startActivity(intent);
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ibtn_add:
                Intent intent = new Intent(MainActivity.this, CreateNoteActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }


}
