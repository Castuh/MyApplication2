


package com.example.surfacejosh.myapplication;
//package Android.Arduino.Bluetooth;
import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
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
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorCompletionService;

import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.Toast;


public class SeniorProject extends AppCompatActivity { //implements AdapterView.OnItemClickListener {

    private static final String TAG = "SeniorProject";
    private Button Connecttracker;
    //Create UI elements and lib variables
    private TextView myLabel;
    private TextView hrLabel;
    private BluetoothAdapter mBluetoothAdapter;
    private Button MafWorkout;
    private Button TreadMillConnect;
    private boolean discoservice = false;
    View v;
    private Context appcontext;
    private Switch HR_SWITCH;
    private boolean mConnected = false;

    // Variables to manage BLE connection
    private static boolean mConnectState;
    private static boolean mServiceConnected;
    private static BluetoothTestService mBluetoothTestService;
    private static final int REQUEST_ENABLE_BLE = 1;
    // Keep track of whether hr Notifications are on or off
    private static boolean HRNotifystate = false;

    //This is required for Android 6.0 (Marshmallow)
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();

    BluetoothTestService bts2 = new BluetoothTestService();

    /**
     * This manages the lifecycle of the BLE service.
     * When the service starts we get the service object and initialize the service.
     */

    private final ServiceConnection mServiceConnection = new ServiceConnection() {



        /**
         * This is called when the BluetoothTestService is connected
         *
         * @param componentName the component name of the service that has been connected
         * @param service service being bound
         */

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.i(TAG, "onServiceConnected");
            mBluetoothTestService = ((BluetoothTestService.LocalBinder) service).getService();

            //mBluetoothTestServiceTread = ((BluetoothTestServiceTread.LocalBinder) service).getService();
            mServiceConnected = true;
            mBluetoothTestService.initialize();
            //mBluetoothTestServiceTread.initialize();
        }



        /**
         * This is called when the BluetoothTestService is disconnected.
         *
         * @param componentName the component name of the service that has been connected
         */

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
        appcontext = getApplicationContext();

        MafWorkout =  findViewById(R.id.MAF_WORKOUT);

        myLabel = (TextView) findViewById(R.id.TextV);
        hrLabel = (TextView) findViewById(R.id.hr_view);

        TreadMillConnect = (Button) findViewById(R.id.TreadConnect);
        Connecttracker = (Button) findViewById(R.id.FitTrackConnect);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
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
                startbtt.postDelayed(sbtrunt, 1300);

