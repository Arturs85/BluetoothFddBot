package com.example.user.bluetoothfddbot;

/**
 * Created by user on 2017.12.24..
 */

public class RawImageWithPosition {
    byte[] data;
    double x;
    double y;
    double omega;
    double gyroOmega;
    RawImageWithPosition(byte[] data,double x, double y,double omega, double gyroOmega ){
        this.data = data;
        this.x = x;
        this.y = y;
        this.omega = omega;
this.gyroOmega =gyroOmega;
    }
}
