package com.example.surfacejosh.myapplication;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.bluetooth.BluetoothGattCharacteristic;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing the BLE data connection with the GATT database.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP) // This is required to allow us to use the lollipop and later scan APIs
public class BluetoothTestService extends Service {
    private final static String TAG = BluetoothTestService.class.getSimpleName();

    // Bluetooth objects that we need to interact with
    private static BluetoothManager mBluetoothManager;
    private static BluetoothAdapter mBluetoothAdapter;
    private static BluetoothLeScanner mLEScanner;
    private static BluetoothDevice mLeDevice;
    private static BluetoothDevice mLeDeviceTread;
    private static BluetoothGatt mBluetoothGatt;
    private static BluetoothGatt mBluetoothGattTread;

    // Bluetooth characteristics that we need to read/write
    private static BluetoothGattCharacteristic mStepCharacteristic;
    private static BluetoothGattCharacteristic mHRCharacteristic;
    private static BluetoothGattCharacteristic mTreadCharacteristic;
    private static BluetoothGattDescriptor mHRCCCD;
    private static BluetoothGattDescriptor mStepCCCD;
    private static BluetoothGattDescriptor mspCCCD;


    // UUIDs for the service and characteristics that the custom CapSenseLED service uses
   //private String uuidoption;
    private final static String HR_SERVICE = "0000180d-0000-1000-8000-00805f9b34fb";
    private final static String ST_SERVICE = "00110011-4455-6677-8899-AABBCCDDEEFF";
    private final static String TR_SERVICE = "00110011-4455-6677-8899-BBCCAADDEEFF";
    public  final static String HRDATACHARACTERISTIC = "00002a37-0000-1000-8000-00805f9b34fb";
    private final static String HRDATACHARDESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb";
    private final static String STEPDATACHARDESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb";
    //private final static String STEPDATACHARDESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb";
    public  final static String STEPDATACHARACTERISTIC = "0000a507-0000-1000-8000-00805f9b34fb";
    public final static String SpeedDataCharacteristic = "00000002-0000-1000-8000-00805f9b34fb";
    //public  final static String STEPDATACHARACTERISTIC = "00002A38-0000-1000-8000-00805f9b34fb";

    // Variables to keep track of the LED switch state and CapSense Value
    private int scanaction = 0;
    private static String speedread = "0";
    private static boolean mLedSwitchState = false;
    private static boolean mStepState = false;
    private static String mHrValue = "..."; // This is the No hr value ...
    private static String mStepValue = "...";
    // Actions used during broadcasts to the main activity
    public final static String ACTION_CONNECTED_TO_FIT_TRACKER = "ACTION_CONNECTED_TO_FIT_TRACKER";
    public final static String ACTION_CONNECTED_TO_TREADMILL = "ACTION_CONNECTED_TO_TREADMILL";

    public final static String ACTION_BLESCAN_CALLBACK_TREADMILL = "ACTION_BLESCAN_CALLBACK_TREADMILL";
    public final static String ACTION_BLESCAN_CALLBACK_FIT_TRACKER= "ACTION_BLESCAN_CALLBACK_FIT_TRACKER";

    public final static String ACTION_DISCONNECTED_TREADMILL ="ACTION_DISCONNECTED_TREADMILL";
    public final static String ACTION_DISCONNECTED_FIT_TRACKER ="ACTION_DISCONNECTED_FIT_TRACKER";

    public final static String ACTION_SERVICES_DISCOVERED_FIT_TRACKER = "ACTION_SERVICES_DISCOVERED_FIT_TRACKER";
    public final static String ACTION_SERVICES_DISCOVERED_TREADMILL = "ACTION_SERVICES_DISCOVERED_TREADMILL";
    public final static String ACTION_DATA_RECEIVED_FIT_TRACKER = "ACTION_DATA_RECEIVED";
    public final static String ACTION_DATA_RECEIVED_TREADMILL = "ACTION_DATA_RECEIVED_TREADMILL";
    public BluetoothTestService() {
    }

