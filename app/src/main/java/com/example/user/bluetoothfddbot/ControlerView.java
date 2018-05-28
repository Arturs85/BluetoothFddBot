package com.example.user.bluetoothfddbot;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


/**
 * Created by user on 2016.09.10..
 */
public class ControlerView extends View {
    Paint paint = new Paint();
    Paint paint2 = new Paint();
    //Bitmap bitmap;
    Point controlerCenter;
    float x =0, y = 0;
    public ControlerView(Context context) {
        super(context);
        controlerCenter = new Point(240,300);
        paint.setAntiAlias(true);
        paint.setColor(Color.CYAN);
        paint2.setColor(Color.BLACK);
x=controlerCenter.x;
        y=controlerCenter.y-55;
       // this.bitmap = bitmap;
    }


    @Override
    protected void onDraw(Canvas canvas) {
Log.d("controllerView", "onDraw");
        super.onDraw(canvas);
canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
canvas.drawCircle(x,y,50,paint);
 canvas.drawLine(controlerCenter.x,controlerCenter.y,x,y,paint);
        canvas.drawCircle(controlerCenter.x,controlerCenter.y,20,paint);
//canvas.drawText("fyfiiifytxzsfghh",100,100,paint2);

        // if (bitmap != null)
       //     canvas.drawBitmap(bitmap, 0, 0, null);
       // else
        //    canvas.drawText("Null bitmap", 44, 40, paint);
        super.onDraw(canvas);

    }
    // invalidate();

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.
         x = e.getX();
         y = e.getY();


            if (e.getAction() == MotionEvent.ACTION_DOWN) {



                    Log.d("TouchEventv", "x = " + x+" y = "+y);

                }



invalidate();
        return true;
    }
int getAngle (){
   return (int)Math.toDegrees(Math.atan2(controlerCenter.x-x,controlerCenter.y-y));

}
byte getSpeed(){
    int speed = (int) Math.sqrt((controlerCenter.x-x)*(controlerCenter.x-x)+(controlerCenter.y-y)*(controlerCenter.y-y));
speed = speed/4;
    if(speed<8)speed = 0;
if(speed>255)speed = 255;
   byte sp = (byte)speed;
    return sp;
}

}