                Handler searchbtt = new Handler(Looper.getMainLooper());
                Runnable searchbtrunt = new Runnable() {

                    @Override
                    public void run() {
                        myLabel.setText("connecting bluetooth");
                        treadconnectBluetooth(v);

                    } // This is your code
                };
               searchbtt.postDelayed(searchbtrunt, 3000);
                Handler discoverservt = new Handler(Looper.getMainLooper());
                Runnable discrunnablet = new Runnable() {

                    @Override
                    public void run() {
                        myLabel.setText("discovering bluetooth");
                        treadDiscoverServices(v);

                    } // This is your code
                };
                discoverservt.postDelayed(discrunnablet, 29000);
                Handler notifsert = new Handler(Looper.getMainLooper());
                Runnable notifrunt = new Runnable(){

                    @Override
                    public void run() {
                        myLabel.setText(mBluetoothTestService.getDeviceName());

                        //mBluetoothTestService.writespeednotification(true);
                        mBluetoothTestService.readSpeedCharacteristic();
                        //mBluetoothTestService.writeStepCharacteristic(true);
                    } // This is your code
                };
                notifsert.postDelayed(notifrunt,40000);

            }
            });




        Connecttracker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                startBluetooth(v);
                // Get a handler that can be used to post to the main thread
                Handler startbt = new Handler(Looper.getMainLooper());
                Runnable sbtrun = new Runnable(){

                    @Override
                    public void run() {
                        myLabel.setText("searching bluetooth");
                        searchBluetooth(v);

                    } // This is your code
                };
                startbt.postDelayed(sbtrun,1500);

                Handler searchbt = new Handler(Looper.getMainLooper());
                Runnable searchbtrun = new Runnable(){

                    @Override
                    public void run() {
                        myLabel.setText("connecting bluetooth");
                        connectBluetooth(v);

                    } // This is your code
                };
                searchbt.postDelayed(searchbtrun,3500);
                Handler discoverserv = new Handler(Looper.getMainLooper());
                Runnable discrunnable = new Runnable(){

                    @Override
                    public void run() {
                        myLabel.setText("discovering bluetooth");
                        discoverServices(v);

                    } // This is your code
                };
                discoverserv.postDelayed(discrunnable,16000);
                Handler notifser = new Handler(Looper.getMainLooper());
                Runnable notifrun = new Runnable(){

                    @Override
                    public void run() {
                        myLabel.setText("notify Update");
                       // if(mBluetoothTestService. != null) {
                            mBluetoothTestService.writeCapSenseNotification(true);
                       // }
                        //mBluetoothTestService.writeStepCharacteristic(true);
                    } // This is your code
                };
                notifser.postDelayed(notifrun,25000);
                Handler notifser2 = new Handler(Looper.getMainLooper());
                Runnable notifrun2 = new Runnable(){

                    @Override
                    public void run() {
                        myLabel.setText("notify Update");

                        //mBluetoothTestService.writeCapSenseNotification(true);
                      //  if(mBluetoothTestService.getDeviceName() != null) {
                        mBluetoothTestService.writeStepCharacteristic(true);
                       // }


                    } // This is your code
                };
                notifser2.postDelayed(notifrun2,27400);
            }
        });

        MafWorkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenMafActivity();
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
        Intent intent = new Intent(this, MafActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register the broadcast receiver. This specified the messages the main activity looks for from the PSoCCapSenseLedService
        final IntentFilter filter = new IntentFilter();
        filter.addAction(mBluetoothTestService.ACTION_BLESCAN_CALLBACK);
        filter.addAction(mBluetoothTestService.ACTION_CONNECTED);
        filter.addAction(mBluetoothTestService.ACTION_DISCONNECTED);
        filter.addAction(mBluetoothTestService.ACTION_SERVICES_DISCOVERED);
        filter.addAction(mBluetoothTestService.ACTION_DATA_RECEIVED);
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
                case BluetoothTestService.ACTION_BLESCAN_CALLBACK:


                    // Disable the search button and enable the connect button
                    break;
                case BluetoothTestService.ACTION_CONNECTED:
                    //discoverServices(v);
                    //mBluetoothTestService.writeCapSenseNotification(true);
                    /* This if statement is needed because we sometimes get a GATT_CONNECTED */


                    /* action when sending Capsense notifications */

                    if (!mConnectState) {
                        // Dsable the connect button, enable the discover services and disconnect buttons


                        mConnectState = true;

                        Log.d(TAG, "Connected to Device");
                        Log.d(TAG, "Connected to Device");
                    }
                    break;
                case BluetoothTestService.ACTION_DISCONNECTED:
                   // Connecttracker.setEnabled(true);

                case BluetoothTestService.ACTION_DATA_RECEIVED:
                    // This is called after a notify or a read completes
                    // Check LED switch Setting

                    /*if (mBluetoothTestService.getLedSwitchState()) {
                        led_switch.setChecked(true);
                    } else {
                        led_switch.setChecked(false);
                    }*/

                    // Get CapSense Slider Value
                    String hrvalue = mBluetoothTestService.getCapSenseValue();

                    hrLabel.setText(hrvalue +" BPM");

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

    //@Override
    //protected void onDestroy() {
       // super.onDestroy();
        // Close and unbind the service when the activity goes away
       // mBluetoothTestService.close();
        //unbindService(mServiceConnection);
        //mBluetoothTestService = null;
        //mServiceConnected = false;
    //}


    /**
     * This method handles the start bluetooth button
     *
     * @param view the view object
     */

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


    /**
     * This method handles the Search for Device button
     *
     * @param view the view object
     */

    public void searchBluetooth(View view) {
        if(mServiceConnected) {
            mBluetoothTestService.scan();
        }


        /* After this we wait for the scan callback to detect that a device has been found *//*

         */
        /* The callback broadcasts a message which is picked up by the mGattUpdateReceiver */

    }
    public void searchBluetoothTread(View view) {
        if(mServiceConnected) {
            mBluetoothTestService.scan();
        }/*TODO SETUP ARDUINO UUID FOR TREADMILL*/


        /* After this we wait for the scan callback to detect that a device has been found *//*

         */
        /* The callback broadcasts a message which is picked up by the mGattUpdateReceiver */

    }

    /**
     * This method handles the Connect to Device button
     *
     * @param view the view object
     */

    public void connectBluetooth(View view) {
        mBluetoothTestService.connect();


        /* After this we wait for the gatt callback to report the device is connected *//*

         */
        /* That event broadcasts a message which is picked up by the mGattUpdateReceiver */

    }


    /**
     * This method handles the Discover Services and Characteristics button
     *
     * @param view the view object
     */

    public void discoverServices(View view) {

        /* This will discover both services and characteristics */

        mBluetoothTestService.discoverServices();
        //myLabel.setText("Connected to Device");
        //lvNewDevices.setAdapter(mBluetoothAdapter.getName());

        /* After this we wait for the gatt callback to report the services and characteristics */


        /* That event broadcasts a message which is picked up by the mGattUpdateReceiver */

    }


    /**
     * This method handles the Disconnect button
     *
     * @param view the view object
     */

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



    /**
     * This method handles the Connect to Device button
     *
     * @param view the view object
     */

    public void treadconnectBluetooth(View view) {
        //mBluetoothTestServiceTread.connect();
        mBluetoothTestService.connectTread();

        /* After this we wait for the gatt callback to report the device is connected *//*

         */
        /* That event broadcasts a message which is picked up by the mGattUpdateReceiver */

    }


    /**
     * This method handles the Discover Services and Characteristics button
     *
     * @param view the view object
     */

    public void treadDiscoverServices(View view) {

        /* This will discover both services and characteristics */

        mBluetoothTestService.discoverServicesTread();
        //mBluetoothTestServiceTread.discoverServices();
        //myLabel.setText("Connected to Device");
        //lvNewDevices.setAdapter(mBluetoothAdapter.getName());

        /* After this we wait for the gatt callback to report the services and characteristics */


        /* That event broadcasts a message which is picked up by the mGattUpdateReceiver */

    }


    /**
     * This method handles the Disconnect button
     *
     * @param view the view object
     */

    public void treadDisconnect(View view) {
        //mBluetoothTestServiceTread.disconnect();
        mBluetoothTestService.disconnect();

        /* After this we wait for the gatt callback to report the device is disconnected *//*

         */
        /* That event broadcasts a message which is picked up by the mGattUpdateReceiver */

    }
}
