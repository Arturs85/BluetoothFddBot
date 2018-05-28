package com.example.user.bluetoothfddbot;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by user on 2017.01.08..
 */

/**
 * Created by user on 2016.04.16..
 */

public class MyCamera {
    Camera mCamera;
    CameraPreview mPreview;
    private Camera.PictureCallback mPicture;
    private Context myContext;

    private static final String TAG = "MyCamera";

    private boolean cameraFront = false;
    private Camera.Parameters mCameraParam;
    private int previewSizeWidth = 200;
    private TextView tekstaLauks;
    private int previewSizeHeight = 300;
    private Intent intent;
    RawImageWithPosition cordImageCur;
    RawImageWithPosition cordImagePrew;
    byte[] data1;
    int frameCounter = 0;
    int noOfFramesToSkip = 0;
    Rect rect;
    byte[] dataCb;
    volatile boolean updateFromCb = false;
    Odometry odometry;
    AttelaApstrade attelaApstradeThread;
    MyPlanView plaanaSkataAprekinsThread;
    ByteArrayOutputStream bao;
    byte[] callbackBuffer;
    byte[] callbackBuffer2;
    int jpgQuality = 20;
    Handler mUpdateHandler;
    final Integer lock = 1;
    // private final RawPictureCallback mRawPictureCallback = new RawPictureCallback();

    public MyCamera(Context context, Handler handler, Odometry odometry) {
        myContext = context;
        mUpdateHandler = handler;
        this.odometry = odometry;
        initialize();
    }


