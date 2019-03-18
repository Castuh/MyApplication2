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
import android.widget.CompoundButton;
import android.widget.ImageView;

import android.widget.Switch;
import android.widget.TextView;

import static java.lang.Thread.sleep;


public class Maf_Final extends AppCompatActivity {
    private TextView MAF_HR;
    private TextView Actual_hr;
    private TextView StepCountView;
    //boolean displayhr = false;
    private ImageView CIRCLE;
    private Switch MAF_Switch;
    private TextView WORKOUT_START;
    int Stepcount;
    int MAFHR;
    int mafhr2;
    int A_Hr = 60;
    double speed = 2.0;
    Bundle extras;
    String MAFLOG = "";
    String MAF_HR_AND_LOG;
    String MAF_HR_STRING;
    double DUR_FROM_HR;
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
                PropertyValuesHolder.ofFloat("scaleX", 2f),
                PropertyValuesHolder.ofFloat("scaleY", 2f));
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

    /*public void MAFRUN() {
        float timeframe = System.currentTimeMillis();
        long runtime = 300000;

        // initial voltage

        while (timeframe < timeframe + runtime) {
            // send voltage increase
            try {
                sleep(15000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            WORKOUT_START.setText("15 Seconds");
        }


        timeframe = System.currentTimeMillis();
        runtime = 600000;

        while (timeframe < timeframe + runtime) {
            if (A_Hr < MAFHR) {
                //increase voltage
            }
            if (A_Hr > MAFHR) {
                //decrease voltage
            }
            try {
                sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            WORKOUT_START.setText("5 Seconds");
        }


        timeframe = System.currentTimeMillis();
        runtime = 300000;

        while (timeframe < timeframe + runtime) {
            // send voltage decrease
            try {
                sleep(15000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            WORKOUT_START.setText("15 Seconds");
        }
    }*/


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
        if (extras != null) {
            MAFHR = extras.getInt("MafHeartRate");
            mafhr2 = MAFHR;

        }
        final IntentFilter filter = new IntentFilter();
        filter.addAction(bts.ACTION_DATA_RECEIVED);
        Intent gattServiceIntent = new Intent(this, BluetoothTestService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        registerReceiver(mBleUpdateReceiver, filter);
        DISPLAY_MAF_HR(MAFHR);
        CHANGE_HEART_SIZE();//DUR_FROM_HR);

        WORKOUT_START = (TextView) findViewById(R.id.WORKOUT_START);
        MAF_Switch = (Switch) findViewById(R.id.MAF_Switch);

        MAF_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean on) {
                if (on) {
                    //WORKOUT_START.setText("Starting Workout");

                    HandlerThread handlerThread = new HandlerThread("MyMafThread");
                    handlerThread.start();
                    Looper looper = handlerThread.getLooper();
                    Handler handler = new Handler(looper);
                   // Handler MAF_HANDLE = new Handler(Looper.getMainLooper());
                    WORKOUT_START.setText("EurickaA");

                    handler.post(new Runnable(){

                        @Override
                        public void run() {
                            WORKOUT_START.setText("Euricka");


                            // initial voltage
                           // Serial.write

                            for(int t = 0; t < 20; t++){
                                try {
                                    sleep(15000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                try {
                                    WORKOUT_START.setText("15 Seconds");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            /*while (timeframe < period) {
                                // send voltage increase
                                try {
                                    sleep(15000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                WORKOUT_START.setText("15 Seconds");
                            }*/

                            for(int t = 0; t < 120; t++){
                                try {
                                    sleep(5000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                try {
                                    WORKOUT_START.setText("5 Seconds");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }


                            /*while (timeframe < timeframe + runtime) {
                                if (A_Hr < MAFHR) {
                                    //increase voltage
                                }
                                if (A_Hr > MAFHR) {
                                    //decrease voltage
                                }
                                try {
                                    sleep(5000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                WORKOUT_START.setText("5 Seconds");
                            }*/


                            for(int t = 0; t < 20; t++){
                                try {
                                    sleep(15000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                try {
                                    WORKOUT_START.setText("15 Seconds");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            /*while (timeframe < timeframe + runtime) {
                                // send voltage decrease
                                try {
                                    sleep(15000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                WORKOUT_START.setText("15 Seconds");
                            }*/


                        }
                    });

                } else {
                    WORKOUT_START.setText("stop");

                }
            }
        });

    }

    private final BroadcastReceiver mBleUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {

                case BluetoothTestService.ACTION_DISCONNECTED:
                    // Connecttracker.setEnabled(true);

                case BluetoothTestService.ACTION_DATA_RECEIVED:
                    // This is called after a notify or a read completes

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

