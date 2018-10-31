package com.example.surfacejosh.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MafActivity extends AppCompatActivity {
    private Button Submit;

    public void CalcMafHr(int age, int weight, int height) {
        int mafhr = 180 - age;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maf);
        Submit = (Button) findViewById(R.id.SubmitMafweights);
        Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CalcMafHr(20, 200, 5);


            }
        });

    }
}