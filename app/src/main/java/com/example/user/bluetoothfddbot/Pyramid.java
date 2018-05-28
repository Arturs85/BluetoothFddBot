package com.example.user.bluetoothfddbot;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static com.example.user.bluetoothfddbot.BirdsEyeKarte.getXlimits;
import static com.example.user.bluetoothfddbot.BirdsEyeKarte.getYlimits;

public class Pyramid {
    short[][] g0; //original image
    short[][] gausian1;
    short[][] gausian2;
    short[][] gaussian3;
    short[][] gaussian4;
    short[][] laplacian0;
    short[][] laplacian1;
    short[][] laplacian2;
    short[][] laplacian3;
    Point[] corners;
    Paint paint;
    List<Point> boundry = new ArrayList<>(2000);
    private Paint redPaint = new Paint();
    Paint blue = new Paint();
    int[] ylim = new int[]{0, 1000};

    Pyramid(byte[] data, int w, int h) {
        g0 = convertTo2dArray(data, w, h);
        reload();
        paint = new Paint();
        paint.setAlpha(255);
        redPaint.setColor(Color.RED);
        redPaint.setStrokeWidth(3);
        redPaint.setAntiAlias(true);
        blue.setStrokeWidth(3);
        blue.setStyle(Paint.Style.FILL);
        blue.setColor(Color.GREEN);
        blue.setAntiAlias(true);
    }

    Pyramid(short[][] sData) {
        g0 = sData;
        reload();
        paint = new Paint();
        redPaint.setColor(Color.RED);
    }


    void reload() {
        Log.d("reload", "g0 w " + g0.length + " g0 h " + g0[0].length);

        gausian1 = reduce(g0);
        gausian2 = reduce(gausian1);
        gaussian3 = reduce(gausian2);
        gaussian4 = reduce(gaussian3);


        laplacian0 = subtrct(g0, expand(gausian1));
        laplacian1 = subtrct(gausian1, expand(gausian2));
        laplacian2 = subtrct(gausian2, expand(gaussian3));
        laplacian3 = subtrct(gaussian3, expand(gaussian4));

    }

    void merge(Pyramid pyramid) {
        addOtherSide(laplacian0, pyramid.laplacian0);
        addOtherSide(laplacian1, pyramid.laplacian1);
        addOtherSide(laplacian2, pyramid.laplacian2);
        addOtherSide(laplacian3, pyramid.laplacian3);

        addOtherSide(gaussian4, pyramid.gaussian4);

    }

    void merge2(Pyramid pyramid, Point p, Point[] corners) {
        int frameCordX = p.x;
        int frameCordY = p.y;
        this.corners = new Point[corners.length];
        for (int i = 0; i < corners.length; i++) {
            this.corners[i] = new Point(corners[i]);
        }
        //this.corners= Arrays.copyOf(corners,corners.length);
        addFragment(laplacian0, pyramid.laplacian0, frameCordX, frameCordY, corners);
        corners = reduceCorners(corners, 2);

        addFragment(laplacian1, pyramid.laplacian1, frameCordX / 2, frameCordY / 2, corners);
        corners = reduceCorners(corners, 2);

        addFragment(laplacian2, pyramid.laplacian2, frameCordX / 4, frameCordY / 4, corners);
        corners = reduceCorners(corners, 2);

        addFragment(laplacian3, pyramid.laplacian3, frameCordX / 8, frameCordY / 8, corners);
        corners = reduceCorners(corners, 2);

        addFragment(gaussian4, pyramid.gaussian4, frameCordX / 16, frameCordY / 16, corners);
    }

    void reconstruct() {
        gaussian3 = sum(laplacian3, expand(gaussian4));

        gausian2 = sum(laplacian2, expand(gaussian3));
        gausian1 = sum(laplacian1, expand(gausian2));
        g0 = sum(laplacian0, expand(gausian1));

    }

    void addOtherSide(short[][] target, short[][] source) {
        for (int x = target.length / 2; x < target.length; x++) {  //savieno vidū
            for (int y = 0; y < target[0].length; y++) {

                target[x][y] = source[x][y];

            }
        }
    }

    void addFragment(short[][] target, short[][] source, int xF, int yF, Point[] corners) {
        int[][] lines = BirdsEyeKarte.calcLines(corners);
        int[] yLimits = getYlimits(corners);
        if (target.length > 1000) {//test
            ylim = getYlimits(corners);
            boundry.clear();
        }

        //int ySize = yLimits[1] - yLimits[0];
        for (int y = yLimits[0] - yF; y < yLimits[1] - yF; y++) {
            // for (int y = 0; y < source[0].length; y++) {
            int[] xLimits = getXlimits(y + yF, lines);
            if (target.length > 1000) {
                boundry.add(new Point(xLimits[0], y + yF));
                boundry.add(new Point(xLimits[1], y + yF));


            }
            for (int x = xLimits[0] - xF; x < xLimits[1] - xF; x++) {
                // for (int x = 0; x < source.length; x++) {

                if (xF + x >= 0 && yF + y >= 0 && x + xF < target.length && y + yF < target[0].length)
                    target[x + xF][y + yF] = source[x][y];

            }
        }
    }


