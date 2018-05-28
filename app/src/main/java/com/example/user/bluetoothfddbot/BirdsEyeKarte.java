package com.example.user.bluetoothfddbot;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static java.lang.Math.cos;

/**
 * Created by user on 2017.11.04..
 */

public class BirdsEyeKarte {
    HashSet<Point> skersli;
    final int RESOLUTION_Y = 160;
    final int RESOLUTION_X = 120;

    final int Y_PROJECTION_MM = 720;
    final int X_PROJECTION_HI = 290;
    final int X_PROJECTION_LO = 45;
    final int Y_CAMERA_OFFSET = 60;
    Paint paint = new Paint();
    final Integer lock = new Integer(1);
    final double devindesmit = Math.toRadians(90);
    //final Bitmap bitmap = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888);
    //Canvas canvas = new Canvas(bitmap);
    Attels attels;
    //  short[][] wholeImage;
    byte[] data;
    Pyramid wholeImagePy;
    short[][] jaunsFragments;
    int mapWidth = 1280;
    int mapHeight = 960;
    int fragmentCounter = 0;
    private Paint redPaint = new Paint();
    Paint blue = new Paint();
    BirdsEyeKarte() {
        // skersli = new HashSet<>(100000);
        redPaint.setColor(Color.RED);
        redPaint.setStrokeWidth(3);
        redPaint.setAntiAlias(true);
        blue.setStrokeWidth(3);
        blue.setStyle(Paint.Style.FILL);
        blue.setColor(Color.GREEN);
        blue.setAntiAlias(true);

        paint.setAntiAlias(false);

        paint.setColor(Color.LTGRAY);

        attels = new Attels();
        attels.atrastKrasuTabulu();
        //  attels.calculateYMap();
        //  wholeImage = new short[640][480];
        byte[] blank = new byte[1280 * 960];
        // byte[] blank = new byte[640 * 480];

        Arrays.fill(blank, (byte) -1);
        wholeImagePy = new Pyramid(blank, 1280, 960);
        // wholeImagePy = new Pyramid(blank, 640, 480);

    }

    public void setData(byte[] data) {
        this.data = data;
    }


