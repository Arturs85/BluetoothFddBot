package com.example.user.bluetoothfddbot;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.text.DecimalFormat;
import java.util.List;

public class Imu {
    private SensorManager mSensorManager;
    private Sensor mSensor;
    Sensor mAccelometer;

    long previousSensorTime = 0;
    long previousAccTime = 0;
    double previousSensorValue = 0;
    float previousAccX = 0;
    float previousAccY = 0;
   double fi = 0;
    float Vx = 0;
    float Vy = 0;
    DecimalFormat f;
    boolean first = true;
    boolean firstAcc = true;
    float[] gravity;
    float[] linear_acceleration;
   float [] omega;
    double[] cosAlfa;
    float[] gyroDrift;
    final float alpha = 0.8f;// cita alpha
   float alphaG = 0.999f;
    double gravityXyz;
HandlerThread sensorHandlerThread;
    Handler sensorHandler;

    Imu(Context context) {
      //  super(name);
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mAccelometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        gravity = new float[3];
        linear_acceleration = new float[3];
        omega = new float[3];
        cosAlfa=new double[3];
        gyroDrift = new float[3];

        f = new DecimalFormat();
        f.setGroupingUsed(false);

        f.setMaximumFractionDigits(2);

        List<Sensor> sensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor : sensorList) {
            Log.d("Gyro Main ", sensor.toString());

        }
        sensorHandlerThread= new HandlerThread("Sensor thread");
        sensorHandlerThread.start();
        sensorHandler = new Handler(sensorHandlerThread.getLooper());

        mSensorManager.registerListener(sensorEventListener, mSensor, SensorManager.SENSOR_DELAY_GAME,sensorHandler);
        mSensorManager.registerListener(accListener, mAccelometer, SensorManager.SENSOR_DELAY_GAME,sensorHandler);

    }

    SensorEventListener accListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            long curTime = event.timestamp;//System.nanoTime();
            long deltaTime = curTime - previousAccTime;
            previousAccTime = curTime;

            float aX = event.values[0];
            float aY = event.values[1];

           gravityXyz = Math.sqrt(gravity[0]*gravity[0]+gravity[1]*gravity[1]+gravity[2]*gravity[2]);
            cosAlfa[0]=gravity[0]/(gravityXyz+0.00001);
            cosAlfa[1]=gravity[1]/(gravityXyz+0.00001);
            cosAlfa[2]=gravity[2]/(gravityXyz+0.00001);

            if (!firstAcc) {

                gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
                gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
                gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

                aX = event.values[0] - gravity[0];
                aY = event.values[1] - gravity[1];
                linear_acceleration[2] = event.values[2] - gravity[2];

                float deltaVx = (previousAccX + aX) * 0.5f * deltaTime / 1000000000;
                Vx += deltaVx;
                float deltaVy = (previousAccY + aY) * 0.5f * deltaTime / 1000000000;
                Vy += deltaVy;

               // textViewAcc.setText("xl: " + f.format(Math.toDegrees(Math.acos(cosAlfa[0]))) + ";   yl: " + f.format(Math.toDegrees(Math.acos(cosAlfa[1]))));

            }

            previousAccX = aX;
            previousAccY = aY;
            firstAcc = false;


        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            // Log.d("GM", String.valueOf(event.values.length));

            long curTime = event.timestamp;//System.nanoTime();
            long deltaTime = curTime - previousSensorTime;
            previousSensorTime = curTime;
            float axisZ = event.values[2] - 0.018f;// kalibraacija
            //omega[0]=0.9f*omega[0]+0.1f*event.values[0];
            //omega[1]=0.9f*omega[1]+0.1f*event.values[1];
            //omega[2]=0.9f*omega[2]+0.1f*event.values[2];

            omega[0]=event.values[0]-0.033f;
            omega[1]=event.values[1]+0.0005f;
            omega[2]=event.values[2]-0.018f;


            //double omegaXyz = Math.sqrt((omega[0]*omega[0]+omega[1]*omega[1]+omega[2]*omega[2]));
double omegaXyz = omega[0]*cosAlfa[0]+omega[1]*cosAlfa[1]+omega[2]*cosAlfa[2];


            if (!first) {

              //  gyroDrift[0] = alphaG * gyroDrift[0] + (1 - alphaG) * event.values[0];
                //gyroDrift[1] = alphaG * gyroDrift[1] + (1 - alphaG) * event.values[1];
                //gyroDrift[2] = alphaG * gyroDrift[2] + (1 - alphaG) * event.values[2];



                double deltaFi =((omegaXyz + previousSensorValue) * 0.5 * deltaTime / 1000000000);
                fi += deltaFi;
              //  textView.setText(f.format(fi)+" gyrod2 "+f.format(gyroDrift[2])+" grav: "+f.format(gravityXyz ));
               // textView.setText(f.format(omega[0]) + "; y: " + f.format(omega[1])+" z: "+f.format(omega[2]));
            }

            previousSensorValue = omegaXyz;
            first = false;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

     void mstop() {

        mSensorManager.unregisterListener(sensorEventListener);
        mSensorManager.unregisterListener(accListener);
        sensorHandlerThread.quit();
    }


}
