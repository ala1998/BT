package com.example.bt;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;


public class DeviceListActivity extends Activity {
    // Debugging for LOGCAT
    private static final String TAG = "DeviceListActivity";
//    private static final boolean D = true;
    public SharedPreferences prefs = null;
    private ListView pairedListView;
    public static BluetoothSocket btSocket = null;
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
//    private String prevAdd="";
    // declare button for launching website and textview for connection status
    Button tlbutton;
    TextView textView1;

    // EXTRA string to send on to mainactivity
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    // Member fields
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_list);

        prefs = getSharedPreferences("com.example.bt", MODE_PRIVATE);

        if (!getIntent().getBooleanExtra("Change", false)) {


            String name = prefs.getString("NAME", null);
            String mac = prefs.getString("ADDRESS", null);

            if (name != null && mac != null) {

//            pairedListView.setVisibility(View.GONE);

             /*   TODO: Eza 3emel click tani mrra o ma redi y3mel connect bkhlleeh bl previous, o eza awwal
                b3ml click o ma redi y3mel connect ma bwddeeh 3la new activity*/
                Intent intent = new Intent(DeviceListActivity.this, MainActivity.class);
                intent.putExtra("NAME", name);
                intent.putExtra("ADDRESS", mac);
                intent.putExtra("FIRST", prefs.getBoolean("FIRST", false));
                startActivity(intent);


            }
        }
        checkBTState();

        textView1 = (TextView) findViewById(R.id.connecting);
        textView1.setTextSize(40);
        textView1.setText(" ");

        // Initialize array adapter for paired devices
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

        // Find and set up the ListView for paired devices
        pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // Get a set of currently paired devices and append to 'pairedDevices'
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        // Add previosuly paired devices to the array
        if (pairedDevices.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);//make title viewable
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            mPairedDevicesArrayAdapter.add(noDevices);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        //***************

    }

    // Set up on-click listener for the list (nicked this - unsure)
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            textView1.setText("Connecting...");
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);


           BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter

            BluetoothDevice device = btAdapter.getRemoteDevice(address);

            try {
                btSocket = createBluetoothSocket(device);

            } catch (IOException e) {
                Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();
            }
//            String prevAdd="";
            // Establish the Bluetooth socket connection.
            try
            {
                if(!btSocket.isConnected() || btSocket==null)
                    btSocket.connect();
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("NAME", info);
                editor.putString("ADDRESS", address);
                editor.putBoolean("FIRST", false);
                editor.commit();
                Intent i = new Intent(DeviceListActivity.this, MainActivity.class);
                i.putExtra("NAME", info);
                i.putExtra("ADDRESS", address);
//                prevAdd=address;
//                prevAdd="ala";
//                Toast.makeText(DeviceListActivity.this,"Connecting ala",Toast.LENGTH_LONG).show();
                startActivity(i);
                Toast.makeText(DeviceListActivity.this,"Connected",Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                textView1.setText("Failed to connect...");
//                Toast.makeText(DeviceListActivity.this,"DisConnecting ala",Toast.LENGTH_LONG).show();
//                Toast.makeText(DeviceListActivity.this,prevAdd,Toast.LENGTH_LONG).show();
              /*  if(!prevAdd.equals("")) {
                    Intent i = new Intent(DeviceListActivity.this, MainActivity.class);
                    i.putExtra("NAME", info);
                    i.putExtra("ADDRESS", prevAdd);
//                prevAdd=address;
                    startActivity(i);
                }*/


//                Toast.makeText(DeviceListActivity.this,"Connection failed!",Toast.LENGTH_LONG).show();

         /*   try
            {
                btSocket.close();
            } catch (IOException e2)
            {
                Toast.makeText(this,"Close onCreate",Toast.LENGTH_LONG).show();

                //insert code to deal with this
            }*/
            }
//            mConnectedThread = new MainActivity.ConnectedThread(btSocket);
//            mConnectedThread.start();
//            Toast.makeText(DeviceListActivity.this,address,Toast.LENGTH_LONG).show();
            // Make an intent to start next activity while taking an extra which is the MAC address.

        }
    };

    private void checkBTState() {
        // Check device has Bluetooth and that it is turned on
        mBtAdapter = BluetoothAdapter.getDefaultAdapter(); // CHECK THIS OUT THAT IT WORKS!!!
        if (mBtAdapter == null) {
            Toast.makeText(getBaseContext(), "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show();
        } else {
            if (mBtAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth ON...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);

            }
        }
    }
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }
}