    void updateMap(byte[] data, Canvas canvas, int x, int y, double angle) {
        // synchronized(lock){
        try {


            double cosa = cos(angle - devindesmit);
            double sina = Math.sin(angle - devindesmit);
            List<Point> robeza = new ArrayList<>(600);
            for (int xt = 0; xt < 720; xt++) {
                int draft = (int) (xt * 0.34);
                int pxSkaitsRindaa = 580 - 2 * draft;
                for (int yt = draft; yt < 580 - draft; yt++) {
                    short ye = attels.yGroundMap[xt];
                    short xe = (short) ((yt - draft) * 480 / pxSkaitsRindaa);
                    //  Log.d("Attels"," ye "+ye+  " xe "+xe);


                    float yValue = data[640 * (xe) + ye];
                    if (yValue < 0) yValue += 255;
                    // paariet uz platformas koord sist
                    int yPl = Y_PROJECTION_MM - xt + Y_CAMERA_OFFSET;
                    int xPl = -yt + X_PROJECTION_HI;
//veic rotāiju un translaciju
                    int xGlobal = (int) (xPl * cosa - yPl * sina + x);
                    int yGlobal = (int) (xPl * sina + yPl * cosa + y);
//                    if (xGlobal / 2 + 240 + 6 < 480 && xGlobal / 2 + 240 - 4 > 0 && -yGlobal / 2 + 320 > 5 && -yGlobal / 2 + 320 < 630) {
//                        wholeImage[xGlobal / 2 + 240][-yGlobal / 2 + 320] = (short) yValue;
//                        if (yt == 580 - draft - 1) {
//                            robeza.add(new Point(xGlobal, yGlobal));
//
//                        }
//                    }


                    paint.setColor(Color.HSVToColor(new float[]{1, 0, yValue / 255}));
                    //     canvas.drawPoint(xGlobal/2+240, -yGlobal/2+320, paint);
                    canvas.drawPoint(xGlobal / 2 + 240, -yGlobal / 2 + 480, paint);


                }
            }
            paint.setColor(Color.BLUE);
         /*  for (Point p:robeza) {
            if(p.x>-470&&p.x<470&&p.y>-630&&p.y<630) {
//canvas.drawLine((p.x-4)/2+240,-p.y/2+320,(p.x+4)/2+240,-p.y/2+320,paint);
                int v1 = (wholeImage[(p.x - 4) / 2 + 240][-p.y / 2 + 320] + wholeImage[(p.x - 2) / 2 + 240][-p.y / 2 + 320] +
                        wholeImage[(p.x) / 2 + 240][-p.y / 2 + 320]+wholeImage[(p.x-2) / 2 + 240][-(p.y-1) / 2 + 320]+wholeImage[(p.x-2) / 2 + 240][-(p.y+1) / 2 + 320]) / 5;

                int v2 = (wholeImage[(p.x - 2) / 2 + 240][-p.y / 2 + 320] + wholeImage[(p.x + 2) / 2 + 240][-p.y / 2 + 320]+
                        wholeImage[(p.x) / 2 + 240][-(p.y-1) / 2 + 320]+wholeImage[(p.x) / 2 + 240][-(p.y+1) / 2 + 320]) / 4;
                int v3 = (wholeImage[(p.x - 2) / 2 + 240][-p.y / 2 + 320] + wholeImage[(p.x) / 2 + 240][-p.y / 2 + 320] + wholeImage[(p.x + 2) / 2 + 240][-p.y / 2 + 320]) / 3;

                paint.setColor(Color.HSVToColor(new float[]{1, 0, v1 / 255f}));
                canvas.drawPoint((p.x - 2) / 2 + 240, -p.y / 2 + 320, paint);
                paint.setColor(Color.HSVToColor(new float[]{1, 0, v2 / 255f}));
                canvas.drawPoint((p.x) / 2 + 240, -p.y / 2 + 320, paint);
                paint.setColor(Color.HSVToColor(new float[]{1, 0, v3 / 255f}));
                canvas.drawPoint((p.x + 2) / 2 + 240, -p.y / 2 + 320, paint);
            }
           }*/
        } catch (Throwable t) {
            Log.e("BEV", "er", t);

        }

    }
// kameras novietojuma parametri, attiecībā pret plakni
    double sinb = 0.79863551;
    double ooo = 1745;
    float oob = 5244;
    float l = Y_CAMERA_OFFSET + Y_PROJECTION_MM;
    float f = 602;
    double sinafi = 0.601815023;
    List<float[]> trajectory = new ArrayList<>(40);
    Point[] cornersVgp = new Point[]{new Point(-290, 0), new Point(290, 0), new Point(45, 720), new Point(-45, 720)};
    Point[] reducedCorners = new Point[]{new Point(-250, 30), new Point(250, 30), new Point(30, 690), new Point(-30, 690)};

