


package com.example.surfacejosh.myapplication;
//package Android.Arduino.Bluetooth;
import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
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
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutorCompletionService;

import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.Toast;


public class SeniorProject extends AppCompatActivity { //implements AdapterView.OnItemClickListener {

    private static final String TAG = "SeniorProject";
    //Create UI elements and lib variables
    private TextView myLabel;
    private TextView hrLabel;
    private BluetoothAdapter mBluetoothAdapter;
    private Button Sync;
    private Button MafWorkout;
    private Button BT_CONNECT;
    private Button BT_ONOFF;
    private Button DISCONNECT;
    private Button SEARCH_BT;
    View v;
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

    public DeviceListAdapter mDeviceListAdapter;
    ListView lvNewDevices;


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


    @TargetApi(Build.VERSION_CODES.M) // This is required for Android 6.0 (Marshmallow) to work
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_senior_project);
        //MafWorkout = (Button) findViewById(R.id.MAF_WORKOUT);
        MafWorkout = (Button) findViewById(R.id.MAF_WORKOUT);
        BT_CONNECT = (Button) findViewById(R.id.BT_CONNECT);
        myLabel = (TextView) findViewById(R.id.TextV);
        hrLabel = (TextView) findViewById(R.id.hr_view);
        BT_ONOFF = (Button) findViewById(R.id.BT_ONOFF);
        lvNewDevices = (ListView) findViewById(R.id.lvNewDevices);
        HR_SWITCH = (Switch) findViewById(R.id.HR_SWITCH);
        Sync = (Button) findViewById(R.id.SYNC);
        DISCONNECT = (Button) findViewById(R.id.BTDISCONNECT);
        SEARCH_BT = (Button) findViewById(R.id.SEARCH_BT);
        //startBluetooth(v);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBTDevices = new ArrayList<>();


        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        //registerReceiver(mBroadcastReceiver4, filter);
        //lvNewDevices.setOnItemClickListener(SeniorProject.this);
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

/*
        BT_ONOFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //enableDisableBT();

                //connectBluetooth(view);
                //discoverServices(view);

            }
        });
        BT_CONNECT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //searchBluetooth(view);

            }
        });
        Sync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });*/
        MafWorkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenMafActivity();
            }
        });
        HR_SWITCH.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Turn CapSense Notifications on/off based on the state of the switch
                mBluetoothTestService.writeCapSenseNotification(isChecked);
                HRNotifystate = isChecked;  // Keep track of CapSense notification state
                if(isChecked) { // Notifications are now on so text has to say "No Touch"
                    hrLabel.setText(R.string.NoTouch);
                } else { // Notifications are now off so text has to say "Notify Off"
                    hrLabel.setText(R.string.NotifyOff);
                }
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
                   // BT_ONOFF.setEnabled(false);
                    //connect_button.setEnabled(true);
                    // Disable the search button and enable the connect button
                    break;
                case BluetoothTestService.ACTION_CONNECTED:

                    /* This if statement is needed because we sometimes get a GATT_CONNECTED */


                    /* action when sending Capsense notifications */

                    if (!mConnectState) {
                        // Dsable the connect button, enable the discover services and disconnect buttons
                        BT_CONNECT.setEnabled(false);
                        mConnectState = true;
                        Log.d(TAG, "Connected to Device");
                        Log.d(TAG, "Connected to Device");
                    }
                    break;
                case BluetoothTestService.ACTION_DISCONNECTED:
                    DISCONNECT.setEnabled(false);
                    SEARCH_BT.setEnabled(false);
                    BT_ONOFF.setEnabled(true);

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

                    hrLabel.setText(hrvalue);

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close and unbind the service when the activity goes away
        mBluetoothTestService.close();
        unbindService(mServiceConnection);
        mBluetoothTestService = null;
        mServiceConnected = false;
    }


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
        BT_ONOFF.setEnabled(false);

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
        SEARCH_BT.setEnabled(false);

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

        BT_CONNECT.setEnabled(false);
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
/*
    public void startConnection(){
        if(mBTDevice != null) {
            myLabel.setText(mBTDevice.getName());
            startBTConnection(mBTDevice, MY_UUID_INSECURE);
        } else {
            myLabel.setText("Device is null");
        }
    }*/




/**
 * starting chat service method
 *//*

 */
