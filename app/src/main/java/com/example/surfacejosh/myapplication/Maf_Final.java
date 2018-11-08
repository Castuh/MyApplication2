package com.example.surfacejosh.myapplication;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
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
    private Drawable CIRCLE_DRAWABLE;
    int MAFHR = 0;
    String MAFLOG = "Your Maf HR = ";
    String MAF_HR_AND_LOG;
    String MAF_HR_STRING;
    int CIRCLE_HEIGHT = 500;
    //int CIRCLE_WIDTH;
    int COUNT;
    int DUR_FROM_HR;

    public void CHANGE_CIRCLE_SIZE(){
        //setContentView(R.id.HR_CIRCLE);
        CIRCLE = (ImageView) findViewById(R.id.HR_CIRCLE);
        //CIRCLE_HEIGHT = CIRCLE.getLayoutParams().height;
        CIRCLE.getLayoutParams().height = 500;

        ObjectAnimator scaleDown = ObjectAnimator.ofPropertyValuesHolder(
                CIRCLE,
                PropertyValuesHolder.ofFloat("scaleX", 2f),
                PropertyValuesHolder.ofFloat("scaleY", 2f));
        scaleDown.setDuration(233);

        scaleDown.setRepeatCount(ObjectAnimator.INFINITE);
        scaleDown.setRepeatMode(ObjectAnimator.REVERSE);

        scaleDown.start();
        //CIRCLE_WIDTH = CIRCLE.getLayoutParams().width;
        //CIRCLE_DRAWABLE = (Drawable) findViewById(R.id.HR_CIRCLE);
//        for (int i = 0; i <= 999999990; i++) {
//            if (COUNT <= 20000) {
//                CIRCLE_HEIGHT++;
//                //CIRCLE_WIDTH++;
//                if (CIRCLE_HEIGHT <= 1000) {
//                    COUNT = 40000;
//                }
//
//            }
//            if (COUNT >= 20000) {
//                CIRCLE_HEIGHT--;
//                // CIRCLE_WIDTH--;
//                if (CIRCLE_HEIGHT >= 1000) {
//                    COUNT = 0;
//                }
//            }
            //LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(CIRCLE_WIDTH,CIRCLE_HEIGHT);
            //CIRCLE.setLayoutParams(params);
            //CIRCLE.getLayoutParams().height = 500;

            CIRCLE.requestLayout();
            COUNT++;
        //}
    }
    public int CONVERT_HR_BPM_DURATION(){


        return DUR_FROM_HR;
    }
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

       // while(true) {
            DISPLAY_MAF_HR();
            CHANGE_CIRCLE_SIZE();
      //  }


    }

}
