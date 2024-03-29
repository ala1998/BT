package com.example.bt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import static com.example.bt.DeviceListActivity.btSocket;

public class MainActivity extends Activity {

//    Button btnOn, btnOff;
    TextView txtArduino, txtString, txtStringLength, sensorView0, sensorView1, sensorView2, sensorView3;
    Handler bluetoothIn;
    int state=0;

    final int handlerState = 0;        				 //used to identify handler message
    private BluetoothAdapter btAdapter = null;
    private StringBuilder recDataString = new StringBuilder();
    private ImageView light;
    private ConnectedThread mConnectedThread;
    private SharedPreferences prefs=null;
    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private SharedPreferences.Editor editor=null;
    // String for MAC address
    private static String address;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //Link the buttons and textViews to respective views
//        btnOn = (Button) findViewById(R.id.buttonOn);
//        btnOff = (Button) findViewById(R.id.buttonOff);
        txtString = (TextView) findViewById(R.id.txtString);
        txtStringLength = (TextView) findViewById(R.id.testView1);
        sensorView0 = (TextView) findViewById(R.id.sensorView0);
        sensorView1 = (TextView) findViewById(R.id.sensorView1);
        sensorView2 = (TextView) findViewById(R.id.sensorView2);
        sensorView3 = (TextView) findViewById(R.id.sensorView3);
        light=findViewById(R.id.ledLight);
        //Get MAC address from DeviceListActivity via intent
        Intent intent = getIntent();

   /*     if(intent.getBooleanExtra("FIRST",false))
        {
           Show change or settings button and start intent to deviceListActivity
     }
     *//*   else
        {
           Hide change device
        }*/
        //Get the MAC address from the DeviceListActivty via EXTRA
        address = intent.getStringExtra("ADDRESS");
//        Toast.makeText(this,address,Toast.LENGTH_LONG).show();
        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter

        prefs = getSharedPreferences("com.example.bt", MODE_PRIVATE);
         editor=prefs.edit();
       /* if(prefs==null){
//            Toast.makeText(this,"NULL",Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(this,"NOT NULL",Toast.LENGTH_LONG).show();

            }*/
        if(prefs.contains("STATE")) {
//            Toast.makeText(this,""+prefs.getInt("STATE",0),Toast.LENGTH_LONG).show();

            if (prefs.getInt("STATE", 0) == 0)
                light.setImageResource(R.drawable.ic_off_bulb);
            else           if (prefs.getInt("STATE", 0) == 1)
                light.setImageResource(R.drawable.ic_bulb_on);
        }
        //     Toast.makeText(this, address, Toast.LENGTH_SHORT).show();
        //create device and set the MAC address
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);

        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        try
        {
            if(!btSocket.isConnected())
            btSocket.connect();
//            Toast.makeText(this,"Connected",Toast.LENGTH_LONG).show();
        } catch (IOException e) {
         /*   try
            {
                btSocket.close();
            } catch (IOException e2)
            {
                Toast.makeText(this,"Close onCreate",Toast.LENGTH_LONG).show();

                //insert code to deal with this
            }*/
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();


        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {										//if message is what we want
                    String readMessage = (String) msg.obj;                                                                // msg.arg1 = bytes from connect thread
                    recDataString.append(readMessage);      								//keep appending to string until ~
                    int endOfLineIndex = recDataString.indexOf("~");                    // determine the end-of-line
                    if (endOfLineIndex > 0) {                                           // make sure there data before ~
                        String dataInPrint = recDataString.substring(0, endOfLineIndex);    // extract string
                        txtString.setText("Data Received = " + dataInPrint);
                        int dataLength = dataInPrint.length();							//get length of data received
                        txtStringLength.setText("String Length = " + String.valueOf(dataLength));

                        if (recDataString.charAt(0) == '#')								//if it starts with # we know it is what we are looking for
                        {
                            String sensor0 = recDataString.substring(1, 5);             //get sensor value from string between indices 1-5
                            String sensor1 = recDataString.substring(6, 10);            //same again...
                            String sensor2 = recDataString.substring(11, 15);
                            String sensor3 = recDataString.substring(16, 20);

                            sensorView0.setText(" Sensor 0 Voltage = " + sensor0 + "V");	//update the textviews with sensor values
                            sensorView1.setText(" Sensor 1 Voltage = " + sensor1 + "V");
                            sensorView2.setText(" Sensor 2 Voltage = " + sensor2 + "V");
                            sensorView3.setText(" Sensor 3 Voltage = " + sensor3 + "V");
                        }
                        recDataString.delete(0, recDataString.length()); 					//clear all string data
                        // strIncom =" ";
                        dataInPrint = " ";
                    }
                }
            }
        };

        checkBTState();

      /*  if(mConnectedThread.read()==0)
            light.setImageResource(R.drawable.ic_off_bulb);
else if(mConnectedThread.read()==1)
            light.setImageResource(R.drawable.ic_bulb_on);
        else  if(mConnectedThread.read()==-1)
            light.setImageResource(R.drawable.ic_off_bulb);
*/
        // Set up onClick listeners for buttons to send 1 or 0 to turn on/off LED
        light.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if(prefs.contains("STATE")) {
//                    Toast.makeText(MainActivity.this,""+prefs.getInt("STATE",0),Toast.LENGTH_LONG).show();

             /*   if(!mConnectedThread.isAlive())
                    mConnectedThread.run();*/
//                if(state==0 && mConnectedThread.isAlive()) {
                    if (prefs.getInt("STATE", 0) == 0) {
                        mConnectedThread.write("1");


                    }
//                else   if(state==0 && mConnectedThread.isAlive()){
                   else if (prefs.getInt("STATE", 0) == 1) {

                        mConnectedThread.write("0");


                    }

                }
                else if(!prefs.contains("STATE"))
                {
                    if(state==0) {
                        mConnectedThread.write("1");

                    }
                    else
                    if(state==1) {
                        mConnectedThread.write("0");
                     /*   state = 0;
                        prefs.edit().putInt("STATE", 0);
                        prefs.edit().commit();*/
                    }

                }
       /*   if(prefs.getInt("STATE",0)==-1){
                    mConnectedThread.write("1");
                    state = 1;
                    prefs.edit().putInt("STATE",state);
                    prefs.edit().commit();
                    light.setImageResource(R.drawable.ic_bulb_on);


                }
*/

                // Send "0" via Bluetooth
