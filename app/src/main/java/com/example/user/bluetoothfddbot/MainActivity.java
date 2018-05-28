package com.example.user.bluetoothfddbot;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

/*
Android Example to connect to and communicate with Bluetooth
In this exercise, the target is a Arduino Due + HC-06 (Bluetooth Module)

Ref:
- Make BlueTooth connection between Android devices
http://android-er.blogspot.com/2014/12/make-bluetooth-connection-between.html
- Bluetooth communication between Android devices
http://android-er.blogspot.com/2014/12/bluetooth-communication-between-android.html
 */

public class MainActivity extends ActionBarActivity {

    private static final int REQUEST_ENABLE_BT = 1;

    BluetoothAdapter bluetoothAdapter;
    NumberFormat f;
    ArrayList<BluetoothDevice> pairedDeviceArrayList;
    SeekBar seekBarP;
    SeekBar seekBarI;
    SeekBar seekBarD;
    SeekBar seekBarSpeed;
    boolean comandExecuted = true; //from platform
    boolean newComand = false;
    int pGain = 1;
    int iGain = 0;
    int dGain = 0;
    int speedT = 0;
    MyCamera myCamera;
    long prewiosFrameRecieveTime = 0;
    int skipedFrames = 0;
    LinearLayout kamerasSkats;
    RelativeLayout kamerasParklajums;
    CameraOverlay overlaySkats;
    TextView textInfo, textStatus, textByteCnt, textViewVoltage;
    // ListView listViewPairedDevice;
    //   final Bitmap bitmap = Bitmap.createBitmap(480, 320, Bitmap.Config.ARGB_8888);
    //  Canvas canvas = new Canvas(bitmap);
    LinearLayout txtFields;
    Paint paint = new Paint();
    //LinearLayout odometrijasSkats;
    Odometry odometry;
    Imu imu;
    // ControlerView controlerView;
    // ArrayAdapter<BluetoothDevice> pairedDeviceAdapter;
    private UUID myUUID;
    private final String UUID_STRING_WELL_KNOWN_SPP =
            "00001101-0000-1000-8000-00805F9B34FB";

    ThreadConnectBTdevice myThreadConnectBTdevice;
    ThreadConnected myThreadConnected;
    SendingThreadBT mySendingThreadBt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        paint.setAntiAlias(true);
        paint.setColor(Color.RED);
        textInfo = (TextView) findViewById(R.id.textView);
        textStatus = (TextView) findViewById(R.id.textView2);
        textByteCnt = (TextView) findViewById(R.id.textView3);
        textViewVoltage = (TextView) findViewById(R.id.textViewVoltage);
        seekBarP = (SeekBar) findViewById(R.id.seekBarP);
        seekBarI = (SeekBar) findViewById(R.id.seekBarI);
        seekBarD = (SeekBar) findViewById(R.id.seekBarD);
        seekBarSpeed = (SeekBar) findViewById(R.id.seekBarSpeed);
        seekBarI.setOnSeekBarChangeListener(seekBarChangeListener);
        seekBarP.setOnSeekBarChangeListener(seekBarChangeListener);
        seekBarD.setOnSeekBarChangeListener(seekBarChangeListener);
        seekBarSpeed.setOnSeekBarChangeListener(seekBarChangeListener);

        // listViewPairedDevice = (ListView) findViewById(R.id.pairedlist);
        f = new DecimalFormat();
        f.setGroupingUsed(false);