    /**
     * This is a binder for the BlueToothTestService
     */
    public class LocalBinder extends Binder {
        BluetoothTestService getService() {
            return BluetoothTestService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // The BLE close method is called when we unbind the service to free up the resources.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Scans for BLE devices that support the service we are looking for
     */
    public void getScanStatusFromMain(int actionfrommain){
        scanaction = actionfrommain;
    }

    public void scan() {
        /*Scan for devices and look for the one with the service that we want */
        UUID   HrService =       UUID.fromString(HR_SERVICE);
        UUID   StepService = UUID.fromString(ST_SERVICE);
        UUID   TreadService = UUID.fromString(TR_SERVICE);
        UUID[] TreadServiceArray = {TreadService};

        // Use old scan method for versions older than lollipop
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //noinspection deprecation

            mBluetoothAdapter.startLeScan(TreadServiceArray, mLeScanCallback);

            mBluetoothAdapter.startLeScan(TreadServiceArray, mLeScanCallbackTread);


        } else { // New BLE scanning introduced in LOLLIPOP
            ScanSettings settings;
            List<ScanFilter> filters;
            mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
            settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
            filters = new ArrayList<>();
            // We will scan just for the CAR's UUID

            ParcelUuid PUuid = new ParcelUuid(HrService);
            ParcelUuid PUUuid = new ParcelUuid(StepService);
            ParcelUuid PUuidd = new ParcelUuid(TreadService);
            ScanFilter filter = new ScanFilter.Builder().setServiceUuid(PUuid,PUUuid).build();
            ScanFilter filter2 = new ScanFilter.Builder().setServiceUuid(PUuidd).build();
            filters.add(filter);
            filters.add(filter2);

            mLEScanner.startScan(filters, settings, mScanCallback);

            //mLEScanner.startScan(filters, settings, mScanCallbackTread);
        }
        /*switch(scanaction) {
            case 1:
                UUID   HrService  = UUID.fromString(HR_SERVICE);
                UUID   StepService = UUID.fromString(ST_SERVICE);
               // UUID   TreadService = UUID.fromString(TR_SERVICE);
                UUID[] HrStepServiceArray = {HrService,StepService};

                // Use old scan method for versions older than lollipop
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    //noinspection deprecation

                    mBluetoothAdapter.startLeScan(HrStepServiceArray, mLeScanCallback);

                    //mBluetoothAdapter.startLeScan(HrStepServiceArray, mLeScanCallbackTread);


                } else { // New BLE scanning introduced in LOLLIPOP
                    ScanSettings settings;
                    List<ScanFilter> filters;
                    mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                    settings = new ScanSettings.Builder()
                            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                            .build();
                    filters = new ArrayList<>();
                    // We will scan just for the CAR's UUID
                    ParcelUuid PUuid = new ParcelUuid(HrService);
                    ParcelUuid PUUuid = new ParcelUuid(StepService);
                    //ParcelUuid PUuidd = new ParcelUuid(TreadService);
                    ScanFilter filter = new ScanFilter.Builder().setServiceUuid(PUuid,PUUuid).build();
                    //ScanFilter filter2 = new ScanFilter.Builder().setServiceUuid(PUuidd).build();
                    filters.add(filter);
                    //filters.add(filter2);

                    mLEScanner.startScan(filters, settings, mScanCallback);

                   // mLEScanner.startScan(filters, settings, mScanCallbackTread);
                }


                break;
            case 2:
               //UUID   capsenseLedService =       UUID.fromString(HR_SERVICE);
                //UUID   StepService = UUID.fromString(ST_SERVICE);
                UUID   TreadService = UUID.fromString(TR_SERVICE);
                UUID[] TreadServiceArray = {TreadService};

                // Use old scan method for versions older than lollipop
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    //noinspection deprecation

                    //mBluetoothAdapter.startLeScan(TreadServiceArray, mLeScanCallback);

                    mBluetoothAdapter.startLeScan(TreadServiceArray, mLeScanCallbackTread);


                } else { // New BLE scanning introduced in LOLLIPOP
                    ScanSettings settings;
                    List<ScanFilter> filters;
                    mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                    settings = new ScanSettings.Builder()
                            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                            .build();
                    filters = new ArrayList<>();
                    // We will scan just for the CAR's UUID
                    //ParcelUuid PUuid = new ParcelUuid(TreadService);
                   // ParcelUuid PUUuid = new ParcelUuid(StepService);
                    ParcelUuid PUuidd = new ParcelUuid(TreadService);
                   // ScanFilter filter = new ScanFilter.Builder().setServiceUuid(PUuid,PUUuid).build();
                    ScanFilter filter2 = new ScanFilter.Builder().setServiceUuid(PUuidd).build();
                    //filters.add(filter);
                    filters.add(filter2);

                    //mLEScanner.startScan(filters, settings, mScanCallback);

                    mLEScanner.startScan(filters, settings, mScanCallback);
                }
                break;

            default:
                break;

        }*/

    }



    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect() {
        if (mBluetoothAdapter == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }
/*

        // Previously connected device.  Try to reconnect.
        if (mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            return mBluetoothGatt.connect();
        }
*/

        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = mLeDevice.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        return true;
    }
    public String getDeviceName(){
        return mLeDeviceTread.getAddress();
    }
    public boolean connectTread() {
        if (mBluetoothAdapter == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }
/*

        // Previously connected device.  Try to reconnect.
        if (mBluetoothGattTread != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            return mBluetoothGattTread.connect();
        }
*/

        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGattTread = mLeDeviceTread.connectGatt(this, false, mGattCallbackTread);
        Log.d(TAG, "Trying to create a new connection Treadmill.");
        return true;
    }

