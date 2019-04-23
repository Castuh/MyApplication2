


package com.example.surfacejosh.myapplication;
import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import java.util.ArrayList;
import android.widget.Switch;
import android.widget.TextView;


public class SeniorProject extends AppCompatActivity { //implements AdapterView.OnItemClickListener {

    private static final String TAG = "SeniorProject";
    private Button Connecttracker;
    //Create UI elements and lib variables
    private TextView myLabel;
    private TextView hrLabel;
    private Button MafWorkout;
    private Button FreeRun;
    private Button TreadMillConnect;
    private Boolean IsFitConnected = false;
    private Boolean IsTreadConnected = false;
    View v;
    // Variables to manage BLE connection
    private static boolean mConnectState;
    private static boolean mServiceConnected;
    private static BluetoothTestService mBluetoothTestService;
    private static final int REQUEST_ENABLE_BLE = 1;
    // Keep track of whether hr Notifications are on or off

    //This is required for Android 6.0 (Marshmallow)
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();




    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        //Basically binding the service to the main activity

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.i(TAG, "onServiceConnected");
            mBluetoothTestService = ((BluetoothTestService.LocalBinder) service).getService();
            mServiceConnected = true;
            mBluetoothTestService.initialize();
            //mBluetoothTestServiceTread.initialize();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(TAG, "onServiceDisconnected");
            mBluetoothTestService = null;
            //mBluetoothTestServiceTread = null;
        }
    };


    @TargetApi(Build.VERSION_CODES.M) // This is required for Android 6.0 (Marshmallow) to work
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_senior_project);

        MafWorkout =  findViewById(R.id.MAF_WORKOUT);
        FreeRun = findViewById(R.id.FREE_RUN);

        myLabel = (TextView) findViewById(R.id.TextV);
        hrLabel = (TextView) findViewById(R.id.hr_view);

        TreadMillConnect = (Button) findViewById(R.id.TreadConnect);
        Connecttracker = (Button) findViewById(R.id.FitTrackConnect);

        mBTDevices = new ArrayList<>();


        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);


        // Initialize service and connection state variable
        mServiceConnected = false;
        mConnectState = false;


        //This section required for Android 6.0 (Marshmallow)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check 
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access ");
                builder.setMessage("Please grant location access so this app can detect devices.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
        } //End of section for Android 6.0 (Marshmallow)
        TreadMillConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if(!IsTreadConnected) {
                    startBluetooth(v);
                    // Get a handler that can be used to post to the main thread

                    Handler startbtt = new Handler(Looper.getMainLooper());
                    Runnable sbtrunt = new Runnable() {

                        @Override
                        public void run() {
                            myLabel.setText("searching bluetooth");
                            searchBluetoothTread(v);
                            //myLabel.setText(mBluetoothTestService.getDeviceName());
                        } // This is your code
                    };
                    startbtt.postDelayed(sbtrunt, 1500);

                }else{
                    mBluetoothTestService.disconnectTread();
                    mBluetoothTestService.closeTread();
                    TreadMillConnect.setText("Treadmill Connect");
                    IsTreadConnected = false;
                }
            }
            });




        Connecttracker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if(!IsFitConnected) {
                    startBluetooth(v);
                    // Get a handler that can be used to post to the main thread
                    Handler startbt = new Handler(Looper.getMainLooper());
                    Runnable sbtrun = new Runnable() {

                        @Override
                        public void run() {
                            myLabel.setText("searching bluetooth");
                            searchBluetooth(v);

                        } // This is your code
                    };
                    startbt.postDelayed(sbtrun, 1500);
                }else{
                    mBluetoothTestService.writeHeartRateNotification(false);
                    hrLabel.setText("");
                    myLabel.setText("Disconnected");
                    mBluetoothTestService.disconnect();
                    mBluetoothTestService.close();
                    Connecttracker.setText("Fitness Tracker Connect");
                    IsFitConnected = false;
                }
            }
        });

        MafWorkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenMafActivity();
            }
        });
        FreeRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenFRActivity();
            }
        });
    }



    //This method required for Android 6.0 (Marshmallow)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Permission for 6.0:", "Coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();
                }
            }
        }
    } //End of section for Android 6.0 (Marshmallow)

    public void OpenMafActivity() {
        Intent intent = new Intent( this, MafActivity.class);
        startActivity(intent);
    }
    public void OpenFRActivity() {
        Intent intent = new Intent( this, FreeRun.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register the broadcast receiver. This specified the messages the main activity looks for from the PSoCCapSenseLedService
        final IntentFilter filter = new IntentFilter();

        //filter.addAction(mBluetoothTestService.ACTION_DISCONNECTED);
        filter.addAction(mBluetoothTestService.ACTION_BLESCAN_CALLBACK_FIT_TRACKER);
        filter.addAction(mBluetoothTestService.ACTION_BLESCAN_CALLBACK_TREADMILL);
        filter.addAction(mBluetoothTestService.ACTION_SERVICES_DISCOVERED_FIT_TRACKER);
        filter.addAction(mBluetoothTestService.ACTION_SERVICES_DISCOVERED_TREADMILL);
        filter.addAction(mBluetoothTestService.ACTION_DATA_RECEIVED_TREADMILL);
        filter.addAction(mBluetoothTestService.ACTION_DATA_RECEIVED_FIT_TRACKER);
        filter.addAction(mBluetoothTestService.ACTION_CONNECTED_TO_FIT_TRACKER);
        filter.addAction(mBluetoothTestService.ACTION_CONNECTED_TO_TREADMILL);
        //tread
        registerReceiver(mBleUpdateReceiver, filter);
    }


    /**
     * Listener for BLE event broadcasts
     */

    private final BroadcastReceiver mBleUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case BluetoothTestService.ACTION_CONNECTED_TO_FIT_TRACKER:
                    myLabel.setText("discovering bluetooth");
                    discoverServices(v);
                    IsFitConnected = true;
                    Connecttracker.setText("Disconnect Fitness Tracker");
                    Handler WriteStepNotifThread = new Handler(Looper.getMainLooper());
                    Runnable WriteStepNotifThreadRun = new Runnable(){

                        @Override
                        public void run() {
                            myLabel.setText("notify Update");

                            mBluetoothTestService.writeStepCharacteristic(true);

                             }

                    };
                    WriteStepNotifThread.postDelayed(WriteStepNotifThreadRun,1000);
                    Handler WriteHRNotifThread = new Handler(Looper.getMainLooper());
                    Runnable WriteHRNotifThreadRun = new Runnable(){

                        @Override
                        public void run() {
                            myLabel.setText("notify Update");


                            mBluetoothTestService.writeHeartRateNotification(true);


                        } // This is your code
                    };
                    WriteHRNotifThread.postDelayed(WriteHRNotifThreadRun,1250);
                    break;
                case BluetoothTestService.ACTION_BLESCAN_CALLBACK_FIT_TRACKER:
                    myLabel.setText("connecting bluetooth Fitness tracker");
                    connectBluetooth(v);

                    // Disable the search button and enable the connect button
                    break;
                case BluetoothTestService.ACTION_BLESCAN_CALLBACK_TREADMILL:
                    myLabel.setText("connecting bluetooth Treadmill");
                    treadconnectBluetooth(v);

                    // Disable the search button and enable the connect button
                    break;
                case BluetoothTestService.ACTION_CONNECTED_TO_TREADMILL:
                    myLabel.setText("discovering treadmill");
                    treadDiscoverServices(v);
                    IsTreadConnected = true;
                    Connecttracker.setText("Disconnect Treadmill");

                    if (!mConnectState) {

                        mConnectState = true;

                        Log.d(TAG, "Connected to Device");

                    }
                    break;

                case BluetoothTestService.ACTION_DATA_RECEIVED_FIT_TRACKER:

                    String hrvalue = mBluetoothTestService.getCapSenseValue();

                    hrLabel.setText(hrvalue +" BPM");
                    break;

                default:
                    break;


            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BLE && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBleUpdateReceiver);
    }

   /* @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close and unbind the service when the activity goes away
        mBluetoothTestService.close();
        unbindService(mServiceConnection);
        mBluetoothTestService = null;
        mServiceConnected = false;
    }*/



    public void startBluetooth(View view) {

        // Find BLE service and adapter
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLE);
        }

        // Start the BLE Service
        Log.d(TAG, "Starting BLE Service");
        Intent gattServiceIntent = new Intent(this, BluetoothTestService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        // Disable the start button and turn on the search  button


        Log.d(TAG, "Bluetooth is Enabled");
    }


    public void searchBluetooth(View view) {
        if(mServiceConnected) {
            mBluetoothTestService.getScanStatusFromMain(1);
            mBluetoothTestService.scan();
        }


        /* After this we wait for the scan callback to detect that a device has been found *//*

         */
        /* The callback broadcasts a message which is picked up by the mGattUpdateReceiver */

    }
    public void searchBluetoothTread(View view) {
        //if(mServiceConnected) {
            mBluetoothTestService.getScanStatusFromMain(2);
            mBluetoothTestService.scan();
        //}/*TODO SETUP ARDUINO UUID FOR TREADMILL*/


        /* After this we wait for the scan callback to detect that a device has been found *//*

         */
        /* The callback broadcasts a message which is picked up by the mGattUpdateReceiver */

    }


    public void connectBluetooth(View view) {
        mBluetoothTestService.connect();


        /* After this we wait for the gatt callback to report the device is connected *//*

         */
        /* That event broadcasts a message which is picked up by the mGattUpdateReceiver */

    }



    public void discoverServices(View view) {

        /* This will discover both services and characteristics */

        mBluetoothTestService.discoverServices();
        //myLabel.setText("Connected to Device");
        //lvNewDevices.setAdapter(mBluetoothAdapter.getName());

        /* After this we wait for the gatt callback to report the services and characteristics */


        /* That event broadcasts a message which is picked up by the mGattUpdateReceiver */

    }



    public void Disconnect(View view) {
        mBluetoothTestService.disconnect();


        /* After this we wait for the gatt callback to report the device is disconnected *//*

         */
        /* That event broadcasts a message which is picked up by the mGattUpdateReceiver */

    }

  //
    //
    //   Treadmill bt connect area below
    //
    //
    //



        /* After this we wait for the scan callback to detect that a device has been found *//*

         */
        /* The callback broadcasts a message which is picked up by the mGattUpdateReceiver */




    public void treadconnectBluetooth(View view) {
        //mBluetoothTestServiceTread.connect();
        mBluetoothTestService.connectTread();

        /* After this we wait for the gatt callback to report the device is connected *//*

         */
        /* That event broadcasts a message which is picked up by the mGattUpdateReceiver */

    }




    public void treadDiscoverServices(View view) {

        /* This will discover both services and characteristics */

        mBluetoothTestService.discoverServicesTread();
        //mBluetoothTestServiceTread.discoverServices();
        //myLabel.setText("Connected to Device");
        //lvNewDevices.setAdapter(mBluetoothAdapter.getName());

        /* After this we wait for the gatt callback to report the services and characteristics */


        /* That event broadcasts a message which is picked up by the mGattUpdateReceiver */

    }




    public void treadDisconnect(View view) {
        //mBluetoothTestServiceTread.disconnect();
        mBluetoothTestService.disconnectTread();

        /* After this we wait for the gatt callback to report the device is disconnected *//*

         */
        /* That event broadcasts a message which is picked up by the mGattUpdateReceiver */

    }
}
