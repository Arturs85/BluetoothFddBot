package com.example.user.bluetoothfddbot;

import android.util.Log;

/**
 * Created by user on 2017.06.21..
 */

public class SpeedAngleCalculator {
    int inputAngle = 0;
    final private int asuAttalums = 58;
    final private int ritenuAttalums = 10;
    byte leftWheelSp = 0;
    byte rightWheelSp = 0;

    public void calculateAngle(int inputAngle, int desiredSpeed) {

        if (inputAngle > 90) inputAngle = 90;
        if (inputAngle < -90) inputAngle = -90;

        int rightWheelSpeed;
        int leftWheelSpeed;
       int turningRadius= 90;

turningRadius = 90-Math.abs(inputAngle);
if(turningRadius>70)
{
    rightWheelSpeed = desiredSpeed;
    leftWheelSpeed = desiredSpeed; //taisna kustiiba
}
else {
    if (inputAngle > 0) { //pa kreisi
        rightWheelSpeed = desiredSpeed;
        leftWheelSpeed =  (desiredSpeed * turningRadius/(turningRadius+ritenuAttalums));
    } else {//pa labi

        leftWheelSpeed = desiredSpeed;
        rightWheelSpeed = (desiredSpeed * turningRadius/(turningRadius+ritenuAttalums));
    }
}
        Log.d("calc", " rWS = " + rightWheelSpeed + " lWs = " + leftWheelSpeed);

        rightWheelSp = (byte) rightWheelSpeed;
        leftWheelSp = (byte) leftWheelSpeed;
    }


}
