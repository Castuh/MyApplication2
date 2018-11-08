package com.example.surfacejosh.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewDebug;
import android.widget.TextView;


public class Maf_Final extends AppCompatActivity {
    private TextView MAF_HR;
    int MAFHR = 0;
    String MAFLOG = "Your Maf HR = ";
    String MAF_HR_AND_LOG;
    String MAF_HR_STRING;


    public void DISPLAY_MAF_HR(){

        MafActivity MAF = ((MafActivity)getApplicationContext());
        MAFHR = MAF.getMafHR();
        MAF_HR_STRING = Integer.toString(MAFHR);
        MAF_HR_AND_LOG = MAFLOG + MAF_HR_STRING;
        MAF_HR = (TextView) findViewById(R.id.MAF_HR_TV);
        MAF_HR.setText(MAF_HR_AND_LOG);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maf__final);
        DISPLAY_MAF_HR();
    }
}