    /**
     * Runs service discovery on the connected device.
     */
    public void discoverServices() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.discoverServices();
    }
    public void discoverServicesTread() {
        if (mBluetoothAdapter == null || mBluetoothGattTread == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGattTread.discoverServices();
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
        mBluetoothGattTread.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null && mBluetoothGattTread == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
        mBluetoothGattTread.close();
        mBluetoothGattTread = null;
    }

    /**
     * This method is used to read the state of the LED from the device
     */
    public void readSpeedCharacteristic() {
        if (mBluetoothAdapter == null || mBluetoothGattTread == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGattTread.readCharacteristic(mTreadCharacteristic);

    }
    public void readStepCharacteristic() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(mStepCharacteristic);

    }
    public String getStepValue(){
        return mStepValue;
    }
    /**
     * This method is used to turn the LED on or off
     *
     * @param value Turns the LED on (1) or off (0)
     */
    public void writeStepCharacteristic(boolean value){
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(mStepCharacteristic, value);
        byte[] byteVal = new byte[1];
        if (value) {
            byteVal[0] = 1;
        } else {
            byteVal[0] = 0;
        }
        Log.i(TAG, "Step notification " + value);

        mStepCCCD.setValue(byteVal);
        mBluetoothGatt.writeDescriptor(mStepCCCD);
    }
    public void writeSpeedCharacteristic(String value) {
        if (mBluetoothAdapter == null || mBluetoothGattTread == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        byte[] bytes = (value.getBytes());
       /* Integer lsb = new Integer((bytes[1] >> 2)&0xFF);
        byte[] bytes2 = lsb.byteValue()[];*/


        try {
            mTreadCharacteristic.setValue(bytes);
            mBluetoothGattTread.writeCharacteristic(mTreadCharacteristic);

        } catch (Exception e) {

        }
    }

    /**
     * This method enables or disables notifications for the CapSense slider
     *
     * @param value Turns notifications on (1) or off (0)
     */
    public void writeCapSenseNotification(boolean value) {
        // Set notifications locally in the CCCD
        mBluetoothGatt.setCharacteristicNotification(mHRCharacteristic, value);
       // mBluetoothGatt.setCharacteristicNotification(mStepCharacteristic, value);
        byte[] byteVal = new byte[1];
        if (value) {
            byteVal[0] = 1;
        } else {
            byteVal[0] = 0;
        }
        // Write Notification value to the device
        Log.i(TAG, "HR Notification " + value);
        mHRCCCD.setValue(byteVal);
        mBluetoothGatt.writeDescriptor(mHRCCCD);
       // mStepCCCD.setValue(byteVal);
       // mBluetoothGatt.writeDescriptor(mStepCCCD);

    }
    /*public void  writespeednotification(boolean value) {
        // Set notifications locally in the CCCD
        mBluetoothGattTread.setCharacteristicNotification(mTreadCharacteristic, value);
        // mBluetoothGatt.setCharacteristicNotification(mStepCharacteristic, value);
        byte[] byteVal = new byte[1];
        if (value) {
            byteVal[0] = 1;
        } else {
            byteVal[0] = 0;
        }
        // Write Notification value to the device
        Log.i(TAG, "speed notification " + value);
        mspCCCD.setValue(byteVal);
        mBluetoothGattTread.writeDescriptor(mspCCCD);
        // mStepCCCD.setValue(byteVal);
        // mBluetoothGatt.writeDescriptor(mStepCCCD);
    }*/

    /**
     * This method returns the state of the LED switch
     *
     * @return the value of the LED swtich state
     */
   /* public boolean getLedSwitchState() {
        return mLedSwitchState;
    }*/

    /**
     * This method returns the value of th CapSense Slider
     *
     * @return the value of the CapSense Slider
     */
    public String getCapSenseValue() {
        return mHrValue;
    }
    public String getSpeedReading(){
        return speedread;
    }

    /**
     * Implements the callback for when scanning for devices has found a device with
     * the service we are looking for.
     *
     * This is the callback for BLE scanning on versions prior to Lollipop
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallbackTread =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {

                    mLeDeviceTread = device;
                    //noinspection deprecation
                    mBluetoothAdapter.stopLeScan(mLeScanCallbackTread); // Stop scanning after the first device is found
                    broadcastUpdate(ACTION_BLESCAN_CALLBACK_TREADMILL); // Tell the main activity that a device has been found
                }
            };

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    mLeDevice = device;
                    //noinspection deprecation
                    mBluetoothAdapter.stopLeScan(mLeScanCallback); // Stop scanning after the first device is found
                    broadcastUpdate(ACTION_BLESCAN_CALLBACK_FIT_TRACKER); // Tell the main activity that a device has been found
                }
            };

    /**
     * Implements the callback for when scanning for devices has faound a device with
     * the service we are looking for.
     *
     * This is the callback for BLE scanning for LOLLIPOP and later
     */
    private final ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {


            if(result.getDevice().getAddress().equals("C0:D4:40:C4:1A:81") && scanaction == 1){
                mLeDevice = result.getDevice();
                mLEScanner.stopScan(mScanCallback); // Stop scanning after the first device is found
                broadcastUpdate(ACTION_BLESCAN_CALLBACK_FIT_TRACKER); // Tell the main activity that a device has been found

            }
            if(result.getDevice().getAddress().equals("E9:44:48:4F:C6:D1") && scanaction == 2){
                mLeDeviceTread = result.getDevice();
                mLEScanner.stopScan(mScanCallback); // Stop scanning after the first device is found
                broadcastUpdate(ACTION_BLESCAN_CALLBACK_TREADMILL); // Tell the main activity that a device has been found

            }
        }
    };
    /*private final ScanCallback mScanCallbackTread = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            mLeDeviceTread = result.getDevice();
            mLEScanner.stopScan(mScanCallbackTread); // Stop scanning after the first device is found
            broadcastUpdate(ACTION_BLESCAN_CALLBACK_TREADMILL); // Tell the main activity that a device has been found
       }
    };*/


    /**
     * Implements callback methods for GATT events that the app cares about.  For example,
     * connection change and services discovered.
     */

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
               // broadcastUpdate(ACTION_CONNECTED);
                broadcastUpdate(ACTION_CONNECTED_TO_FIT_TRACKER);
                Log.i(TAG, "Connected to GATT server.");
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(ACTION_DISCONNECTED_FIT_TRACKER);

            }



        }

        /*@Override
        public void onConnectionUpdated(BluetoothGatt gatt, int interval, int latency, int timeout,
                                        int status){

        }*/
        /**
         * This is called when a service discovery has completed.
         *
         * It gets the characteristics we are interested in and then
         * broadcasts an update to the main activity.
         *
         * @param gatt The GATT database object
         * @param status Status of whether the write was successful.
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
           /* if(mLeDevice == null){

                if(mLeDeviceTread.getAddress().equals("E9:44:48:4F:C6:D1")) {
                    BluetoothGattService mService3 = gatt.getService(UUID.fromString(TR_SERVICE));
                    *//*TreadCharacteristic*//*

                    mTreadCharacteristic = mService3.getCharacteristic(UUID.fromString(SpeedDataCharacteristic));
                    mspCCCD = mTreadCharacteristic.getDescriptor(UUID.fromString(HRDATACHARDESCRIPTOR));
                    readSpeedCharacteristic();
                }
            }*/
            //if(mLeDeviceTread == null) {
                if(mLeDevice.getAddress().equals("C0:D4:40:C4:1A:81")) {
                    // Get just the service that we are looking for
//hr service
                    BluetoothGattService mService = gatt.getService(UUID.fromString(HR_SERVICE));
                    //step service
                    BluetoothGattService mService2 = gatt.getService(UUID.fromString(ST_SERVICE));
                    /* Get characteristics from our desired service */
                    mHRCharacteristic = mService.getCharacteristic(UUID.fromString(HRDATACHARACTERISTIC));
                    /* Get the hr CCCD */
                    mHRCCCD = mHRCharacteristic.getDescriptor(UUID.fromString(HRDATACHARDESCRIPTOR));



                    //step characteristic
                    mStepCharacteristic = mService2.getCharacteristic(UUID.fromString(STEPDATACHARACTERISTIC));
                    //step discriptor
                    mStepCCCD = mStepCharacteristic.getDescriptor(UUID.fromString(STEPDATACHARDESCRIPTOR));
                }
            //}
            //Tread service


            //writeStepCharacteristic(true);
            // Read the current state of the LED from the device
            //readLedCharacteristic();
            //readStepCharacteristic();

            //mStepValue = mService.getCharacteristic(UUID.fromString(STEPDATACHARACTERISTIC)).getValue().toString();
            // Broadcast that service/characteristic/descriptor discovery is done

            //readStepCharacteristic();
            broadcastUpdate(ACTION_SERVICES_DISCOVERED_FIT_TRACKER);
        }

        /**
         * This is called when a read completes
         *
         * @param gatt the GATT database object
         * @param characteristic the GATT characteristic that was read
         * @param status the status of the transaction
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {

            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Verify that the read was the LED state
                String uuid = characteristic.getUuid().toString();
                // In this case, the only read the app does is the LED state.
                // If the application had additional characteristics to read we could
                // use a switch statement here to operate on each one separately.
               /* if(uuid.equalsIgnoreCase(STEPDATACHARACTERISTIC)) {
                  // final byte[] data = characteristic.getValue();
                    int spint = (characteristic.getValue()[0]);
                  mStepValue = Integer.toString(spint);

                    // Set the LED switch state variable based on the characteristic value ttat was read\
                    //int value = (data[0] & 0xff);
                    //mStepValue = toString(value);
                }*/
                // Notify the main activity that new data is available
                broadcastUpdate(ACTION_DATA_RECEIVED_FIT_TRACKER);
            }
        }



        /**
         * This is called when a characteristic with notify set changes.
         * It broadcasts an update to the main activity with the changed data.
         *
         * @param gatt The GATT database object
         * @param characteristic The characteristic that was changed
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {

            String uuid = characteristic.getUuid().toString();

            // In this case, the only notification the apps gets is the CapSense value.
            // If the application had additional notifications we could
            //TODO  make switch statement
            //characteristic = mStepCharacteristic;
            //uuid = STEPDATACHARACTERISTIC;
            switch (uuid) {
                case HRDATACHARACTERISTIC:
                    // use a switch statement here to operate on each one separately.
                    int hrb1 = (characteristic.getValue()[0]& 0xFF);
                    int hrb2 = (characteristic.getValue()[1]& 0xFF);
                    int concathr = hrb2 << 8;
                    int finconhr = concathr | hrb1;
                    mHrValue = Integer.toString(finconhr);
                    //}
                    break;
                case STEPDATACHARACTERISTIC:
                    int spint2 = (characteristic.getValue()[0]& 0xFF);
                    int spint3 = (characteristic.getValue()[1]& 0xFF);
                    int concat = spint3 << 8;
                    int fincon = concat | spint2;
                    mStepValue = Integer.toString(fincon);

                    break;

                default:

                    }

                    // Notify the main activity that new data is available
                    broadcastUpdate(ACTION_DATA_RECEIVED_FIT_TRACKER);
                }




    }; // End of GATT event callback methods
    //
    //
    //TREAD GATT CALLBACK METHODS
    //
    //
    //
    private final BluetoothGattCallback mGattCallbackTread = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                broadcastUpdate(ACTION_CONNECTED_TO_TREADMILL);
                Log.i(TAG, "Connected to GATT Tread server.");
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from Tread GATT server.");
                broadcastUpdate(ACTION_DISCONNECTED_TREADMILL);
            }

        }


        /**
         * This is called when a service discovery has completed.
         *
         * It gets the characteristics we are interested in and then
         * broadcasts an update to the main activity.
         *
         * @param gatt The GATT database object
         * @param status Status of whether the write was successful.
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            //if(mLeDevice == null){

                if(mLeDeviceTread.getAddress().equals("E9:44:48:4F:C6:D1")) {
                    BluetoothGattService mService3 = gatt.getService(UUID.fromString(TR_SERVICE));
                    /*TreadCharacteristic*/

                    mTreadCharacteristic = mService3.getCharacteristic(UUID.fromString(SpeedDataCharacteristic));
                    mspCCCD = mTreadCharacteristic.getDescriptor(UUID.fromString(HRDATACHARDESCRIPTOR));
                    //readSpeedCharacteristic();
                }
           // }
           /* if(mLeDeviceTread == null) {
                if(mLeDevice.getAddress().equals("C0:D4:40:C4:1A:81")) {
                    // Get just the service that we are looking for

                    BluetoothGattService mService = gatt.getService(UUID.fromString(HR_SERVICE));
                    //step service
                    BluetoothGattService mService2 = gatt.getService(UUID.fromString(ST_SERVICE));
                    *//* Get characteristics from our desired service *//*
                    mHRCharacteristic = mService.getCharacteristic(UUID.fromString(HRDATACHARACTERISTIC));
                    *//* Get the hr CCCD *//*
                    mHRCCCD = mHRCharacteristic.getDescriptor(UUID.fromString(HRDATACHARDESCRIPTOR));



                    //step characteristic
                    mStepCharacteristic = mService2.getCharacteristic(UUID.fromString(STEPDATACHARACTERISTIC));
                    //step discriptor
                    mStepCCCD = mStepCharacteristic.getDescriptor(UUID.fromString(STEPDATACHARDESCRIPTOR));
                }
            }


*/
            //writeStepCharacteristic(true);
            // Read the current state of the LED from the device
            //readLedCharacteristic();
            //readStepCharacteristic();

            //mStepValue = mService.getCharacteristic(UUID.fromString(STEPDATACHARACTERISTIC)).getValue().toString();
            // Broadcast that service/characteristic/descriptor discovery is done

            //readStepCharacteristic();
            broadcastUpdate(ACTION_SERVICES_DISCOVERED_TREADMILL);
        }

        /**
         * This is called when a read completes
         *
         * @param gatt the GATT database object
         * @param characteristic the GATT characteristic that was read
         * @param status the status of the transaction
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {

            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Verify that the read was the LED state
                String uuid = characteristic.getUuid().toString();
                // In this case, the only read the app does is the LED state.
                // If the application had additional characteristics to read we could
                // use a switch statement here to operate on each one separately.
                if(uuid.equalsIgnoreCase(SpeedDataCharacteristic)) {
                    // final byte[] data = characteristic.getValue();
                    int speedbyte1 = (characteristic.getValue()[0]& 0xFF);
                    //int speedbyte2 = (characteristic.getValue()[1]& 0xFF);
                    //int concatspeed = speedbyte2 << 8;
                    //int finconspeed = concatspeed | speedbyte1;
                    speedread = Integer.toString(speedbyte1);//finconspeed);
                }
                // Notify the main activity that new data is available
                broadcastUpdate(ACTION_DATA_RECEIVED_TREADMILL);
            }
        }


        /**
         * This is called when a characteristic with notify set changes.
         * It broadcasts an update to the main activity with the changed data.
         *
         * @param gatt The GATT database object
         * @param characteristic The characteristic that was changed
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {

            String uuid = characteristic.getUuid().toString();

            // In this case, the only notification the apps gets is the CapSense value.
            // If the application had additional notifications we could
            //TODO  make switch statement
            //characteristic = mStepCharacteristic;
            //uuid = STEPDATACHARACTERISTIC;
            /*switch (uuid) {
                case SpeedDataCharacteristic:
                    Log.d(TAG,"speed CHARACTERISTIC");
                    int lsb2 = (characteristic.getValue()[1] & 0xFF);
                    speedread = Integer.toString(lsb2);
                default:

            }*/

            // Notify the main activity that new data is available
            broadcastUpdate(ACTION_DATA_RECEIVED_TREADMILL);
        }




    }; // End of Tread GATT event callback methods

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic)
    {
        mBluetoothGatt.writeCharacteristic(characteristic);
    }
    /**
     * Sends a broadcast to the listener in the main activity.
     *
     * @param action The type of action that occurred.
     */
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

}
