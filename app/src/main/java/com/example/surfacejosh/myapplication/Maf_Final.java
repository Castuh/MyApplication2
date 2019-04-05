package com.example.surfacejosh.myapplication;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.content.ServiceConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;

import android.widget.Switch;
import android.widget.TextView;

import static java.lang.Thread.sleep;


public class Maf_Final extends AppCompatActivity {
    private Button SpeedDown;
    private Button SpeedStop;
    private TextView MAF_HR;
    private TextView Actual_hr;
    private TextView StepCountView;
    private Button Speedup;
    //boolean displayhr = false;
    private ImageView CIRCLE;
    private Switch MAF_Switch;
    private TextView WORKOUT_START;
    int Stepcount;
    String speedvalue;
    int MAFHR;
    int mafhr2;
    int A_Hr = 60;
    int rangedown;
    int rangeup;
    //  TREADMILL  speedup/slowdown/ and stop or ( no speed change )
    String spdup = "2";
    String spdown = "5";
    String spdstop = "6";
    ////////////////////////
    Bundle extras;
    String MAFLOG = "";
    String MAF_HR_AND_LOG;
    String MAF_HR_STRING;
    double DUR_FROM_HR;
    int seconds = 0;
    int secondsdisplay = 0;
    int Mafworkoutstate = 0;
    int MafEndSecs1 = 300; // 300 seconds   Change to 10,10,10 to test each phase of workout
    int MafEndSecs2 = 600; //600
    int MafEndSecs3 = 300; //300
    //int hrdur;
    BluetoothTestService bts;


