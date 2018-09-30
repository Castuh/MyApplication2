package com.example.surfacejosh.myapplication;

import com.physicaloid.lib.usb.driver.uart.ReadLisener;
import com.physicaloid.lib.Physicaloid;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.os.Handler;

public class SeniorProject extends AppCompatActivity {

    //Create UI elements and lib variables
    Button SYNC, MAF_WORKOUT, BT_SEARCH;
    TextView tvRead;
    BluetoothAdapter mBluetoothadapter;
    Physicaloid mPhysicaloid;
    ///////////////////////////////////


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_senior_project);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button BT_SEARCH = (Button) findViewById(R.id.BT_SEARCH);
        BT_SEARCH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickBTSEARCH(view);
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                     //   .setAction("Action", null).show();
            }
        });


        // in onCreate Method Initialize The variables
       // BT_SEARCH = (Button) findViewById(R.id.BT_SEARCH);
        SYNC = (Button) findViewById(R.id.SYNC);
        MAF_WORKOUT  = (Button) findViewById(R.id.MAF_WORKOUT);
        tvRead = (TextView) findViewById(R.id.TextV);

        mBluetoothadapter = BluetoothAdapter.getDefaultAdapter();
        mPhysicaloid = new Physicaloid(this);
        ///////////////////////////

    }

    public void onClickBTSEARCH(View v) {
        mPhysicaloid.setBaudrate(9600);
        mPhysicaloid.addReadListener(new ReadLisener()
        {
            int size ;
                public void onRead(int size) {
                    byte[] buf = new byte[size];
                    mPhysicaloid.read(buf, size);
                    tvAttatch(tvRead,("<font color=blue>" + new String(buf) + "</font>"));
                }

        });
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

    //// Handles Text from Arduino
    Handler mHandle = new Handler();
    private void tvAttatch(TextView tv, CharSequence text)
    {
        final TextView ftv = tv;
        final CharSequence ftext = text;
        mHandle.post(new Runnable() {
            @Override
            public void run() {
                ftv.append(ftext);
            }
        });
    }
    /////////////////
}