    // ievieto kameras attēlu data kartes piramīdā, balstoties uz xr,yr,angle - platformas novietojumu, un uzzīmē ekrānā
    void updateMapDirect(byte[] data, Canvas canvas, int xr, int yr, double angle) {
        // synchronized(lock){
        //  wholeImage = new short[640][480];
        trajectory.add(new float[]{xr, yr, (float) angle});
        fragmentCounter++;
        try {


            double cTh = cos(angle - devindesmit);   //robota leņķis kartē
            double sTh = Math.sin(angle - devindesmit);

            Point[] corners = new Point[4];
            Point[] rdcCorners = new Point[4];

            //tiešā transformācija lai noteiktu četrstūra koordinātes globālajā kartē
            // no punktiem vgp
            for (int i = 0; i < cornersVgp.length; i++) {
                Point c = cornersVgp[i];
                //  int x = (int)(xr+c.x*cTh-c.y*sTh);
                // int y = (int) (l-yr-c.y*cTh-c.x*sTh);
                int x = (int) (xr + c.x * cTh + (c.y - l) * sTh);
                int y = (int) (yr + (l - c.y) * cTh + c.x * sTh);

                corners[i] = new Point(x, y);
//samazinātajai figurai
                Point c2 = reducedCorners[i];

                x = (int) (xr + c2.x * cTh + (c2.y - l) * sTh);
                y = (int) (yr + (l - c2.y) * cTh + c2.x * sTh);
                rdcCorners[i] = new Point(x, y);


            }
            boolean errorShown = false;
            //  float xCam=f*;
            int[][] lines = calcLines(corners);
            int[] yLimits = getYlimits(corners);
            int actualYsize = yLimits[1] - yLimits[0];
//nominālie izmēri attēla masīvam- lai dalās ar 16 - četrām piramīdas kārtām.
            int[] xLimitsWhole = getXlimits(corners);
            int actualXsize = xLimitsWhole[1] - xLimitsWhole[0];
            int nominalXsize = nextNominalSize(actualXsize);
            jaunsFragments = new short[nominalXsize][nextNominalSize(actualYsize)];
            for (short[] s : jaunsFragments) {
                Arrays.fill(s, (short) 255);

            }
            int jaunsFragmentsYCounter = 0;
            //inversā transformācija- katram globālās kartes punktam četrstūrī atrod atbilstošo punktu
            // kameras attēlā
            Log.d("beye", "Ylim " + yLimits[0] + " - " + yLimits[1]);
            for (int yg = yLimits[0]; yg < yLimits[1]; yg++) {

                int[] xLimits = getXlimits(yg, lines);

                //   jaunsFragments[jaunsFragmentsYCounter] = new short[nominalXsize];
                // jaunsFragments[jaunsFragmentsYCounter][0] = (short) yg;
                //jaunsFragments[jaunsFragmentsYCounter][1] = (short) xLimits[0];

                for (int xg = xLimits[0]; xg < xLimits[1]; xg++) {

                    //atrod punkta koordinātes virtuālajā grīdas plaknē robota priekšā
                    double pox = (xg - xr) * cTh + (-yr + yg) * sTh;
                    double poy = (l + (-xr + xg) * sTh + (yr - yg) * cTh);
// un no tām atrod koordinātes kameras attēlā
                    //  canvas.drawPoint(xg/2 + 240, -yg/2 + 440, paint);


                    double xCam = f * 8.15 * pox / (ooo + (oob - 8.53 * poy) * sinb);
                    double yCam = f * (oob - 8.53 * poy) * sinafi / (ooo + (oob - 8.53 * poy) * sinb);

                    // double xCam = f*pox/(ooo+(oob-poy)*sinb);
                    //double yCam = f*(oob-poy)*sinafi/(ooo+(oob-poy)*sinb);
// tā kā kamera ir pagriezta un x0 ir attēla malā nevis centrā
                    int yc = -(int) xCam + 240;
                    int xc = 320 - (int) yCam;

                    if ((640 * (yc) + xc) > 400000) {
                        Log.e("beye", "xr= " + xr + " yr= " + yr + "tetha : " + Math.toDegrees(angle - devindesmit));

                        Log.e("beye", "corners " + corners[0] + corners[1] + corners[2] + corners[3]);
                        Log.e("beye", "xg= " + xg + " yg= " + yg);
                        Log.e("beye", "pox= " + pox + " poy= " + poy);
                        Log.e("beye", "xCam= " + xCam + " ycam= " + yCam);
                        Log.e("beye", "xc= " + xc + " yc= " + yc);
                    }

                    if ((640 * (yc) + xc) > 0) {
                        short yValue = data[640 * (yc) + xc];
                        if (yValue < 0) yValue += 255;
                        //int targetColor = attels.krasuTabula[data[((yc / 2 + 480) * 640 + (xc & 0b1111111111111110) + 1)] + 128][data[((yc / 2 + 480) * 640 + (xc & 0b1111111111111110))] + 128];
                        //    if (xg > 0 && xg < 640 && yg > 0 && yg < 480) {
                        // if (wholeImage[xg][yg] != 0)
                        //   yValue = (short) ((yValue + wholeImage[xg][yg]) / 2);
                        //  jaunsFragments[xg - xLimitsWhole[0]][jaunsFragmentsYCounter] = yValue;
                        //        wholeImage[xg][yg] = yValue;
                        //    }
                        if (xg - xLimitsWhole[0] > 0 && xg - xLimitsWhole[0] < jaunsFragments.length)
                            jaunsFragments[xg - xLimitsWhole[0]][jaunsFragmentsYCounter] = yValue;

                        //   int color = attels.yuvToRGB(yValue, data[((yc / 2 + 480) * 640 + (xc & 0b1111111111111110) + 1)], data[((yc / 2 + 480) * 640 + (xc & 0b1111111111111110))]);

                        //paint.setColor(color);
                        //canvas.drawPoint(xg + 240, -yg + 740, paint);

                    } else if (!errorShown) {

                        Log.e("beye", "corners " + corners[0] + corners[1] + corners[2] + corners[3]);
                        errorShown = true;
                    }
                }
                jaunsFragmentsYCounter++;
            }
//izveido piramīdu no fragmenta
            Pyramid fragments = new Pyramid(jaunsFragments);
            // Point center = findCenter(xLimitsWhole,yLimits);
            //  corners = reduceArea(corners,center,0.9);
            wholeImagePy.merge2(fragments, new Point(xLimitsWhole[0], yLimits[0]), rdcCorners);
            wholeImagePy.reconstruct();
            wholeImagePy.draw(wholeImagePy.g0, xLimitsWhole, yLimits, canvas, 0, trajectory);
            if (trajectory.size() > 2) {//zīmē trajektoriju
                for (int i = 1; i < trajectory.size(); i++) {
                    float[] first = trajectory.get(i - 1);
                    float[] second = trajectory.get(i);
                    canvas.drawLine(first[0] / 2, (960 - first[1]) / 2, second[0] / 2, (960 - second[1]) / 2, redPaint);

                }
                for (int i = 1; i < trajectory.size(); i++) {// zimē pozīcijas
                    float[] second = trajectory.get(i);
                    canvas.drawCircle(second[0] / 2, (960 - second[1]) / 2, 5, blue);
                   canvas.drawLine(second[0] / 2, (960 - second[1]) / 2, (float) (20 * Math.cos(second[2])) + second[0] / 2, (float) -(20 * Math.sin(second[2])) + (960 - second[1]) / 2, blue);

                }
            }
            paint.setColor(Color.WHITE);
            canvas.drawRect(0, 0, 200, 35, paint);
            paint.setColor(Color.LTGRAY);

            canvas.drawText("Fragmentu skaits: " + fragmentCounter, 10, 15, paint);

        } catch (Throwable t) {
            Log.e("BEV", "er", t);

        }

    }
    /* byte[] getImage() {
            byte[] res = new byte[640 * (480 + 240)];
            int counter = 0;
            for (int y = 0; y < wholeImage[0].length; y++) {
                for (int x = 0; x < wholeImage.length; x++) {

                    short s = wholeImage[x][y];
                    if (s > 127)
                        res[counter++] = (byte) (s - 255);
                    else
                        res[counter++] = (byte) (s);

                }
            }
            return res;
        }
    */