    short[][] convertTo2dArray(byte[] data, int width, int height) {
        short[][] res = new short[width][height];
        //Arrays.fill(res,255);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                short val = data[x + width * y];
                if (val < 0) val += 255;
                res[x][y] = val;
            }
        }
        return res;
    }

    short[][] subtrct(short[][] s1, short[][] s2) {
        short[][] res = new short[s1.length][s1[0].length];
        Log.d("sbtrct", "s1 w " + s1.length + " s1 h " + s1[0].length + "s2 w " + s2.length + " s2 h " + s2[0].length);

        for (int y = 0; y < res[0].length; y++) {
            for (int x = 0; x < res.length; x++) {
                res[x][y] = (short) (s1[x][y] - s2[x][y]);

            }
        }
        return res;
    }

    short[][] sum(short[][] s1, short[][] s2) {
        short[][] res = new short[s1.length][s1[0].length];
        for (int y = 0; y < res[0].length; y++) {
            for (int x = 0; x < res.length; x++) {
                res[x][y] = (short) (s1[x][y] + s2[x][y]);

            }
        }
        return res;
    }

    short[][] reduce(short[][] data) {
        int width = data.length;
        int height = data[0].length;
        short[] w = new short[]{16, 1, 4, 6, 4, 1};
        short[][] reducedImg = new short[width / 2][height / 2];
        short[][] temp = new short[width / 2][height];
        //horizontālais filtrs
        for (int y = 0; y < height; y += 1) {
            for (int x = 2; x < width - 2; x += 2) {
                //    Log.d("reduce","x "+x+" y "+y);

                temp[x / 2][y] = (short) ((w[1] * data[x - 2][y] + w[2] * data[x - 1][y] + w[3] * data[x][y] + w[4] * data[x + 1][y] + w[5] * data[x + 2][y]) / w[0]);
            }
        }
        //vertikālais filtrs
        for (int x = 0; x < width / 2; x++) {
            for (int y = 2; y < height - 2; y += 2) {
                reducedImg[x][y / 2] = (short) ((w[1] * temp[x][y - 2] + w[2] * temp[x][y - 1] + w[3] * temp[x][y] + w[4] * temp[x][y + 1] + w[5] * temp[x][y + 2]) / w[0]);
            }
        }
        return reducedImg;
    }

    short[][] expand(short[][] data) {
        int width = data.length;
        int height = data[0].length;
        short[][] expandedImg = new short[width * 2][height * 2];
        short[][] temp = new short[width * 2][height];
        short[] w = new short[]{8, 1, 4, 6, 4, 1};

        //horizontālais filtrs
        for (int y = 0; y < height; y += 1) {
            for (int x = 1; x < width * 2 - 1; x += 2) {
                temp[x][y] = (short) ((w[2] * data[x / 2][y] + w[4] * data[x / 2 + 1][y]) / w[0]);
            }
            for (int x = 2; x < width * 2 - 2; x += 2) {
                temp[x][y] = (short) ((w[1] * data[x / 2 - 1][y] + w[3] * data[x / 2][y] + w[5] * data[x / 2 + 1][y]) / w[0]);
            }

        }

        //vertikālais filtrs
        for (int x = 0; x < width * 2; x += 1) {
            for (int y = 1; y < height * 2 - 2; y += 2) {
                expandedImg[x][y] = (short) ((w[2] * temp[x][y / 2] + w[4] * temp[x][y / 2 + 1]) / w[0]);
            }
            for (int y = 2; y < height * 2 - 2; y += 2) {
                expandedImg[x][y] = (short) ((w[1] * temp[x][y / 2 - 1] + w[3] * temp[x][y / 2] + w[5] * temp[x][y / 2 + 1]) / w[0]);
            }

        }
        return expandedImg;
    }


    void draw(short[][] image,int[]xlim,int[]ylim, Canvas g, int xOff, List<float[]> tr) {
       xlim[0]=xlim[0]-20;
        xlim[1]=xlim[1]+20;
        ylim[0]=ylim[0]-20;
        ylim[1]=ylim[1]+20;


        if(xlim[0]<0) xlim[0]=0;
        if (xlim[1]>image.length)xlim[1]=image.length;
        if(ylim[0]<0) ylim[0]=0;
        if (ylim[1]>image.length)ylim[1]=image[0].length;

       // for (int y = 0; y < image[0].length; y++) {
         //   for (int x = 0; x < image.length; x++) {
        for (int y = ylim[0]; y < ylim[1]; y++) {
               for (int x = xlim[0]; x < xlim[1]; x++) {


            paint.setColor(Color.HSVToColor(new float[]{0, 0, image[x][y] / 255f}));
                //g.drawLine(x + xOff, y, x + xOff, y, paint);
                g.drawPoint(x / 2, (960 - y) / 2, paint);
            }
        }
         /* if(corners!=null){
              for (Point p:corners) {
                 g.drawCircle(p.x/2,(960-p.y)/2,5,redPaint);
              }
g.drawLine(0,(960-ylim[0])/2,500,(960-ylim[0])/2,redPaint);
              g.drawLine(0,(960-ylim[1])/2,500,(960-ylim[1])/2,redPaint);
              for (Point p : boundry) {
                  g.drawCircle(p.x/2,(960-p.y)/2,2,redPaint);
              }
         */


}

    Point[] reduceCorners(Point[] corners, int divider) {
        for (Point p : corners) {
            p.x /= divider;
            p.y /= divider;
        }
        return corners;
    }

}