        f.setMaximumFractionDigits(1);
        //  Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        // setSupportActionBar(myToolbar);
        kamerasParklajums = (RelativeLayout) findViewById(R.id.cameraOverlay);
        kamerasSkats = (LinearLayout) findViewById(R.id.cameraPreview);
        // odometrijasSkats = (LinearLayout)findViewById(R.id.odometryView);
        odometry = new Odometry(this);
        //  odometrijasSkats.addView(odometry);
        odometry.setVisibility(View.GONE);
        // odometrijasSkats.bringToFront();
        myCamera = new MyCamera(this, mHandler, odometry);
        overlaySkats = new CameraOverlay(this);
        kamerasSkats.addView(myCamera.mPreview);
        kamerasParklajums.addView(overlaySkats);
        txtFields = (LinearLayout) findViewById(R.id.textFieldsRelativel);
        txtFields.bringToFront();
        // kamerasParklajums.setRotation(90);
        FrameLayout controlerLayout = (FrameLayout) findViewById(R.id.controlView);
        controlerLayout.bringToFront();
        // controlerView = new ControlerView(this);
        controlerLayout.addView(odometry);
        // controlerView.invalidate();
        //  controlerView.bringToFront(); // nno use

imu = new Imu(this);
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            Toast.makeText(this,
                    "FEATURE_BLUETOOTH NOT support",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        //using the well-known SPP UUID
        myUUID = UUID.fromString(UUID_STRING_WELL_KNOWN_SPP);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this,
                    "Bluetooth is not supported on this hardware platform",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String stInfo = bluetoothAdapter.getName() + "\n" +
                bluetoothAdapter.getAddress();

        textInfo.setText(stInfo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.action_buttons, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem chk = menu.findItem(R.id.actionCheck);
        chk.setChecked(myCamera.attelaApstradeThread.oneFrame);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case  R.id.actionCheck:
                myCamera.attelaApstradeThread.oneFrame=!item.isChecked();
                item.setChecked(myCamera.attelaApstradeThread.oneFrame);
           if(item.isChecked())
               item.setIcon(Resources.getSystem().getDrawable(android.R.drawable.checkbox_on_background));
                else
               item.setIcon(Resources.getSystem().getDrawable(android.R.drawable.checkbox_off_background));

            case R.id.action_add:
               // newComand = true;
                myCamera.attelaApstradeThread.izsauktsSaglabat=true;
                return true;
            case R.id.action_changeToOdo:
                overlaySkats.setVisibility(View.GONE);
                odometry.backgroundDrawn = false;
                odometry.setVisibility(View.VISIBLE);
                odometry.bringToFront();
                //odometry.invalidate();

                return true;
            case R.id.action_changeToOverlay:
                odometry.setVisibility(View.GONE);
                myCamera.attelaApstradeThread.nextImg = true;
                overlaySkats.setVisibility(View.VISIBLE);
                // myCamera.saglabatRaw(myCamera.cordImageCur);//temp
                return true;
            case R.id.action_resetLocation:
                odometry.reset();

                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        myCamera.mCamera.setPreviewCallbackWithBuffer(myCamera.getPrevCallback());
        txtFields.bringToFront();


    }

    @Override
    protected void onStart() {
        super.onStart();
        //Turn ON BlueTooth if it is OFF
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        setup();
    }

    private void setup() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            pairedDeviceArrayList = new ArrayList<BluetoothDevice>();

            for (BluetoothDevice device : pairedDevices) {
                pairedDeviceArrayList.add(device);
                Log.d("MBluetooth", device.getName());
                if (device.getName().contains("robots2")) { //autoconnect
                    myThreadConnectBTdevice = new ThreadConnectBTdevice(device);
                    myThreadConnectBTdevice.start();

                }

            }

            //   pairedDeviceAdapter = new ArrayAdapter<BluetoothDevice>(this,
            //   android.R.layout.simple_list_item_1, pairedDeviceArrayList);
            //  listViewPairedDevice.setAdapter(pairedDeviceAdapter);


        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (myThreadConnectBTdevice != null) {
            myThreadConnectBTdevice.cancel();
            if (myThreadConnected != null)
                myThreadConnected.isRunning = false;
            if (mySendingThreadBt != null)
                mySendingThreadBt.isRunning = false;
             if (myCamera != null)
            myCamera.stopThreads();
              //   myCamera.releaseCamera();

            if(imu!=null)
            imu.mstop();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                setup();
            } else {
                Toast.makeText(this,
                        "BlueTooth NOT enabled",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private Handler mHandler = new Handler() {//a ui tredaa
        @Override
        public void handleMessage(Message msg) {

            if (msg.obj != null) {
                overlaySkats.mSetBitmap((Bitmap) msg.obj);
                //  Log.d("Msg handler", "recieved message: " +((Bitmap) msg.obj).getByteCount());

            }
        }
    };

    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (seekBar.getId() == seekBarP.getId())
                pGain = progress;
            else if (seekBar.getId() == seekBarI.getId())
                iGain = progress;
            else if (seekBar.getId() == seekBarD.getId())
                dGain = progress;
            else
                speedT = progress;

        }


        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    //Called in ThreadConnectBTdevice once connect successed
    //to start ThreadConnected
    private void startThreadConnected(BluetoothSocket socket) {

        myThreadConnected = new ThreadConnected(socket);
        myThreadConnected.start();
    }

    /*
    ThreadConnectBTdevice:
    Background Thread to handle BlueTooth connecting
    */
    private class ThreadConnectBTdevice extends Thread {

        private BluetoothSocket bluetoothSocket = null;
        private final BluetoothDevice bluetoothDevice;


        private ThreadConnectBTdevice(BluetoothDevice device) {
            bluetoothDevice = device;

            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(myUUID);
                textStatus.setText("bluetoothSocket: \n" + bluetoothSocket);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            boolean success = false;
            try {
                bluetoothSocket.connect();
                success = true;
            } catch (IOException e) {
                e.printStackTrace();

                final String eMessage = e.getMessage();
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        textStatus.setText("something wrong bluetoothSocket.connect(): \n" + eMessage);
                    }
                });

                try {
                    bluetoothSocket.close();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }

            if (success) {
                //connect successful
                final String msgconnected = "connect successful:\n"
                        + "BluetoothSocket: " + bluetoothSocket + "\n"
                        + "BluetoothDevice: " + bluetoothDevice;

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        textStatus.setText("");
                        textByteCnt.setText("");
                        Toast.makeText(MainActivity.this, msgconnected, Toast.LENGTH_LONG).show();

                        //  listViewPairedDevice.setVisibility(View.GONE);

                    }
                });

