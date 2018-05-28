package com.example.user.bluetoothfddbot;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import java.util.HashSet;
import java.util.List;

/**
 * Created by user on 2017.11.04..
 */

public class Karte {
    HashSet<Point> skersli;
    final int RESOLUTION_Y = 160;
    final int RESOLUTION_X = 120;

    final int Y_PROJECTION_MM = 720;
    final int X_PROJECTION_HI =290;
    final int X_PROJECTION_LO =45;
    final int Y_CAMERA_OFFSET = 60;
    Paint paint = new Paint();
    final Integer lock = new Integer(1);

    Karte(){
        skersli = new HashSet<>(100000);
     paint.setAntiAlias(true);
        paint.setColor(Color.GRAY);
    }
    void updateMap(List<Point> cameraPoints,int x, int y, double angle ) {
       synchronized(lock){
        for (Point pt : cameraPoints) {
double fi = Math.atan(0.532-pt.y*0.00665);
            double ycord = 136*Math.sin(0.488+fi)/Math.sin(0.646-fi); //kameras transformaacija uz plakni
            int yOffsetFromRobot = Y_CAMERA_OFFSET +(int)ycord;
            int xOffsetFromRobot = ((int)ycord*(X_PROJECTION_HI-X_PROJECTION_LO) /Y_PROJECTION_MM+X_PROJECTION_LO)*(pt.x-RESOLUTION_X/2)/(RESOLUTION_X/2);
            int dist = (int) Math.sqrt(xOffsetFromRobot*xOffsetFromRobot+yOffsetFromRobot*yOffsetFromRobot);
          double  alfa = Math.atan2(xOffsetFromRobot,yOffsetFromRobot);
            int xGlobal = (int) (x+ dist*Math.sin(-angle-alfa+Math.PI/2));
            int yGlobal = (int) (y+dist*Math.cos(- angle-alfa+Math.PI/2));
         //  Log.d("Karte","skerslis x: "+xGlobal+" y: "+yGlobal+" Ymm: "+yOffsetFromRobot+" pt.y: "+pt.y);
           // synchronized (skersli){
            skersli.add(new Point(xGlobal,yGlobal));

        //}
        }}
    }
void reset(){
    skersli = new HashSet<>(100000);


}

}
