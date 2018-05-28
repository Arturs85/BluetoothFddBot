package com.example.user.bluetoothfddbot;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.view.View;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by user on 2017.11.02..
 */

public class Odometry extends View {
    Odometry(Context context){
        super(context);
        paint.setAntiAlias(true);
        paint.setColor(Color.MAGENTA);
    karte = new Karte();
    }
    Set<Point> trajectory = new HashSet<>(10000);
Karte karte;
    BirdsEyeKarte birdsEyeKarte = new BirdsEyeKarte();

    Paint paint = new Paint();

final double WHEEL_DISTANCE = 95; //aptuveni
   final int ONE_REV_VALUE = 1024;
    final int RADIUS= 42;//mm aptuveni
    double xCord =0;
    double yCord = 0;
    double directionAngle = 1.570796327;
    double gyroAngle=0;
boolean backgroundDrawn= false;
    void update(int deltaLeftWheelValue, int deltaRightWheelValue){
       double deltaleftWheelAngle =  deltaLeftWheelValue*2*Math.PI*RADIUS/ONE_REV_VALUE;
        double deltaRightWheelAngle = deltaRightWheelValue*2*Math.PI*RADIUS/ONE_REV_VALUE;
        //Log.d("Odometry","RWDist: "+deltaRightWheelAngle+" LWDist: "+deltaleftWheelAngle);

        double deltaAngle = (-deltaleftWheelAngle+deltaRightWheelAngle)/WHEEL_DISTANCE;
        double deltaCels = (deltaRightWheelAngle+deltaleftWheelAngle)/2;
        int deltaX = (int)(deltaCels*Math.cos(directionAngle+deltaAngle/2));
        int deltaY = (int)(deltaCels*Math.sin(directionAngle+deltaAngle/2));
        directionAngle +=deltaAngle;

        xCord+=deltaX;
        yCord+=deltaY;
       //synchronized (karte.lock) {
         // trajectory.add(new Point(xCord,-yCord));
           // Log.d("Odometry","X: "+xCord+" Y: "+yCord);
       //}
       postInvalidate();
    }
    void reset(){
        xCord = 0;
        yCord = 0;
        directionAngle = 1.570796327;
trajectory = new HashSet<>(10000);
 karte.reset();
    }
    @Override
    protected void onDraw(Canvas canvas) {
  // Log.d("Odometry","onDraw--------------");
    //if(!backgroundDrawn) {
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_OVER);
  //  backgroundDrawn = true;
   // }
      //  canvas.drawText(" Test ",100,100,paint);
       synchronized (karte.lock) {
           for (Point pt : trajectory) {
               canvas.drawCircle(240 + pt.x/2, 320 + pt.y/2,2, paint);
           }
       }
       float x1 =(float)xCord/2+240;
       float y1 =(float)-(yCord/2)+320;//preteejs virziens y asij
       canvas.drawCircle(x1,y1,5,paint);
canvas.drawLine(x1,y1,(float)(20*Math.cos(directionAngle))+x1,(float)-(20*Math.sin(directionAngle))+y1,paint);
       // super.onDraw(canvas);

        //synchronized (karte.lock) {
          //  for (Point pt : karte.skersli) {
            //    canvas.drawCircle(240 + pt.x / 2, 320 + pt.y / 2, 2, karte.paint);
            //}
        //}
//        synchronized (birdsEyeKarte.lock) {
//           canvas.drawBitmap(birdsEyeKarte.bitmap,0,0,paint);
//        }
    }


}