                startThreadConnected(bluetoothSocket);

            } else {
                //fail
            }
        }

        public void cancel() {

            Toast.makeText(getApplicationContext(),
                    "close bluetoothSocket",
                    Toast.LENGTH_LONG).show();

            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    }

    /*
    ThreadConnected:
    Background Thread to handle Bluetooth data communication
    after connected
     */
    private class ThreadConnected extends Thread {
        private final BluetoothSocket connectedBluetoothSocket;
        private final InputStream connectedInputStream;
        private final OutputStream connectedOutputStream;
        volatile boolean isRunning = true;
        byte[] frame = new byte[5];
        byte bytesInFrame = 0;
        int sensorValue = 0;
        int x = 0;
        int dalitajs = 0;

        public ThreadConnected(BluetoothSocket socket) {
            connectedBluetoothSocket = socket;
            InputStream in = null;
            OutputStream out = null;
            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            connectedInputStream = in;
            connectedOutputStream = out;

        }

        @Override
        public void run() {
            byte[] buffer = new byte[5];
            int bytes;

            String strRx = "";
            mySendingThreadBt = new SendingThreadBT(connectedOutputStream);
            mySendingThreadBt.start();

            while (isRunning) {
                try {
                    bytes = connectedInputStream.read(buffer);
                    final String strReceived = new String(buffer, 0, bytes);
                    final String strByteCnt = String.valueOf(bytes) + " bytes received.\n";
                    //  Log.d("RecThread", "buffer[0]: " + buffer[0]);
                    // Log.d("RecThread", "recieved bytes: " + bytes);
                    updateRxFrame(buffer, bytes);
                    // canvas.drawPoint(x,sensorValue/3,paint);


//if(x>=480 ) {
                    // x = 0;
                    // canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//}//

                    //  grafiks.postInvalidate();
                    /// runOnUiThread(new Runnable() {

                    //   @Override
                    // public void run() {
                    //     textStatus.append(strReceived);
                    //    textByteCnt.append(strByteCnt);
                    //}
                    // });

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                    final String msgConnectionLost = "Connection lost:\n"
                            + e.getMessage();
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            textStatus.setText(msgConnectionLost);
                        }
                    });
                }


            }
        }

        public void updateRxFrame(byte[] data, int length) {
            // Log.d("RecThread", "bytes in frame: "+length);

            for (int i = 0; i < length; i++) {

                //  Log.d("RecThread", "bytes in frame: "+bytesInFrame+" i: "+i);

                if (bytesInFrame < 5) {
                    if (bytesInFrame == 0 && data[i] != 65)
                        continue;
                    frame[bytesInFrame] = data[i];
                    bytesInFrame++;
                } else {
                    bytesInFrame = 0;
                    break;
                }
            }
            if (bytesInFrame >= 5) { //pilns frame
                //  ++x;
                sensorValue = bytesToInt(frame[3], frame[4]);
                // Log.d("RecThread", "recieved frame: " + frame[2] + " " + frame[3] + " " + frame[4] + " value: " + sensorValue);
                if (frame[4] == 100)
                    comandExecuted = true;
                bytesInFrame = 0;
                odometry.update(byteToInt(frame[3]),byteToInt( frame[2]));
                /*if (dalitajs++ >= 10) {
                synchronized (odometry.birdsEyeKarte.lock) {
                    if(myCamera.attelaApstradeThread.dati1!=null) {
                       // odometry.birdsEyeKarte.updateMap(myCamera.attelaApstradeThread.dati1, (int) odometry.xCord, (int) odometry.yCord, odometry.directionAngle);
                        //myCamera.mCamera.addCallbackBuffer(myCamera.attelaApstradeThread.dati1);

                    }
                    //  odometry.karte.updateMap(myCamera.attelaApstradeThread.obst, odometry.xCord, odometry.yCord, odometry.directionAngle);
                //Log.d("RecThread", "skersli size: " + odometry.karte.skersli.size());
                    dalitajs = 0;
                  }
                }
                */
                long curTime = System.nanoTime();
                long delta = curTime - prewiosFrameRecieveTime;
                if (delta > 160000000)//160 ms
                    skipedFrames++;
                prewiosFrameRecieveTime = curTime;

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // textInfo.setText(frame[0] + " pwm: " + frame[2] + " speed: " + bytesToShort(frame[3],(byte)0) + " " + frame[4]);
                        textInfo.setText(frame[0] + "; encoders " + frame[2] + " " + frame[3] + " Skiped frames: " + skipedFrames);

                        double voltage = bytesToInt(frame[1], (byte) 0) * 5 * 3.97 / 255;
                        String volts = f.format(voltage);
                        textViewVoltage.setText("Voltage: " + volts + " v"+" x: "+(int)odometry.xCord+" y: "+(int)odometry.yCord+" Ifi: "+f.format(Math.toDegrees(imu.fi))
                                +" Ofi: "+f.format(Math.toDegrees(odometry.directionAngle)));
odometry.gyroAngle=imu.fi;
                    }
                });
            }

        }


        public void cancel() {
            try {
                connectedBluetoothSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private class SendingThreadBT extends Thread {
        OutputStream connectedOutputStream;
        SpeedAngleCalculator speedAngleCalculator;
        volatile boolean isRunning = true;
        int angle = 0;
        byte speed = 0;
        byte[] frame = new byte[3];
        byte bytesInFrame = 0;

        SendingThreadBT(OutputStream os) {
            speedAngleCalculator = new SpeedAngleCalculator();
            connectedOutputStream = os;
        }

        @Override
        public void run() {
            while (isRunning) {

//angle = controlerView.getAngle();
//speed = controlerView.getSpeed();
                //    Log.d("SendingThread", " Angle = " + angle);
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        textByteCnt.setText("angle: " + angle + "; speed: " + bytesToInt(speed, (byte) 0));
                        //    textByteCnt.setText("P: " + pGain + "; I: " + iGain + " D: " + dGain + " Speed: " + speedT);

                    }
                });
                // speedAngleCalculator.calculateAngle(angle, speed);
                //write (new byte[]{90,speedAngleCalculator.rightWheelSp,speedAngleCalculator.leftWheelSp,(byte)dGain,(byte)speedT});

                byte ltargetSpeed = 0;
                byte rTargetSpeed = 0;


                if (comandExecuted && newComand) {
                    int ltargetSp = myCamera.attelaApstradeThread.rightObstacleOut;
                    int rTargetSp = myCamera.attelaApstradeThread.leftObstacleOut;
                    ltargetSp = ltargetSp / 30;
                    ltargetSp -= 7;
                    rTargetSp = rTargetSp / 30;
                    rTargetSp -= 7;
                    if (ltargetSp < 0)
                        ltargetSp = 0;
                    else if (ltargetSp > 13)
                        ltargetSp = 13;
                    if (rTargetSp < 0)
                        rTargetSp = 0;
                    else if (rTargetSp > 13)
                        rTargetSp = 13;
                    sendCommand((byte) ltargetSp, (byte) rTargetSp);
                    comandExecuted = false;
                    //newComand = false;
                }
                try {
                    sleep(500, 0);
                } catch (Exception e) {
                }


            }

        }

        void sendCommand(byte leftTargetPos, byte rightTargetPos) {

            write(new byte[]{90, rightTargetPos, leftTargetPos, 33, (byte) speedT});
        }

        public void write(byte[] buffer) {
            try {
                connectedOutputStream.write(buffer);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }


    int bytesToInt(byte b1, byte b2) {
        int res = b2 << 8;
        int r2 = b1 & 0xFF;
        res |= r2;
        return res;

    }

    int byteToInt(byte b){//recover unsigned byte
int res =0;
        res=res|b;
        return res;
}
    int bytesToShort(byte b1, byte b2) {
        short res = (short) (b2 << 8);
        short r2 = (short) (b1 & 0xFF);
        res |= r2;
        return res;

    }

}