//                Toast.makeText(getBaseContext(), "Turn off LED", Toast.LENGTH_SHORT).show();
            }
        });

      /*  btnOn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("1");
                state=1;
                light.setImageResource(R.drawable.ic_bulb_on);
                // Send "1" via Bluetooth
//                Toast.makeText(getBaseContext(), "Turn on LED", Toast.LENGTH_SHORT).show();
            }
        });*/
    }


    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }

    @Override
    public void onResume() {
        super.onResume();

     /* //Get MAC address from DeviceListActivity via intent
        Intent intent = getIntent();

        //Get the MAC address from the DeviceListActivty via EXTRA
        address = intent.getStringExtra("ADDRESS");

//       Toast.makeText(this, address, Toast.LENGTH_SHORT).show();
        //create device and set the MAC address
      BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);

        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        try
        {
            btSocket.connect();
            Toast.makeText(this,"Connected",Toast.LENGTH_LONG).show();
        } catch (IOException e) {
          *//*  try
            {
                btSocket.close();
            } catch (IOException e2)
            {
                Toast.makeText(this,"Close onResume",Toast.LENGTH_LONG).show();

                //insert code to deal with this
            }*//*
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();
*/
        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
//        mConnectedThread.write("0");
    }

    @Override
    public void onPause()
    {
        super.onPause();
     /*   try
        {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            Toast.makeText(this,"Close onPause",Toast.LENGTH_LONG).show();
        }*/
    }

    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBTState() {

        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "Device does not support bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    public void showList(View view) {
      try
      {
          btSocket.close();
      }catch (Exception e){
          Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();
      }
        Intent intent=new Intent(MainActivity.this,DeviceListActivity.class);
        intent.putExtra("Change",true);
        startActivity(intent);
    }

    //create new class for connect thread
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }


        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);            //read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                if (btSocket.isConnected()) {
                    mmOutStream.write(msgBuffer);
                    if(input.equals("1")) {
                        state = 1;
                        editor.putInt("STATE", 1);
                        editor.commit();
                        light.setImageResource(R.drawable.ic_bulb_on);
                    }
                    else if(input.equals("0")) {
                        state = 0;
                        editor.putInt("STATE", 0);
                        editor.commit();
                        light.setImageResource(R.drawable.ic_off_bulb);
                    }
//                   btSocket.close();
                }

                else if (!btSocket.isConnected()) {
                    btSocket.connect();
                    mmOutStream.write(msgBuffer);
                    if(input.equals("1")) {
                        state = 1;
                        editor.putInt("STATE", 1);
                        editor.commit();
                        light.setImageResource(R.drawable.ic_bulb_on);
                    }
                    else if(input.equals("0")) {
                        light.setImageResource(R.drawable.ic_off_bulb);
                        state = 0;
                        editor.putInt("STATE", 0);
                        editor.commit();
                    }
//                 btSocket.close();
                }
                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "Ala  Write "+e.getMessage(), Toast.LENGTH_LONG).show();
//                Toast.makeText(getBaseContext(), "fail to write", Toast.LENGTH_LONG).show();
                //  finish();
              /* try{
                   btSocket.close(); //Close connection
               }
               catch (Exception ex){
                   Toast.makeText(MainActivity.this, "Close "+ex.getMessage(), Toast.LENGTH_SHORT).show();
               }*/

            }
        }

        //write method
      /*  public int read() {
//            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                if (btSocket.isConnected()) {
                    Toast.makeText(MainActivity.this,""+mmInStream.read(),Toast.LENGTH_LONG).show();
                    return mmInStream.read();
                    //                   btSocket.close();
                }

                if (!btSocket.isConnected()) {
                    btSocket.connect();
                    Toast.makeText(MainActivity.this,""+mmInStream.read(),Toast.LENGTH_LONG).show();
                    return mmInStream.read();
//                 btSocket.close();
                }
                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "Read "+e.getMessage(), Toast.LENGTH_LONG).show();
//                Toast.makeText(getBaseContext(), "fail to write", Toast.LENGTH_LONG).show();
                //  finish();
                return -1;

            }
            return -1;
        }

*/
    }
  /*  public void receiveData(BluetoothSocketWrapper socket) throws IOException{
        InputStream socketInputStream =  socket.getInputStream();
        byte[] buffer = new byte[256];
        int bytes;

        // Keep looping to listen for received messages
        while (true) {
            try {
                bytes = socketInputStream.read(buffer);            //read bytes from input buffer
                String readMessage = new String(buffer, 0, bytes);
                // Send the obtained bytes to the UI Activity via handler
                Log.i("logging", readMessage + "");
            } catch (IOException e) {
                break;
            }
        }

    }*/
}