    private final ServiceConnection mServiceConnection = new ServiceConnection() {


        /**
         * This is called when the BluetoothTestService is connected
         *
         * @param componentName the component name of the service that has been connected
         * @param service service being bound
         */

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {

            bts = ((BluetoothTestService.LocalBinder) service).getService();

            bts.initialize();

        }


        /**
         * This is called when the BluetoothTestService is disconnected.
         *
         * @param componentName the component name of the service that has been connected
         */

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

            bts = null;

        }
    };


    public void CHANGE_HEART_SIZE() {//double DUR){
        DUR_FROM_HR = (1 / (((double) A_Hr / 60))) * 1000;
        //double dur = DUR;
        CIRCLE = (ImageView) findViewById(R.id.HR_CIRCLE);

        CIRCLE.getLayoutParams().height = 400;

        ObjectAnimator scaleDown = ObjectAnimator.ofPropertyValuesHolder(
                CIRCLE,
                PropertyValuesHolder.ofFloat("scaleX", 1.3f),
                PropertyValuesHolder.ofFloat("scaleY", 1.3f));
        scaleDown.setDuration((long) DUR_FROM_HR);//dur);

        scaleDown.setRepeatCount(ObjectAnimator.INFINITE);
        scaleDown.setRepeatMode(ObjectAnimator.REVERSE);

        scaleDown.start();

        CIRCLE.requestLayout();

    }

    public void doBindService() {
        Intent gattServiceIntent = new Intent(this, BluetoothTestService.class);

        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);


    }



    public void DISPLAY_MAF_HR(int hr) {

        MAF_HR_STRING = Integer.toString(hr);
        MAF_HR_AND_LOG = MAFLOG + MAF_HR_STRING;
        MAF_HR = (TextView) findViewById(R.id.MAF_HR_TV);
        MAF_HR.setText(MAF_HR_AND_LOG);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maf__final);
        doBindService();
        extras = getIntent().getExtras();
        Actual_hr = (TextView) findViewById(R.id.HR);
        StepCountView = (TextView) findViewById(R.id.StepCount);
        Speedup = (Button) findViewById(R.id.speedup);
        SpeedDown = (Button) findViewById(R.id.speeddown);
        SpeedStop = (Button) findViewById(R.id.speedstop);
        if (extras != null) {
            MAFHR = extras.getInt("MafHeartRate");
            mafhr2 = MAFHR;

        }
        final IntentFilter filter = new IntentFilter();
        filter.addAction(bts.ACTION_DATA_RECEIVED_FIT_TRACKER);
        Intent gattServiceIntent = new Intent(this, BluetoothTestService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        registerReceiver(mBleUpdateReceiver, filter);
        DISPLAY_MAF_HR(MAFHR);
        CHANGE_HEART_SIZE();//DUR_FROM_HR);
        WORKOUT_START = (TextView) findViewById(R.id.WORKOUT_START);
        MAF_Switch = (Switch) findViewById(R.id.MAF_Switch);
        Speedup.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(final View v) {
                                           bts.writeSpeedCharacteristic(spdup);
                                          WORKOUT_START.setText(spdup);
                                          bts.readStepCharacteristic();
                                       }
                                   });
        SpeedDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                bts.writeSpeedCharacteristic(spdown);
                WORKOUT_START.setText(spdown);
            }
        });
        SpeedStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                bts.writeSpeedCharacteristic(spdstop);
                WORKOUT_START.setText(spdstop);
            }
        });
        MAF_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean on) {

                if (on) {
                    HandlerThread handlerThread = new HandlerThread("handlerThread");
                    //WORKOUT_START.setText("Starting Workout");
                    //TODO FIX HANDLER NOT QUITTING OUT OF LOOPS  ::BUG::

                    handlerThread.start();
                    Looper looper = handlerThread.getLooper();
                    final Handler handler = new Handler(looper);
                   // Handler MAF_HANDLE = new Handler(Looper.getMainLooper());
                    WORKOUT_START.setText("EurickaA");

                    handler.post(new Runnable(){

                        @Override
                        public void run() {
                            WORKOUT_START.setText("Euricka");


                            // initial voltage
                           // Serial.write
                            if(MAF_Switch.isChecked() == true && Mafworkoutstate == 0) {
                                for (int t = 0; t <= MafEndSecs1; t++) {
                                    try {
                                        if (MAF_Switch.isChecked() == true) {
                                            if(seconds < 15){
                                                WORKOUT_START.setText("Warmup started");
                                            }
                                            sleep(1000);
                                            seconds++;
                                            secondsdisplay++;
                                            if(seconds % 4 == 0) {
                                                bts.writeSpeedCharacteristic("0");
                                            }
                                            else if(A_Hr > MAFHR){
                                                bts.writeSpeedCharacteristic(spdown);
                                                //WORKOUT_START.setText(spdown);
                                            }
                                            else if(A_Hr < MAFHR) {
                                                bts.writeSpeedCharacteristic(spdup);
                                                //WORKOUT_START.setText(spdup);
                                            }

                                        }

                                        if (MAF_Switch.isChecked() == false) {

                                            try {
                                                secondsdisplay = seconds;
                                                WORKOUT_START.setText("Methodology Stopped  prt 1 at: " + secondsdisplay + " Seconds");
                                                seconds = 0;
                                                Mafworkoutstate = 0;
                                                bts.writeSpeedCharacteristic(spdstop);
                                                WORKOUT_START.setText(spdstop);

                                                break;
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                        }
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                    try {
                                        if (MAF_Switch.isChecked() == true && seconds%10 == 0 && Mafworkoutstate == 0) {
                                            WORKOUT_START.setText("P1: 15 Seconds gone by: total seconds: " + (seconds));
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    if (seconds == MafEndSecs1){
                                        Mafworkoutstate++;
                                    }
                                }
                            }


                            if(MAF_Switch.isChecked() == true && Mafworkoutstate == 1) {
                                for (int t = 0; t <= MafEndSecs2; t++) {
                                    try {
                                        if (MAF_Switch.isChecked() == true) {

                                            sleep(1000);
                                            seconds++;
                                            secondsdisplay++;
                                            if(A_Hr < MAFHR) {
                                                bts.writeSpeedCharacteristic(spdup);
                                                WORKOUT_START.setText(spdup);
                                            }
                                            if(A_Hr > MAFHR) {
                                                bts.writeSpeedCharacteristic(spdown);
                                                WORKOUT_START.setText(spdown);
                                            }
                                        }

                                        if (MAF_Switch.isChecked() == false) {

                                            try {

                                                secondsdisplay = seconds;
                                                WORKOUT_START.setText("Methodology Stopped prt 2 at: " + secondsdisplay + " Seconds");
                                                seconds = 0;
                                                Mafworkoutstate = 0;
                                                bts.writeSpeedCharacteristic(spdstop);
                                                WORKOUT_START.setText(spdstop);
                                                break;
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                        }
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                    try {
                                        if (MAF_Switch.isChecked() == true && seconds%5 == 0 && Mafworkoutstate == 1) {
                                            WORKOUT_START.setText("P2: 5 Seconds gone by: total seconds: " + (seconds));
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    if (seconds - MafEndSecs1 == MafEndSecs2){
                                        Mafworkoutstate++;
                                    }
                                }
                            }

                                if (MAF_Switch.isChecked() == true && Mafworkoutstate == 2) {
                                    for (int t = 0; t <= MafEndSecs3; t++) {
                                        try {
                                            if (MAF_Switch.isChecked() == true) {
                                                //WORKOUT_START.setText("entering warmdown");
                                                sleep(1000);
                                                seconds++;
                                                secondsdisplay++;
                                                if(A_Hr > MAFHR) {
                                                    bts.writeSpeedCharacteristic(spdown);
                                                    WORKOUT_START.setText(spdown);
                                                }
                                            }

                                            if (MAF_Switch.isChecked() == false) {

                                                try {
                                                    secondsdisplay = seconds;
                                                    WORKOUT_START.setText("Methodology stopped prt3 at : " + secondsdisplay + " Seconds");
                                                    seconds = 0;
                                                    Mafworkoutstate = 0;
                                                    bts.writeSpeedCharacteristic(spdstop);
                                                    WORKOUT_START.setText(spdstop);
                                                    break;
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }

                                            }
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }

                                        try {
                                            if (MAF_Switch.isChecked() == true && seconds%15 == 0&& Mafworkoutstate == 2) {
                                                WORKOUT_START.setText("P3: 15 Seconds gone by: total seconds: " + (seconds));
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        if (seconds == MafEndSecs3+ MafEndSecs2 +MafEndSecs1){
                                            //TODO:: End workout.
                                            secondsdisplay = 0;
                                            seconds = 0;
                                            Mafworkoutstate = 0;
                                            bts.writeSpeedCharacteristic(spdstop);
                                            WORKOUT_START.setText(spdstop);
                                            WORKOUT_START.setText("END WORKOUT");
                                            break;
                                        }
                                    }

                                }
                            }
                    });

                }
            }
        });

    }

    private final BroadcastReceiver mBleUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {

                //case BluetoothTestService.ACTION_DISCONNECTED:
                    // Connecttracker.setEnabled(true);

                case BluetoothTestService.ACTION_DATA_RECEIVED_FIT_TRACKER:
                    // This is called after a notify or a read completes
                    //TODO: Change these actions states to Fit_Tracker and Treadmill Action broadcasts

                    //get heartrate
                    String hrvalue = bts.getCapSenseValue();
                    String stpvalue = bts.getStepValue();

                    Actual_hr.setText(hrvalue);
                    StepCountView.setText(stpvalue);
                    //A_Hr = Integer.parseInt(hrvalue);

                default:
                    break;


            }
        }
    };
}