/*public void startBTConnection(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection.");

        mBluetoothConnection.startClient(device,uuid);
    }*//*

    // OnResume, called right before UI is displayed.  Connect to the bluetooth device.
    */
/*@Override
    protected void onResume() {
        super.onResume();
        myLabel.setText("Scanning for devices ...");

        //uart.connectFirstAvailable();
    }*//*

 */
/*private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState("connected");
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState("Disconnected");
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.
                    ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the
                // user interface.
               // displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayHRData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };
    private void clearUI() {
        //mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        hrLabel.setText("no_data");
    }
    private void updateConnectionState(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                myLabel.setText(message);
            }
        });
    }
    private void displayHRData(String data) {
        if (data != null) {
            hrLabel.setText(data);
        }
    }
    public void enableDisableBT(){
        if(mBluetoothAdapter == null){
            Log.d(TAG, "enableDisableBT: Does not have BT capabilities.");
        }
        if(!mBluetoothAdapter.isEnabled()){
            Log.d(TAG, "enableDisableBT: enabling BT.");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            //registerReceiver(mBroadcastReceiver1, BTIntent);
        }
        if(mBluetoothAdapter.isEnabled()){
            Log.d(TAG, "enableDisableBT: disabling BT.");
            mBluetoothAdapter.disable();

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            //registerReceiver(mBroadcastReceiver1, BTIntent);
        }

    }
*//*


 */
/* public void btnEnableDisable_Discoverable(View view) {
        Log.d(TAG, "btnEnableDisable_Discoverable: Making device discoverable for 300 seconds.");

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        IntentFilter intentFilter = new IntentFilter(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
       // registerReceiver(mBroadcastReceiver2,intentFilter);

    }
*//*

 */
/* public void btnDiscover(View view) {
        Log.d(TAG, "btnDiscover: Looking for unpaired devices.");

        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "btnDiscover: Canceling discovery.");

            //check BT permissions in manifest
            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
        if(!mBluetoothAdapter.isDiscovering()){

            //check BT permissions in manifest
            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
    }*//*


 */
/**
 * This method is required for all devices running API23+
 * Android must programmatically check the permissions for bluetooth. Putting the proper permissions
 * in the manifest is not enough.
 *
 * NOTE: This will only execute on versions > LOLLIPOP because it is not needed otherwise.
 *//*

 */
/*
    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }else{
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }*//*

 */
/* //@Override
    public void onReceive(BluetoothLeUart uart, BluetoothGattCharacteristic rx) {
        // Called when data is received by the UART.
        hrLabel.setText("Received: " + rx.getStringValue(0));
    }*//*



 */
/* @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //first cancel discovery because its very memory intensive.
        mBluetoothAdapter.cancelDiscovery();

        Log.d(TAG, "onItemClick: You Clicked on a device.");
        String deviceName = mBTDevices.get(i).getName();
        String deviceAddress = mBTDevices.get(i).getAddress();

        Log.d(TAG, "onItemClick: deviceName = " + deviceName);
        Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

        //create the bond.
        //NOTE: Requires API 17+? I think this is JellyBean
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
            Log.d(TAG, "Trying to pair with " + deviceName);
            mBTDevices.get(i).createBond();

            mBTDevice = mBTDevices.get(i);
            mBluetoothConnection = new BluetoothConnectionService(SeniorProject.this);
        }
    }*//*

 */
/**
 * Broadcast Receiver for changes made to bluetooth states such as:
 * 1) Discoverability mode on/off or expire.
 *//*

    /*
    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {

                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (mode) {
                    //Device is in Discoverable Mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Enabled.");
                        break;
                    //Device not in discoverable mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Able to receive connections.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Not able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "mBroadcastReceiver2: Connecting....");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "mBroadcastReceiver2: Connected.");
                        break;
                }

            }
        }
    };




  * Broadcast Receiver for listing devices that are not yet paired
     * -Executed by btnDiscover() method.


    private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                mBTDevices.add(device);
                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
                lvNewDevices.setAdapter(mDeviceListAdapter);
            }
        }
    };




     * Broadcast Receiver that detects bond state changes (Pairing status changes)


    private final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases:
                //case1: bonded already
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED.");
                    //inside BroadcastReceiver4
                    mBTDevice = mDevice;
                }
                //case2: creating a bone
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING.");
                }
                //case3: breaking a bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "BroadcastReceiver: BOND_NONE.");
                }
            }
        }
    };

*/