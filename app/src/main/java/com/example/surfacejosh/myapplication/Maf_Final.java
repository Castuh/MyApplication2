package com.example.surfacejosh.myapplication;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewDebug;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;


public class Maf_Final extends AppCompatActivity {
    private TextView MAF_HR;
    private ImageView CIRCLE;
    int REALTIMEHR = 1;
    int MAFHR;
    int mafhr2;
    Bundle extras;
    String MAFLOG = "Your Maf HR = ";
    String MAF_HR_AND_LOG;
    String MAF_HR_STRING;
    double DUR_FROM_HR;
    int hrdur;

    public void CHANGE_HEART_SIZE(double DUR){

        double dur = DUR;
        CIRCLE = (ImageView) findViewById(R.id.HR_CIRCLE);

        CIRCLE.getLayoutParams().height = 400;

        ObjectAnimator scaleDown = ObjectAnimator.ofPropertyValuesHolder(
                CIRCLE,
                PropertyValuesHolder.ofFloat("scaleX", 2f),
                PropertyValuesHolder.ofFloat("scaleY", 2f));
        scaleDown.setDuration((long)dur);

        scaleDown.setRepeatCount(ObjectAnimator.INFINITE);
        scaleDown.setRepeatMode(ObjectAnimator.REVERSE);

        scaleDown.start();

            CIRCLE.requestLayout();

    }
    //public void HR_BPM_DURATION(int mafhr){
       // MAFHR= mafhr;

       // DUR_FROM_HR = (1/((double)MAFHR/60)*1000);
        //return DUR_FROM_HR;
   // }
    public void DISPLAY_MAF_HR(int hr){
        DUR_FROM_HR = (1/((double)MAFHR/60)*1000);
        MAF_HR_STRING = Integer.toString(hr);
        MAF_HR_AND_LOG = MAFLOG + MAF_HR_STRING;
        MAF_HR = (TextView) findViewById(R.id.MAF_HR_TV);
        MAF_HR.setText(MAF_HR_AND_LOG);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maf__final);
        extras = getIntent().getExtras();
        if(extras != null){
            MAFHR = extras.getInt("MafHeartRate");
            mafhr2 = MAFHR;
        }
        DISPLAY_MAF_HR(MAFHR);
        CHANGE_HEART_SIZE(DUR_FROM_HR);
    }

}
