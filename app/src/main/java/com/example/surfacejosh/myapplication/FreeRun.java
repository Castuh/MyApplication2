package com.example.surfacejosh.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class FreeRun extends AppCompatActivity {
    private Button HIT;
    private Button long_distance;

    public void OpenHIT_Final(){

        Intent intent = new Intent(FreeRun.this, HIT_Final.class);
        startActivity(intent);
    }

    public void OpenLD_Final(){

        Intent intent = new Intent(FreeRun.this, LD_Final.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.freerun);
        HIT = (Button) findViewById(R.id.HIT);
        long_distance = (Button) findViewById(R.id.long_distance);

        HIT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               OpenHIT_Final();
            }
        });
        long_distance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenLD_Final();
            }
        });
    }
}
