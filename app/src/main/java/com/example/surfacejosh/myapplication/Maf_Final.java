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

    //int hrdur;
    BluetoothTestService bts;
    //BluetoothTestServiceTread bts2;
    private HandlerThread handlerThread = new HandlerThread("handlerThread");
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
            //bts2 = ((BluetoothTestServiceTread.LocalBinder) service).getService();
            bts.initialize();
            //bts2.initialize();
        }


        /**
         * This is called when the BluetoothTestService is disconnected.
         *
         * @param componentName the component name of the service that has been connected
         */

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

            bts = null;
           // bts2 = null;
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
        //Intent gattServiceIntent2 = new Intent(this, BluetoothTestServiceTread.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
       // bindService(gattServiceIntent2, mServiceConnection, BIND_AUTO_CREATE);

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
        //Todo Still dont know if works
        //Intent gattServiceIntentTread = new Intent(this, BluetoothTestServiceTread.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        //TODO not sure if works
        //bindService(gattServiceIntentTread, mServiceConnection, BIND_AUTO_CREATE);
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

                            for(int t = 0; t < 20; t++){
                                try {
                                    sleep(15000);
                                    if(MAF_Switch.isChecked() == false) {

                                        try {
                                            WORKOUT_START.setText("Methodology Stopped");
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        handlerThread.quit();
                                    }
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
                    handlerThread.quitSafely();

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

