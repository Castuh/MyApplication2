package com.example.surfacejosh.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.w3c.dom.Text;

public class MafActivity extends AppCompatActivity {
    private Button Submit;
    private Button Debug;
    TextInputEditText TIE;
    TextInputEditText TIEW;
    Bundle extras;
    static int Maf_HR;
    //public BluetoothTestService bts1;
    int age = 0;
    int weight = 0;
    private static final String SHARED_PREFS = "sharedprefs";
    private static final String AGE = "0";
    private static final String WEIGHT = "1";
    private String ages;
    private String weights;
    private Boolean Debugmode = false;


   // public static int getMAFHR(){
     //   return Maf_HR;
    //}
    public void OpenMaf_Final(int hr){
        saveData();
        Intent intent = new Intent(MafActivity.this, Maf_Final.class);
        intent.putExtra("MafHeartRate",hr);
        intent.putExtra("mah",hr);
        intent.putExtra("Weight",weight);
        intent.putExtra("Age",age);
        intent.putExtra("Debug",Debugmode);
        //intent.putExtra("bts1", (Parcelable) bts1);
        startActivity(intent);
    }
    public int CalcMafHr() {
        //TIE = (TextInputEditText) findViewById(R.id.EnterAge);
        String AgeString = TIE.getText().toString();
        age = Integer.parseInt(AgeString);
        String WeightString = TIEW.getText().toString();
        weight = Integer.parseInt(WeightString);
        return 180 - age;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maf);
        Submit = (Button) findViewById(R.id.SubmitMafweights);
        Debug = (ToggleButton) findViewById(R.id.Debug_Toggle);
        TIE = (TextInputEditText) findViewById(R.id.EnterAge);
        TIEW = (TextInputEditText) findViewById(R.id.EnterWeight);

        Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Maf_HR = CalcMafHr();
                OpenMaf_Final(Maf_HR);

            }
        });
        Debug.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if( Debugmode == false ){
                    Debugmode = true;
                } else {
                    Debugmode = false;
                }
            }

                                 }
                );

        loadData();
        updateViews();


    }
    public void saveData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(AGE, TIE.getText().toString());
        editor.putString(WEIGHT,TIEW.getText().toString());
        editor.apply();

    }
    public void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        ages = sharedPreferences.getString(AGE,"0");
        weights = sharedPreferences.getString(WEIGHT,"0");
    }
    public void updateViews(){
        TIE.setText(ages);
        TIEW.setText(weights);
    }

}