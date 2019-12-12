package com.example.gmapdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    EditText L1,L2;
    Button Route;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        L2 = findViewById(R.id.location2);
        Route = findViewById(R.id.show_route_btn);
        Route.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v == Route)
        {
            if(!L2.getText().toString().isEmpty()) {

                //Toast.makeText(this, location2,Toast.LENGTH_LONG ).show();
                Intent i = new Intent(this, Map.class);
                i.putExtra("L2",L2.getText().toString());
                startActivity(i);
                finish();
            }
        }
    }
}
