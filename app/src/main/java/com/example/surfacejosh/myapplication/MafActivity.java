package com.example.surfacejosh.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
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
   // public static int getMAFHR(){
     //   return Maf_HR;
    //}
    public void OpenMaf_Final(int hr){

        Intent intent = new Intent(MafActivity.this, Maf_Final.class);
        intent.putExtra("MafHeartRate",hr);
        intent.putExtra("mah",hr);
        startActivity(intent);
    }
    public int CalcMafHr() {
        TIE = (TextInputEditText) findViewById(R.id.EnterAge);
        String AgeString = TIE.getText().toString();
        age = Integer.parseInt(AgeString);
        return 180 - age;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maf);
        Submit = (Button) findViewById(R.id.SubmitMafweights);
        Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Maf_HR = CalcMafHr();
                OpenMaf_Final(Maf_HR);

            }
        });

    }

}