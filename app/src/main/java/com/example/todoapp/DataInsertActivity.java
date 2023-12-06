package com.example.todoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class DataInsertActivity extends AppCompatActivity {
    EditText insert_title, insert_description ;
    Button btn_insert ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_insert);

        insert_title = findViewById(R.id.insert_title);
        insert_description = findViewById(R.id.insert_description);
        btn_insert = findViewById(R.id.btn_insert);
        String type = getIntent().getStringExtra("type");
        if (type.equals("update")) {
            setTitle("update");
            insert_title.setText(getIntent().getStringExtra("title"));
            insert_description.setText(getIntent().getStringExtra("description"));
            int id = getIntent().getIntExtra("id",0);
            btn_insert.setText("update note");

            btn_insert.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent();
                    intent.putExtra("title", insert_title.getText().toString());
                    intent.putExtra("description", insert_description.getText().toString());
                    intent.putExtra("id",id);
                    setResult(RESULT_OK, intent);
                    finish();

                }
            });
        }
        else {

            setTitle("Add Mode");
        btn_insert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("title", insert_title.getText().toString());
                intent.putExtra("description", insert_description.getText().toString());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(DataInsertActivity.this, MainActivity.class));
    }
}