    void reset() {
        skersli = new HashSet<>(100000);


    }

    // atrod punktus līnijām, kas veidojas savienojot secīgi punktus corners
    static int[][] calcLines(Point[] corners) {
        int[][] res = new int[4][];    // 4 līnijas
        for (int i = 0; i < corners.length; i++) {
            Point corner = corners[i];

            int nextPoint = i + 1;
            if (i == corners.length - 1)
                nextPoint = 0;

            int startY = corner.y; // atrod augšējo un apakšējo punktu pec y
            int endY = corners[nextPoint].y;
            if (corner.y > corners[nextPoint].y) {
                startY = corners[nextPoint].y;
                endY = corner.y;
            }
            int lengthx = endY - startY + 2;// Math.abs(corner.y-corners[nextPoint].y);
            startY--;
            res[i] = new int[lengthx];
            res[i][0] = startY; // līnijas 0 pozīcijā ir sākuma y vērtība, pārējās - x vērtības

            for (int y = 1; y < lengthx; y++) {
                if (corners[nextPoint].y - corner.y != 0)
                    res[i][y] = (int) ((y + startY - corner.y) / (float) (corners[nextPoint].y - corner.y) * (corners[nextPoint].x - corner.x) + corner.x);
                else
                    res[i][y] = corner.x;
                //   System.out.println("x: "+res[i][y]+"  y: "+(y+startY));
            }


        }
        return res;
    }

