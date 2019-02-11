package com.example.surfacejosh.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;



import static android.content.Context.BIND_AUTO_CREATE;
import static android.support.v4.app.ActivityCompat.startActivityForResult;

public class ConSettingsActivity extends AppCompatActivity {
    private static final String TAG = "ConSettingsActivity";
    private Button Connecttracker;
    private Button ConnectTreadmill;

    private static boolean mConnectState;
    private static boolean mServiceConnected;
    private static BluetoothTestService mBluetoothTestService;
    private static final int REQUEST_ENABLE_BLE = 1;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
    private Context context;



        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.i(TAG, "onServiceConnected");
            mBluetoothTestService = ((BluetoothTestService.LocalBinder) service).getService();
            mServiceConnected = true;
            mBluetoothTestService.initialize();
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
        }
    };
    public void startConnectTread(View view){
        //ConnectThread thread = new ConnectThread(mBluetoothTestService);
       // thread.start();

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_con_settings);
     Connecttracker = (Button) findViewById(R.id.FitTrackConnect);
     ConnectTreadmill = (Button) findViewById(R.id.TreadCon);



    ConnectTreadmill.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {

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

                        searchBluetooth(v);

                    } // This is your code
                };
                startbt.postDelayed(sbtrun,500);

                Handler searchbt = new Handler(Looper.getMainLooper());
                Runnable searchbtrun = new Runnable(){

                    @Override
                    public void run() {

                        connectBluetooth(v);

                    } // This is your code
                };
                searchbt.postDelayed(searchbtrun,2000);
                Handler discoverserv = new Handler(Looper.getMainLooper());
                Runnable discrunnable = new Runnable(){

                    @Override
                    public void run() {

                        discoverServices(v);

                    } // This is your code
                };
                discoverserv.postDelayed(discrunnable,4000);
               /* Handler startbthandler = new Handler(Looper.getMainLooper());
                startbthandler.postDelayed(new Runnable() {
                    public void run() {
                        // Actions to do after 10 seconds

                        startBluetooth(v);
                    }
                }, 500);
                Handler searchbthandler = new Handler(Looper.getMainLooper());
                searchbthandler.postDelayed(new Runnable() {
                    public void run() {
                        // Actions to do after 10 seconds
                        searchBluetooth(v);

                    }
                }, 2500);

            Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    public void run() {
                        // Actions to do after 10 seconds
                        connectBluetooth(v);

                    }
                }, 6000);
                Handler notifyhandler = new Handler(Looper.getMainLooper());
                notifyhandler.postDelayed(new Runnable() {
                    public void run() {
                        // Actions to do after 10 seconds
                        discoverServices(v);

                    }
                }, 500);*/
                //connectBluetooth(v);

            }
    });



    }

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
        //BT_ONOFF.setEnabled(false);

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
        //SEARCH_BT.setEnabled(false);

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

        //BT_CONNECT.setEnabled(false);
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

}
