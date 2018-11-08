package com.example.surfacejosh.myapplication;

import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.w3c.dom.Text;

public class MafActivity extends AppCompatActivity {
    private Button Submit;
    TextInputEditText TIE;
    static int Maf_HR;
    int age = 0;
    public static int getMafHR(){
        return Maf_HR;
    }
    public void OpenMaf_Final(){
        Intent intent = new Intent(this, Maf_Final.class);
        startActivity(intent);
    }
    public void CalcMafHr() {
        TIE = (TextInputEditText) findViewById(R.id.EnterAge);
        String AgeString = TIE.getText().toString();
        age = Integer.parseInt(AgeString);
        Maf_HR = 180 - age;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maf);
        Submit = (Button) findViewById(R.id.SubmitMafweights);
        Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CalcMafHr();
                OpenMaf_Final();

            }
        });

    }
}