    // atrod mazāko un lielāko x vērtības, kas ietilpst četrstūrī, y augstumā
    static int[] getXlimits(int y, int[][] lines) {
        int count = 0;
        int[] res = new int[]{0, 0};
        for (int i = 0; i < lines.length; i++) {
            int[] line = lines[i];
            if (y <= line[0] + line.length - 1 && y >= line[0] + 1)
                res[count++] = line[-line[0] + y];
            if (count >= 2)
                break;
        }
        if (res[0] > res[1]) {
            count = res[0];
            res[0] = res[1];
            res[1] = count;
        }
        return res;
    }

    //atrod mazāko un lielāko y vērtību no punktu masīva
    static int[] getYlimits(Point[] corners) {
        int[] ylim = new int[]{10000, -10000};
        for (int i = 0; i < corners.length; i++) {
            Point corner = corners[i];
            if (corner.y > ylim[1]) ylim[1] = corner.y;

            if (corner.y < ylim[0]) ylim[0] = corner.y;
        }
        return ylim;
    }

    //atrod mazāko un lielāko x vērtību no punktu masīva
    int[] getXlimits(Point[] corners) {
        int[] xlim = new int[]{10000, -10000};
        for (int i = 0; i < corners.length; i++) {
            Point corner = corners[i];
            if (corner.x > xlim[1]) xlim[1] = corner.x;

            if (corner.x < xlim[0]) xlim[0] = corner.x;
        }
        return xlim;

    }

    // atrod nākošo lielāko skaitli, kas dalās ar 16
    int nextNominalSize(int actualSize) {
        int iterations = 0;
        int curVal = actualSize;
        while (iterations < 16) {
            curVal = actualSize + iterations;
            int division = curVal / 16;
            if (division * 16 == curVal)
                break;
            iterations++;
        }
        return curVal;
    }

    //samazina četrstūra izmērus attiecībā pret centra punktu- figūra saraujas tās centra virzienā
    Point[] reduceArea(Point[] corners, Point center, double redFactor) {
        Point[] res = new Point[corners.length];
        for (int i = 0; i < corners.length; i++) {
            Point p = corners[i];
            //pārvieto koord sākumpunktu figūras centrā un samazina figūru, pec tam pārvieto sākumpunktu iepriekšējā pozīcijā
            int x = (int) ((p.x - center.x) * redFactor + center.x);
            int y = (int) ((p.y - center.y) * redFactor + center.y);
            res[i] = new Point(x, y);

        }

        return res;
    }

    // atrod četrstūra centru pēc min un max x un y vērtībām
    Point findCenter(int[] xLim, int[] yLim) {
        int x = (xLim[1] + xLim[0]) / 2;
        int y = (yLim[1] + yLim[0]) / 2;
        return new Point(x, y);


    }


}
