package com.example.surfacejosh.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MafActivity extends AppCompatActivity {
    private Button Submit;
    TextInputEditText TIE;
    Bundle extras;
    static int Maf_HR;
    //public BluetoothTestService bts1;
    int age = 0;

   // public static int getMAFHR(){
     //   return Maf_HR;
    //}
    public void OpenMaf_Final(int hr){

        Intent intent = new Intent(MafActivity.this, Maf_Final.class);
        intent.putExtra("MafHeartRate",hr);
        intent.putExtra("mah",hr);
        //intent.putExtra("bts1", (Parcelable) bts1);
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

        /*else if(Integer.parseInt(TIE.getText().toString()) >= 0 && Integer.parseInt(TIE.getText().toString()) <= 120)
        {*/

        //}


    }

}