    private int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                cameraFront = true;
                break;
            }
        }
        return cameraId;
    }

    private int findBackFacingCamera() {
        int cameraId = -1;
        //Search for the back facing camera
        //get the number of cameras
        int numberOfCameras = Camera.getNumberOfCameras();
        //for every camera check
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                cameraFront = false;
                break;
            }
        }
        return cameraId;
    }


    public void initialize() {

        if (!hasCamera(myContext)) {
            Toast toast = Toast.makeText(myContext, "Sorry, your phone does not have a camera!", Toast.LENGTH_LONG);
            toast.show();

        }
        mCamera = Camera.open(findBackFacingCamera());
        // Camera.PreviewCallback previewCallback = getPrevCallback();
        //if (previewCallback == null)
        //  Log.e(TAG, "Callback = null ");
        //else
        //  Log.e(TAG, "prew Callback ok ");
        mPreview = new CameraPreview(myContext, mCamera);
        // mCamera.addCallbackBuffer(callbackBuffer);


        mPreview.refreshCamera(mCamera);
        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_AUTO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }
        previewSizeHeight = parameters.getPreviewSize().height;
        previewSizeWidth = parameters.getPreviewSize().width;
        //mCamera.setParameters(parameters);
        mCamera.setDisplayOrientation(90);
        int buferaIzmers = previewSizeHeight * previewSizeWidth;
        buferaIzmers = buferaIzmers / 2 + buferaIzmers;
        Toast toast = Toast.makeText(myContext, Integer.toString(previewSizeWidth) + "x" + Integer.toString(previewSizeHeight), Toast.LENGTH_LONG);
        toast.show();
        callbackBuffer = new byte[buferaIzmers];
        callbackBuffer2 = new byte[buferaIzmers];
        rect = new Rect(0, 0, previewSizeWidth, previewSizeHeight);


        // mCamera.addCallbackBuffer(callbackBuffer);


        // mPreview.refreshCamera(mCamera);

        //  Camera.PreviewCallback previewCallback = getPrevCallback();
        // if (previewCallback == null)
        //    Log.e(TAG, "Callback = null ");
        //else
        //  Log.e(TAG, "prew Callback ok ");
        mCamera.addCallbackBuffer(callbackBuffer);
        // mCamera.setPreviewCallbackWithBuffer(previewCallback);
        //bao = new ByteArrayOutputStream();
        startOverlay1();
        //startPlaanaSkats();
    }

    void startOverlay1() {
        attelaApstradeThread = new AttelaApstrade();
        attelaApstradeThread.start();


    }

    void startPlaanaSkats() {
        plaanaSkataAprekinsThread = new MyPlanView();
        plaanaSkataAprekinsThread.start();
    }

    void stopThreads() {
        if (attelaApstradeThread != null)
            attelaApstradeThread.isRunning = false;
        if (plaanaSkataAprekinsThread != null)
            plaanaSkataAprekinsThread.isRunning = false;

    }

    private boolean hasCamera(Context context) {
        //check if the device has camera
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    Camera.AutoFocusCallback mAutoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            Toast toast = Toast.makeText(myContext, "autofocus done", Toast.LENGTH_SHORT);
            toast.show();
        }
    };

    Camera.PreviewCallback getPrevCallback() {
        Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {

            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                // Log.d(TAG, " onPreview called ");
                synchronized (lock) {
                    cordImagePrew = cordImageCur;
                    cordImageCur = new RawImageWithPosition(data, odometry.xCord, odometry.yCord, odometry.directionAngle, odometry.gyroAngle);
                }
                dataCb = data; //globāla norāde uz pieejamo preview datu masīvu
                updateFromCb = true;


                // data1 = callbackBuffer;

            }
        };
        return previewCallback;
    }


    //make picture and save to a folder
    private File getOutputMediaFile(String cord) {
        //make a new file directory inside the "sdcard" folder
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getPath(), "IndexCamera");

        //if this "JCGCamera folder does not exist
        if (!mediaStorageDir.exists()) {
            //if you cannot make this folder return
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }


        //take the current timeStamp
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        //and make a media file:
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + cord + ".png");

        return mediaFile;
    }

    void saglabatRaw(Bitmap img, int x, int y, int fi10) {
        // void saglabatRaw(byte[] img,int x, int y, int fi10) {
        OutputStream outStream = null;
        String cord = "_x" + x + "y" + y + "fi_" + fi10 + "_";
        File file = (getOutputMediaFile(cord));
        if (file != null) {
            Log.d(TAG, file.toString());

        }
        try {
            outStream = new FileOutputStream(file);
            //img.compressToJpeg(rect, 100, outStream);


            img.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            //      outStream.write(img);
            outStream.flush();
            outStream.close();

            //  Toast toast = Toast.makeText(myContext, "Picture saved: " + file.getName(), Toast.LENGTH_LONG);
            // toast.show();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    void releaseCamera() {
        // stop and release camera
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            //  mCamera.release();
            mCamera = null;
            stopThreads();
        }
    }

    public synchronized void updateMessages(String msg) {
        Log.e(TAG, "Updating message: " + msg);


        Bundle messageBundle = new Bundle();
        messageBundle.putString("msg", msg);

        Message message = new Message();
        message.setData(messageBundle);
        mUpdateHandler.sendMessage(message);

    }

    class AttelaApstrade extends Thread {
        AttelaApstrade() {
            linijuKrasa.setColor(Color.RED);
            linijuKrasa.setAntiAlias(true);
            attels.calculateYGroundMap160();
            attels.calculateYScreenMap160();
        }

        byte[] dati1;
        Bitmap bulta = BitmapFactory.decodeResource(mPreview.getResources(), R.drawable.arrow4);
        Matrix matrix = new Matrix();

        Paint paint = new Paint();

        Paint linijuKrasa = new Paint();
        final Bitmap bitmap2 = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888);

        final Bitmap bitmap = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888);
        Canvas canvas2 = new Canvas(bitmap2);
        Canvas canvas = new Canvas(bitmap);
        boolean pirmaisUzEkrana = true;//false
        boolean isRunning = true;
        int rightObstacleOut = 1;
        int leftObstacleOut = 1;
        int sugestedDirectionOut = 90;
        Attels attels = new Attels();
        int[] krasuHist = new int[362];
        // boolean[][] skersluPx=new boolean[320][240];
        int radius = 20;
        int blivums = 70;
        boolean[][] ekstremi160x120 = new boolean[160][120];
        List<Point> obst = new ArrayList<>(120);
        boolean nextImg = false;
        BirdsEyeKarte birdsEyeKarte = new BirdsEyeKarte();
        volatile boolean izsauktsSaglabat = false;
        volatile boolean oneFrame = true;

        synchronized void zimetBitmap(byte[] dati, Canvas canvas) {
            if (oneFrame) {
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

            }
//while (nextImg==false);
            // attels.spilgtumsDelta = spilgtumsDelta*2+50;
            // if(true)
            //     return;
            //  synchronized (lock) {
            if (cordImageCur != null && cordImagePrew != null) {

//                    boolean[][] mas = attels.nesakritibaIPM160(cordImagePrew, cordImageCur);
                //                  for (int x = 0; x < 160; ++x) { // 640x384
                //                    for (int y = 0; y < 120; ++y) {
                //                      if (mas[x][y]) {

                //                               canvas.drawRect(x * 4, y * 4, x * 4 + 4, y * 4 + 4, linijuKrasa);
//                            }
                //                      }
                //                }

                birdsEyeKarte.updateMapDirect(dati, canvas, (int) cordImageCur.x, (int) cordImageCur.y, cordImageCur.omega);
                //  canvas.drawBitmap(birdsEyeKarte.bitmap,0,0,null);
                //  Log.d("MVision PEO", "drawn on canvas ");
                canvas.drawText("Fokusa attālums: " + mCamera.getParameters().getFocalLength(), 10, 30, paint);

            } else {
                Log.d("MVision PEO", "---------------------img null ");
                mCamera.addCallbackBuffer(callbackBuffer2);
                return;
            }
            //  }
            //obst = o;
            // while (nextImg != true) ;
            //nextImg = false;

            mCamera.addCallbackBuffer(cordImagePrew.data);
        }

        /*synchronized void zimetBitmap(byte[] dati,Canvas canvas) {
             canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

            // attels.spilgtumsDelta = spilgtumsDelta*2+50;
             ekstremi160x120 = attels.atrastAtspidumu160x120(dati, ekstremi160x120);//bez atspiduma, tikai edge
            List<Point>  o = new ArrayList<>(120);
             for (int x = 0; x <= 159; ++x) { // 640x384
                 for (int y = 0; y <= 119; ++y) {
                     if (ekstremi160x120[x][y]) {
                         o.add(new Point(120-y, x));

                         canvas.drawRect(x * 4, y * 4, x * 4 + 4, y * 4 + 4, linijuKrasa);
                     }
                 }
             }
             obst = o;
             mCamera.addCallbackBuffer(dati);
         }
 */
        /*void zimetBitmap(byte[] dati, Canvas canvas) {
            int kolonna = 1;
            int rinda = 1;

            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
          //  attels.vSlieksnis = spilgtumsDelta;
            krasuHist = attels.atrastKrasuHistogrammu(krasuHist, dati);
            //  Log.d("MVision PEO", "krasu hist atrasta ");

            skersluPx = attels.atrastVirsmu(skersluPx, krasuHist, dati);
            //Log.d("MVision PEO", "array size: "+skersluPx.length);

            ArrayList<Point> centri = attels.apvienotPx(skersluPx,radius,blivums);
            //Log.d("MVision PEO", "list size: "+centri.size());
int leftObstacle = 1;
            int rightObstacle = 1;

            for (Point centrs:centri) {

                if((centrs.y<240)&& (rightObstacle<centrs.x)){// jauns, tuvaaks punkts kreisaja pusee
                    rightObstacle = centrs.x;
                }
                if((centrs.y>240) &&(leftObstacle<centrs.x))//tuvaaks p labajaa pusee
                    leftObstacle = centrs.x;

            }


            leftObstacle = 640-leftObstacle;
            rightObstacle = 640-rightObstacle;
          leftObstacleOut=leftObstacle;
            rightObstacleOut=rightObstacle;

            if(leftObstacle>rightObstacle)
                          sugestedDirectionOut = (int)Math.toDegrees(Math.atan2(leftObstacle,-200));//pa kreisi
else if(rightObstacle>leftObstacle)
              sugestedDirectionOut = (int)Math.toDegrees(Math.atan2(rightObstacle,200)); //pa labi
            else
                sugestedDirectionOut = 90;

            sugestedDirectionOut-=90;
            sugestedDirectionOut*=2;
            sugestedDirectionOut+=90;
            for (Point centrs:centri) {
                canvas.drawCircle(centrs.x,centrs.y,radius*2,linijuKrasa);

            }
            canvas. drawText("dir: "+sugestedDirectionOut,400,200,linijuKrasa);
            canvas. drawText("lo: "+leftObstacleOut,400,100,linijuKrasa);
            canvas. drawText("ro: "+rightObstacleOut,400,50,linijuKrasa);

            matrix.setRotate(360-sugestedDirectionOut,200,200);
            // matrix.setRotate(sugestedDirection);
            canvas.drawBitmap(bulta,matrix,null);

            for (int i = 0; i < krasuHist.length; i++) {
                int j = krasuHist[i];
                float[] hsv = {(float) i, 0.9f, 0.9f};
                paint.setColor(Color.HSVToColor(hsv));
                canvas.drawLine(1 * i + 5, 5, 1 * i + 5, j, paint);

            }
            mCamera.addCallbackBuffer(dati);
        }

*/
        public void run() {
            linijuKrasa.setTextSize(40);
            paint.setAlpha(0);
            paint.setColor(Color.LTGRAY);
            Log.e(TAG, "Started attela thread: ");

            try {
                while (isRunning) {

                    if (updateFromCb) {
                        dati1 = cordImageCur.data;
                        // synchronized (bitmap) {
                        // canvas = mySurface.getHolder().lockCanvas();

                        if (dati1 != null) {
                            if (pirmaisUzEkrana || !pirmaisUzEkrana) {
                                zimetBitmap(dati1, canvas2);
                                // canvas2.drawText("Position x: " + cordImageCur.x + " y: " + cordImageCur.y + " fi: " + Math.toDegrees(cordImageCur.omega), 100, 55, linijuKrasa);

                            } else {
                                zimetBitmap(dati1, canvas);
                                // canvas.drawText("Position x: " + cordImageCur.x + " y: " + cordImageCur.y + " fi: " + Math.toDegrees(cordImageCur.omega), 100, 55, linijuKrasa);
                            }

                        } else
                            canvas.drawText("null", 155, 55, linijuKrasa);

                        // mySurface.getHolder().unlockCanvasAndPost(canvas);

                        updateFromCb = false;
                        Message msg = new Message();
                        if (pirmaisUzEkrana || !pirmaisUzEkrana) {
                            pirmaisUzEkrana = false;
                            msg.obj = bitmap2;
                        } else {
                            msg.obj = bitmap;
                            pirmaisUzEkrana = true;
                        }
                        //   Log.e(TAG, "Updating message: " );
                        if (izsauktsSaglabat) {
                            // saglabatRaw(birdsEyeKarte.getImage(), (int) odometry.xCord, (int) odometry.yCord, (int) (odometry.directionAngle * 10));

                            saglabatRaw((Bitmap) (msg.obj), (int) odometry.xCord, (int) odometry.yCord, (int) (odometry.directionAngle * 10));
                            izsauktsSaglabat = false;
                        }
                        mUpdateHandler.sendMessage(msg);

                        // }
                    }
                }
            } catch (Throwable t) {
// just end the background thread
            }
        }

    }

    class MyPlanView extends AttelaApstrade {
        MyPlanView() {
            // super();
            linijuKrasa.setColor(Color.BLUE);
            attels.calculateYMap();
        }


        @Override
        synchronized void zimetBitmap(byte[] dati, Canvas canvas) {
            canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_OVER);
            attels.drawToGroundPlane(dati, canvas, linijuKrasa);
            // attels.spilgtumsDelta = spilgtumsDelta*2+50;
            //ekstremi160x120 = attels.atrastAtspidumu160x120(dati, ekstremi160x120);//bez atspiduma, tikai edge
            //List<Point>  o = new ArrayList<>(120);
            //for (int x = 0; x <= 159; ++x) { // 640x384
            //  for (int y = 0; y <= 119; ++y) {
            //    if (ekstremi160x120[x][y]) {
            //      o.add(new Point(120-y, x));

//                      canvas.drawRect(x * 4, y * 4, x * 4 + 4, y * 4 + 4, linijuKrasa);
            //                }
            //          }
            //    }
            //  obst = o;
            mCamera.addCallbackBuffer(dati);
        }


    }

}


