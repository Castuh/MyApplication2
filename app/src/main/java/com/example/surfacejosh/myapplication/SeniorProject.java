package com.example.surfacejosh.myapplication;
//package Android.Arduino.Bluetooth;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;
import java.io.IOException;
import java.io.InputStream;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;


public class SeniorProject extends AppCompatActivity {

    private static final String TAG = "SeniorProject";
    //Create UI elements and lib variables
    //Button SYNC, MAF_WORKOUT, BT_SEARCH;
    TextView myLabel;
    TextView hrLabel;
    Handler mHandle;
    BluetoothAdapter mBluetoothadapter;
    volatile boolean stopWorker;
    int counter;
    int readBufferPosition;
    byte[] readBuffer;
    Thread workerThread;
    InputStream mmInputStream;
    private Button MafWorkout;
    private Button BT_SEARCH;
    UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
    //BluetoothSocket mmSocket;
    private static String address = "E9:44:48:4F:C6:D1";
    private BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream inputStream;

    ///////////////////////////////////
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(mBluetoothadapter.ACTION_STATE_CHANGED)){
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothadapter.ERROR);
                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onRecieve: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "onRecieve: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "onRecieve: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "onRecieve: STATE TURNING ON");
                        break;
                }
            //when discovery finds a device
            //if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                //BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //String deviceName = device.getName();
                //String deviceHardwareAddress = device.getAddress(); // MAC address
            }
        }
    };
    @Override
    protected void onDestroy(){
        Log.d(TAG, "onDestroy: called");
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver1);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_senior_project);
        MafWorkout = (Button) findViewById(R.id.MAF_WORKOUT);
        BT_SEARCH = (Button) findViewById(R.id.BT_SEARCH);
        myLabel = (TextView) findViewById(R.id.TextV);
        hrLabel = (TextView) findViewById(R.id.hr_view);
        mBluetoothadapter = BluetoothAdapter.getDefaultAdapter();

        BT_SEARCH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    findBT();
                    openBT();
                } catch (IOException ex) {

                }


            }
        });
       MafWorkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             OpenMafActivity();
            }
        });

    }
    void findBT() {
        mBluetoothadapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothadapter == null) {
            myLabel.setText("No bluetooth adapter available");
        }

        if (!mBluetoothadapter.isEnabled()) {
            Intent enableBluetooth = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothadapter
                .getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals("linor")) // this name have to be
                    //TODO Get device name from teammates and place it above
                // replaced with your
                // bluetooth device name
                {
                    mmDevice = device;
                    Log.v("ArduinoBT",
                            "findBT found device named " + mmDevice.getName());
                    Log.v("ArduinoBT",
                            "device address is " + mmDevice.getAddress());
                    break;
                }
            }
        }
        myLabel.setText("Bluetooth Device Found!!");
    }

    public void OpenMafActivity(){
        Intent intent = new Intent(this, MafActivity.class);
        startActivity(intent);
    }
    public void enableDisableBT() {
        if (mBluetoothadapter == null) {
            Log.d(TAG, "Device cannot recieve BT");
        }
        if (!mBluetoothadapter.isEnabled()) {
            Log.d(TAG, "enableDisableBT: Enabling BT.");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1,BTIntent);
            beginListenForData();
        }
        if(mBluetoothadapter.isEnabled()){
            Log.d(TAG, "enableDisable: Disable BT.");
            mBluetoothadapter.disable();
            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1,BTIntent);
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_senior_project, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void beginListenForData()
    {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        int bytesAvailable = mmInputStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            hrLabel.setText(data);
                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }

            }
        });

        workerThread.start();
    }



        void openBT() throws IOException {

        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); // Standard
            mBluetoothadapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothadapter == null) {
                Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            else {
                if(mmDevice == null){
                    myLabel.setText("Bluetooth device is null");
                }
                BluetoothDevice device = mBluetoothadapter.getRemoteDevice(address);
                mmSocket = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString("0000111F-0000-1000-8000-00805F9B34FB"));
                mmSocket.connect();


            }
            // SerialPortService
        // ID
            //mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        //BluetoothSocket socket = this.mmSocket.accept();
        //mmSocket.connect();
       // mmOutputStream = mmSocket.getOutputStream();
       // mmInputStream = mmSocket.getInputStream();
        //myLabel.setText("Bluetooth Opened");
        //beginListenForData();

    }
    void closeBT() throws IOException {
        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
        myLabel.setText("Bluetooth Closed");
    }

    }


