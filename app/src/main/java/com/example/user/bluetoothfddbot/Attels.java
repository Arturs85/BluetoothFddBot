package com.example.user.bluetoothfddbot;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;

import static java.lang.Math.atan;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.tan;
import static java.lang.Math.toRadians;

/**
 * Created by user on 2016.09.18..
 */
public class Attels {
    boolean[] ekstremi = new boolean[307200];
    boolean[] ekstremiVertikali;
    static HorizontalaLinija linijaH;
    byte[] datuMasivs;
    short[] yGroundMap;// short[groundYpx] = screenYpx
    short[] yScreenMap;//mas[sceenYPx] = groundYPx
    short[] yScreenMap320;
   short[] yGroundMap320;
    short[] yScreenMap160;
    short[] yGroundMap160;

    int summa1 = 0;
    int summa2 = 0;
    int summaY = 0;
    int skaits = 0;
    int vSlieksnis = 10;
    ArrayList<HorizontalaLinija> linijas;
    int spilgtumsDelta = 20;
    int[][] krasuTabula;

    //KrasainaBilde krasainaBilde;
    // EkstremuBilde ekstremuBilde;
    // EkstremuBilde vertikaluEkstremuBilde;
    // LinijuBilde linijuBilde;
    // TirasLinijas tiraBilde;
    ArrayList<HorizontalaLinija> mekletHorLinijas(byte[] datuMasivs) {
        int kolonna = 0;
        int rinda = 0;
        int sakumaKoordinateX = 0;
        int sakumaKoordinateY = 0;
        int linijasGarums = 0;
        linijas = new ArrayList<HorizontalaLinija>();
        // if (ekstremi==null){
        ekstremi = atrastEkstremus(datuMasivs, ekstremi,640,480);
        //   }
        boolean velkLiniju = false;
        for (int a = 0; a <= ekstremi.length - 2; a++) {

            if (ekstremi[a] == false && ekstremi[a + 1] == true) {
                velkLiniju = true;
                sakumaKoordinateX = kolonna;
                sakumaKoordinateY = rinda;

            }
            if (ekstremi[a] == true && ekstremi[a + 1] == true) {
                velkLiniju = true;
                linijasGarums++;

            }
            if (ekstremi[a] == true && ekstremi[a + 1] == false) {
                velkLiniju = false;
                if (linijasGarums > 100) {
//linijaH = new HorizontalaLinija(sakumaKoordinateX,sakumaKoordinateY,kolonna);
                    linijas.add(new HorizontalaLinija(sakumaKoordinateX, sakumaKoordinateY, kolonna));


                }
                linijasGarums = 0;
            }

            kolonna++;
            if (kolonna >= 640) {
                rinda++;
                linijasGarums = 0;
                kolonna = 0;
            }
        }

        return linijas;

    }

    //*
    ArrayList<HorizontalaLinija> atlasitLinijas(ArrayList<HorizontalaLinija> visasLinijas) {
        for (int a = 0; a <= visasLinijas.size() - 2; a++) {

            pieskaititBlakusliniju(a, visasLinijas);
            visasLinijas.get(a).sakumaX = summa1 / skaits;
            visasLinijas.get(a).beiguX = summa2 / skaits;
            visasLinijas.get(a).y = summaY / skaits;

            /// / int pieskaitamais = 1;

        }
        return visasLinijas;

    }

    void pieskaititBlakusliniju(int indeks, ArrayList<HorizontalaLinija> visasLinijas) {
        int pieskaitamais = 0;
        skaits = 1;
        summa1 = visasLinijas.get(indeks).sakumaX;
        summa2 = visasLinijas.get(indeks).beiguX;
        summaY = visasLinijas.get(indeks).y;
        //ignoreet tajaa pat rindaa esossaas liinijas
        do {
            pieskaitamais++;


        }
        while (visasLinijas.get(indeks).y == visasLinijas.get(indeks + pieskaitamais).y && indeks + pieskaitamais < visasLinijas.size() - 2);


        // ja ir blakusliinija
        // ArrayList<HorizontalaLinija> visasLinijas ;

        if (visasLinijas.get(indeks + pieskaitamais).y <= visasLinijas.get(indeks).y + 2
                && visasLinijas.get(indeks + pieskaitamais).sakumaX < visasLinijas.get(indeks).beiguX
                && visasLinijas.get(indeks + pieskaitamais).beiguX > visasLinijas.get(indeks).sakumaX
                && indeks + pieskaitamais < visasLinijas.size() - 2) {
            // indeks++;
            summa1 = summa1 + visasLinijas.get(indeks + pieskaitamais).sakumaX;
            summa2 = summa2 + visasLinijas.get(indeks + pieskaitamais).beiguX;
            summaY = summaY + visasLinijas.get(indeks + pieskaitamais).y;
            skaits++;


            pieskaititBlakusliniju(indeks + pieskaitamais + 1, visasLinijas);
            visasLinijas.remove(indeks + pieskaitamais);

            return;
        } else

            return;

    }

    //*/
    boolean[] atrastVisusEkstremus(byte[] datuMasivs) {
        ekstremi = new boolean[307200];
        ekstremi = atrastEkstremus(datuMasivs, ekstremi,640,480);
        ekstremi = atrastVertEkstremus(datuMasivs, ekstremi);
        return ekstremi;
    }

    boolean[] atrastEkstremus(byte[] dati, boolean[] ekstremi,int width,int heigth) {
        // boolean[] ekstremi =   new boolean[245760];
        int greySize = heigth*width;
        int limitedSize = greySize-4*width-1;
        for (int a = 0; a < limitedSize; a = a + 1) {
            if (noteiktMalu(dati[a], dati[a + width], dati[a + width * 2], dati[a + width * 3])) {
                ekstremi[a] = true;
            } else {
                if (noteiktMalu(dati[a], dati[a + 1], dati[a + 2], dati[a + 3])) {
                    ekstremi[a] = true;
                } else
                    ekstremi[a] = false;
            }

        }
        return ekstremi;
    }

    boolean[] atrastEkstremusFast(byte[] dati, boolean[] ekstremi,int width,int heigth) {//salidzina 2 punktus pa vert un horiz linijam, nemot visus px pec kartas

        //boolean[][] ekstremi = new boolean[160][120];
        int x = 0;
        int y = 0;
        int greySize = heigth*width;
        int limitedSize = greySize-4*width-1;

        for (int a = 0; a < limitedSize; a = a + 1) {
            if (noteiktMalu(dati[a], dati[a + width])) {
                ekstremi[a] = true;
            } else {
                if (noteiktMalu(dati[a], dati[a + 1])) {
                    ekstremi[a] = true;
                } else
                    ekstremi[a] = false;
            }

        }
        return ekstremi;
    }


    boolean[][] atrastEkstremus160x120(byte[] dati, boolean[][] ekstremi) {

        //boolean[][] ekstremi = new boolean[160][120];
        int x = 0;
        int y = 0;

        for (int a = 0; a < 307200 - 2564; a = a + 4) {
            if (x >= 159) {
                x = 0;
                y++;
                a += 3 * 640 + 4;
            }
            if (noteiktMalu(dati[a], dati[a + 640 * 3])) {
                ekstremi[x][y] = true;

            } else {
                if (noteiktMalu(dati[a], dati[a + 4]))
                    ekstremi[x][y] = true;
                else
                    ekstremi[x][y] = false;

            }

            x++;
        }
        return ekstremi;
    }

    boolean[][] atrastAtspidumu160x120(byte[] dati, boolean[][] ekstremi) {
        ekstremi = new boolean[160][120];//;ai nav jāpārzīmē visi elementi

        boolean colorOn = false;
        //boolean[][] ekstremi = new boolean[160][120];
        int x = 0;
        int y = 0;
//for(int a = 0;a<120;a++){//parbauda katru rindu
        //int col = 640;
        //while(col>10){

        // }

//}


        for (int a = 307200; a > 2004; a = a - 4) {
            if (x >= 159) {
                x = 0;
                y++;
                a -= 3 * 640 + 4;
                colorOn = false;
            }

            // if (dati[a] < dati[a - 4] - spilgtumsDelta)
            //   colorOn = false;
            //else if (dati[a] > dati[a - 4] + spilgtumsDelta)
            //  colorOn = true;
            short yp = dati[a - 4];
            if (yp < 0) yp += 255;
            short yp2 = dati[a - 8];
            if (yp2 < 0) yp2 += 255;
            if (Math.abs(yp - yp2) < 20) {//no edge
                //if (yp2>spilgtumsDelta)
                //  ekstremi[159 - x][119 - y] = true;
                //else
                //  ekstremi[159 - x][119 - y] = false;

            } else {// pāriet uz naakosso liiniju
                a = a - (158 - x) * 4;
                ekstremi[159 - x][119 - y] = true;//atziimee arii edge

                x = 158;

            }


            x++;
        }
        // Log.d("Attels","return");

        return ekstremi;
    }


    //returns masiivu ar celjam nederigiem punktiem izmantojot kadra apaksas krasu histogramu
    Canvas atrastVirsmuLoRes(Canvas canvas, Paint paint, int[] hist, byte[] data) {
        int rinda = 1;
        int kolonna = 1;
        int uvRinda = 480;
        int vKolonna = 1;
        Paint caursp = new Paint();
        caursp.setAlpha(0);
        for (int a = 0; a < 307200 - 2000; a = a + 2) { // 640x384


            if (kolonna >= 641) {
                rinda = rinda + 1;
                a = a + 640;
                kolonna = 1;
                vKolonna = 1;
            }


            int v = data[((rinda + 480) * 640 + kolonna - 1)];
            if (v < 0) v += 127;
            else v -= 128;

            int u = data[((rinda + 480) * 640 + (kolonna))];
            if (u < 0) u += 127;
            else u -= 128;

            if (!(Math.abs(v) < 2 && Math.abs(u) < 2)) {
                double hue = Math.atan2(v, u);
                hue = Math.toDegrees(hue);
                int hue2 = (int) hue;
                if (hue2 < 0)
                    hue2 = 360 + hue2;
                hue2 = hue2 - 90;
                if (hue2 < 0)
                    hue2 = 360 + hue2;
                if (hue2 > 361) {
                    System.out.println(hue2);
                    hue2 = 360;
                }
                // g.setColor(Color.black);

                //g.setColor(yuvToRGB(y, v, u));
                if (hist[hue2] < vSlieksnis)
                    canvas.drawRect(kolonna, 2 * rinda, kolonna + 2, 2 * rinda + 2, paint);

                    // masivs[a]=true;
                else
                    canvas.drawRect(kolonna, 2 * rinda, kolonna + 2, 2 * rinda + 2, caursp);
                // masivs[a]=false;

                // g.drawLine(kolonna, rinda, kolonna , rinda );
                // g.drawLine(400-rinda,kolonna, 401-rinda,kolonna+1);
            }
            kolonna = kolonna + 2;


        }
        return canvas; //masivs;
    }

    //returns masiivu ar celjam nederigiem punktiem izmantojot kadra apaksas krasu histogramu
    boolean[][] atrastVirsmu(boolean[][] skersluPx, int[] hist, byte[] data) {
        int rinda = 1;
        int kolonna = 1;
        int uvRinda = 480;
        int vKolonna = 1;
        Paint caursp = new Paint();
        caursp.setAlpha(0);
        for (int a = 0; a < 307200 - 2000; a = a + 2) { // 640x384

            if (kolonna >= 641) {
                rinda = rinda + 1;
                a = a + 640;
                kolonna = 1;
                vKolonna = 1;
            }


            int v = data[((rinda + 480) * 640 + kolonna - 1)];
            if (v < 0) v += 127;
            else v -= 128;

            int u = data[((rinda + 480) * 640 + (kolonna))];
            if (u < 0) u += 127;
            else u -= 128;

            if (!(Math.abs(v) < 2 && Math.abs(u) < 2)) {
                double hue = Math.atan2(v, u);
                hue = Math.toDegrees(hue);
                int hue2 = (int) hue;
                if (hue2 < 0)
                    hue2 = 360 + hue2;
                hue2 = hue2 - 90;
                if (hue2 < 0)
                    hue2 = 360 + hue2;
                if (hue2 > 361) {
                    System.out.println(hue2);
                    hue2 = 360;
                }
           /* if (kolonna >= 641) {
                rinda=rinda+1;
                if ((rinda & 1) == 1){ // paara rindas gadiijumaa
                    uvRinda++;
                    //System.out.println(uvRinda+" "+vKolonna+" "+rinda);
                }
                kolonna = 1;
                vKolonna = 1;
            }


            int v = data[(uvRinda * 640 + vKolonna-1) ];
            if(v < 0) v += 127; else v -= 128;

            int u = data[(uvRinda * 640 + (vKolonna) )];
            if(u < 0) u += 127; else u -= 128;

            if(!(Math.abs(v)<2 && Math.abs(u)<2)){
                double hue = Math.atan2(v, u);
                hue = Math.toDegrees(hue);
                int hue2 = (int)hue;
                if (hue2<0)
                    hue2 = 360 + hue2;
                hue2 = hue2-90;
                if(hue2<0)
                    hue2 = 360 + hue2;
                if(hue2 >361){
                    System.out.println(hue2);
                    hue2 = 360;
                }*/
                // g.setColor(Color.black);

                //g.setColor(yuvToRGB(y, v, u));
                if (hist[hue2] < vSlieksnis)
                    skersluPx[(kolonna - 1) / 2][(rinda - 1)] = true;
                    // masivs[a]=true;
                else
                    skersluPx[(kolonna - 1) / 2][(rinda - 1)] = false;
                // masivs[a]=false;

                // g.drawLine(kolonna, rinda, kolonna , rinda );
                // g.drawLine(400-rinda,kolonna, 401-rinda,kolonna+1);
            }
            kolonna = kolonna + 2;
            //  if((kolonna & 1)==1)
            //    vKolonna = vKolonna+2;

        }
        return skersluPx; //masivs;
    }

    // returns histogrammu, masiva indeksi atbilst hue graadiem
    int[] atrastKrasuHistogrammu(int[] hist, byte[] data) {
        int rinda = 0;
        int kolonna = 0;

        for (int i = 0; i < hist.length; i++) { //izdzēš iepr datus

            hist[i] = 0;
        }
        for (int i = (480 * 640) + 540; i < 720 * 640 - 200; i += 2) {

            if (kolonna >= 100) {
                kolonna = 0;
                rinda++;
                i += 540;
            }
            int u = (data[i]);
            int v = data[i + 1];
            if (u < 0) u += 127;
            else u -= 128;
            if (v < 0) v += 127;
            else v -= 128;
            double hue = Math.atan2(u, v);
            hue = Math.toDegrees(hue);
            int hue2 = (int) hue;
            if (hue2 < 0)
                hue2 = 360 + hue2;
            hue2 = hue2 - 90;
            if (hue2 < 0)
                hue2 = 360 + hue2;

//data[i] = 0;
            //if(y < 0) y += 255;
            hist[hue2]++;

            kolonna += 2;
        }
        return hist;
    }

    boolean[][] atrastKrasuLaukumus(byte[] dati, boolean[][] ekstremi) {

        //boolean[][] ekstremi = new boolean[160][120];
        int x = 0;
        int y = 0;

        for (int a = 307200; a < 460800 - 1400; a = a + 2) {
            if (x >= 640) {
                x = 0;
                y += 2;
                // a += 3 * 640 + 4;
            }
            int v = dati[a];
            if (v < 0) v += 127;
            else v -= 128;
            if (v > vSlieksnis) {
                ekstremi[x][y] = true;
                ekstremi[x + 1][y] = true;
                ekstremi[x + 1][y + 1] = true;
                ekstremi[x][y + 1] = true;
            } else {
                ekstremi[x][y] = false;
                ekstremi[x + 1][y] = false;
                ekstremi[x + 1][y + 1] = false;
                ekstremi[x][y + 1] = false;

            }
            x += 2;
        }
        return ekstremi;
    }

    boolean[] atrastVertEkstremus(byte[] dati, boolean[] ekstremi) {
        // boolean[] ekstremiVertikali =   new boolean[245760];
        for (int a = 0; a < 307200 - 4; a = a + 1) {
            if (noteiktMalu(dati[a], dati[a + 1], dati[a + 2], dati[a + 3])) {
                ekstremi[a] = true;
            }
            //else
            //  ekstremiVertikali[a]=false;

        }
        return ekstremi;
    }

    boolean noteiktMalu(int a, int a1, int a2, int a3) {
        boolean irMala = false;
        if (a + a1 < a2 + a3 - spilgtumsDelta || a + a1 > a2 + a3 + spilgtumsDelta)
            irMala = true;


        return irMala;
    }

    boolean noteiktMalu(int a, int a1) {
        boolean irMala = false;
        if (a < a1 - spilgtumsDelta || a > a1 + spilgtumsDelta)
            irMala = true;


        return irMala;
    }

    boolean[] atrastOtrasKartasEkstremus(boolean[] pirmasKEkst) {
        for (int a = 0; a < pirmasKEkst.length - 641; ++a) {
            if (pirmasKEkst[a] == false && pirmasKEkst[a + 1] == true)
                pirmasKEkst[a] = true;
            else {
                if (pirmasKEkst[a] == false && pirmasKEkst[a + 640] == true)
                    pirmasKEkst[a] = true;
                else
                    pirmasKEkst[a] = false;

            }

        }
        return pirmasKEkst;
    }

    //pagaidām taisa jaunu divdimensiju masīvu no viendimensiju masīva
    boolean[][] atrastOtrasKartasEkstremus2D(boolean[] pirmasKEkst, boolean[][] ekstremi) {
        // boolean[][] ekstremi = new boolean[640][480];
        int x = 0;
        int y = 0;

        for (int a = 0; a < 307200 - 640; ++a) {
            if (x >= 640) {
                x = 0;
                ++y;
            }
            if (pirmasKEkst[a] == false && pirmasKEkst[a + 1] == true)
                ekstremi[x][y] = true;
            else {
                if (pirmasKEkst[a] == false && pirmasKEkst[a + 640] == true)
                    ekstremi[x][y] = true;
                else
                    ekstremi[x][y] = false;
            }
            x++;
        }

        return ekstremi;
    }

    ArrayList<Point> atrastTaisnasLinijas(boolean[][] otrasKartasEkst) {
        ArrayList<Point> points = new ArrayList<>();
        for (int y = 0; y <= 479; y++) {
            for (int x = 0; x <= 639; ++x) {             //pārbauda visus punktus masīvā

                if (otrasKartasEkst[x][y]) {
                    Point sakumpunkts = new Point(x, y);
                    Point[] galapunkti = new Point[4];

                    galapunkti = pievienotPirmosPunktus(galapunkti, sakumpunkts, otrasKartasEkst);
                    //Point beiguPunkts = pievienotBlakusPunktu(1.5, sakumaPunkts, sakumaPunkts, new Point(1000, 1000), (short) 0, otrasKartasEkst);

                    for (int i = 0; i < 4; i++) {

                        if (galapunkti[i] != null) {
                            points.add(sakumpunkts);
                            points.add(new Point(galapunkti[i]));
                        }
                    }
                }
            }
        }

        return points;
    }

    Point[] pievienotPirmosPunktus(Point[] galaPunkti, Point point, boolean[][] otrasKartasEkst) {
        if (point.x >= 638 || point.y >= 478 || point.x <= 1) {
            galaPunkti[0] = null;
            galaPunkti[1] = null;
            galaPunkti[2] = null;
            galaPunkti[3] = null;

            return galaPunkti;
        }

        if (otrasKartasEkst[point.x - 1][point.y + 1]) { //punktsSW ????
            //System.out.println(point.x);

            galaPunkti[0] = pievienotBlakusPunktu(230, point, new Point(point.x - 1, point.y + 1), new Point(1000, 1000), (short) 0, otrasKartasEkst);
        }
        if (otrasKartasEkst[point.x][point.y + 1]) { //punktsW?
            //System.out.println(point.x);

            galaPunkti[1] = pievienotBlakusPunktu(150, point, new Point(point.x, point.y + 1), new Point(1000, 1000), (short) 0, otrasKartasEkst);
        }

        if (otrasKartasEkst[point.x + 1][point.y])//&&(point.x+1!=previous.x &&point.y!=previous.y))vai null, ja blakus punktu nav
            galaPunkti[3] = pievienotBlakusPunktu(30, point, new Point(point.x + 1, point.y), new Point(1000, 1000), (short) 0, otrasKartasEkst);

        if (otrasKartasEkst[point.x + 1][point.y + 1]) //punktsSE
            galaPunkti[2] = pievienotBlakusPunktu(50, point, new Point(point.x + 1, point.y + 1), new Point(1000, 1000), (short) 0, otrasKartasEkst);

        return galaPunkti;
    }


    Point pievienotBlakusPunktu(int vidVirziens, Point sakumaPunkts, Point p, Point pMinus1, short punktuSkaits, boolean[][] otrasKartasEkst) { //ja virziens ir derīgs, tad meklēt nākamo punktu
        Point blakuspunkts = mekletBlakuspunktu(p, pMinus1, otrasKartasEkst); //blakuspunkts--> tekošais punkts															// jan nav derīgs, tad return galapunktu
        if (blakuspunkts != null) {
            //if(punktuSkaits<40)
            //return null;
            //else
            //{

            ////return p;
            //}
            ++punktuSkaits;
            int siPunktaVirzRad = (int) (100 * Math.atan2(blakuspunkts.y - sakumaPunkts.y, blakuspunkts.x - sakumaPunkts.x));//virziens rad
            //int siPunktaVirziens = (blakuspunkts.y-sakumaPunkts.y)*100/(blakuspunkts.x-sakumaPunkts.x+1);
            //System.out.println(siPunktaVirzRad+"psk: "+punktuSkaits);
            if (Math.abs(siPunktaVirzRad - vidVirziens) < 120 / punktuSkaits)// virziena izmaiņa ir pieņemama
            {

                int vidVirz = (siPunktaVirzRad + vidVirziens) / 2; // aprēķina jaunu vidējo virzienu ņemot vērā šo punktu
                p = pievienotBlakusPunktu(vidVirz, sakumaPunkts, blakuspunkts, p, punktuSkaits, otrasKartasEkst); //meklē nākošo punktu
                // if(punktuSkaits > 20)
                otrasKartasEkst[blakuspunkts.x][blakuspunkts.y] = false; // izdēš pievienoto punktu, lai to nepārbaudītu otrreiz

                return p;
            }
        }

        if (punktuSkaits < 40)
            p = null;

        return p;

    }

    Point mekletBlakuspunktu(Point point, Point previous, boolean[][] otrasKartasEkst) { //atgriež punktu Point ja tāds atrodas blakus dotajam punktam
        if (point.x >= 638 || point.y >= 478 || point.x <= 0)
            return null;
        //System.out.println(point.x);

        if (otrasKartasEkst[point.x - 1][point.y + 1]) { //punktsSW ????
            //System.out.println(point.x);

            return new Point(point.x - 1, point.y + 1);

        }
        if (otrasKartasEkst[point.x - 1][point.y] && (point.x - 1 != previous.x)) { //punktsW?
            //System.out.println(point.x);

            return new Point(point.x - 1, point.y);
        }

        if (otrasKartasEkst[point.x + 1][point.y])//&&(point.x+1!=previous.x &&point.y!=previous.y))		//punktsE						// vai null, ja blakus punktu nav
            return new Point(point.x + 1, point.y);

        if (otrasKartasEkst[point.x + 1][point.y + 1]) //punktsSE
            return new Point(point.x + 1, point.y + 1);
        if (otrasKartasEkst[point.x][point.y + 1]) //punktsS
            return new Point(point.x, point.y + 1);

        return null;
    }

    ArrayList<Point> apvienotPx(boolean[][] skersluPx, int radius, int blivumsProc) {
        int summXass = 0;
        int summYass = 0;
        ArrayList<Point> centri = new ArrayList<>();
       /* for (int y = radius; y <= 479-radius; y+=2*radius) {
            for (int x = radius; x <= 639-radius; x+=2*radius) {
                for(int a= -radius;a<=radius;++a){
                    if(skersluPx[x+a][y])
                        ++summXass;
                    if(skersluPx[x][y+a])
                        ++summYass;

                }

                if(summXass*100>=2*radius*blivumsProc&&summYass*100>=2*radius*blivumsProc)
                    centri.add(new Point(x,y));
                summXass=0;
                summYass=0;

            }}*/
        for (int y = radius; y <= 240 - radius; y += 2 * radius) {
            for (int x = radius; x <= 319 - radius; x += 2 * radius) {
                for (int a = -radius; a < radius; ++a) {
                    if (skersluPx[x + a][y])
                        ++summXass;
                    if (skersluPx[x][y + a])
                        ++summYass;

                }

                if (summXass * 100 >= 2 * radius * blivumsProc && summYass * 100 >= 2 * radius * blivumsProc)
                    centri.add(new Point(x * 2, y * 2));
                summXass = 0;
                summYass = 0;

            }
        }
        return centri;
    }


    Canvas drawToGroundPlane(byte[] dati, Canvas canvas, Paint paint) {
        for (int x = 0; x < 720; x++) {
            int draft = (int) (x * 0.34);
            int pxSkaitsRindaa = 580 - 2 * draft;
            for (int y = draft; y < 580 - draft; y++) {
                short ye = yGroundMap[x];
                short xe = (short) ((y - draft) * 480 / pxSkaitsRindaa);
                //  Log.d("Attels"," ye "+ye+  " xe "+xe);


                byte yValue = dati[640 * (xe) + ye];
                if (yValue < 0) yValue += 255;
                paint.setColor(Color.argb(250, yValue, 100, 100));
                canvas.drawPoint(x, y, paint);


            }
        }

        // Log.d("Attels"," ----------------          ------return canvas ");

        return canvas;
    }

    void calculateYMap() { // izveido tabulu ar px paarnesi
        yGroundMap = new short[720];
        for (int i = 0; i < 720; i++) {
            double ygPx = 8.53 * i;
            double f = 5244 - ygPx;
            double ye = 320 - (602 * 0.6018 * f / (1745 + 0.7986 * f));


            yGroundMap[i] = (short) ye;
            // Log.d("Attels"," groundY = "+i+ " screenY = "+yGroundMap[i]);
        }
    }


    void calculateYScreenMap() {//izveido tabulu kur indeksi ir ekraana y koordinaate, bet veertibas ir zemes y koordinate px
        yScreenMap = new short[640];

        for (int i = 0; i < 640; i++) {

            double fi = atan((320 - i) / 602);
            double alfa = 0.4887 - fi;
            double yG = 6716 * Math.sin(alfa) / Math.sin(2.9845 - alfa);
            yScreenMap[i] = (short) (yG / 8.53);// pāriet no px uz cm;

            // Log.d("Attels"," groundY = "+i+ " screenY = "+yGroundMap[i]);
        }

    }
    void calculateYGroundMap320() { // izveido tabulu ,kur indeksi ir zemes koordin, vērtības- ekrāna kordin
        yGroundMap320 = new short[720];
        for (int i = 0; i < 720; i++) {
            double ygPx = 4.266666667 * i;
            double f = 2619.805053 - ygPx;
            double ye = 160 - (300.9162345 * 0.601815023 * f / (872.9555408 + 0.79863551 * f));


            yGroundMap320[i] = (short) Math.round(ye);
            // Log.d("Attels"," groundY = "+i+ " screenY = "+yGroundMap[i]);
            // System.out.println(" groundY = "+i+ " screenY = "+yGroundMap320[i]);

        }
    }
    void calculateYScreenMap320() {//izveido tabulu kur indeksi ir ekraana y koordinaate, bet veertibas ir zemes y koordinate px
        yScreenMap320 = new short[320];

        for (int i = 0; i < 320; i++) {
            double fi = atan((160d - i)/300.9162345);
            double alfa = 0.48869219 - fi;
            double yG = 3358.324899 * Math.sin(alfa) / Math.sin(2.984513021 - alfa);
            yScreenMap320[i] = (short) Math.round(yG *0.234375);// pāriet no px uz mm;

            // Log.d("Attels"," groundY = "+i+ " screenY = "+yGroundMap[i]);
            //System.out.println(" screenY = "+i+ " groundY = "+yScreenMap320[i]+" fi: "+fi/radInOneDegree+" alfa: "+alfa/radInOneDegree);

        }

    }


    final int Y_CAM_OFFSET = 70; //mm
    final int GROUND_FRAME_HEIGTH = 720;//mm
    final int Y_OFFSET=Y_CAM_OFFSET+GROUND_FRAME_HEIGTH;

    boolean[][] nesakritibaIPM320(RawImageWithPosition imgPrew, RawImageWithPosition imgCur) {

        boolean[] ekst = atrastEkstremusFast(imgPrew.data,new boolean[320*240],320,240);

        boolean [][] res = new boolean[320][240];
        short xeCur = 0;
        short yeCur = 0;
        short xePrew = 0;
        short yePrew = 0;
        double cosa = cos( 1.570796327-imgCur.omega);//pagriež pašreizējā skatu punkta koordinātes, lai tās atbilstu globalajam
        double sina = Math.sin(1.570796327-imgCur.omega);
        double deltax = (imgPrew.x - imgCur.x) * cosa - (imgPrew.y - imgCur.y) * sina; //koordināu sist rotācija, lai parietu uz pedējā attēla koordinātēm
        double deltay = (imgPrew.x - imgCur.x) * sina + (imgPrew.y - imgCur.y) * cosa;
        double deltaFi = imgCur.omega - imgPrew.omega;  // iepriekšējā skatu punkta  leņķis attiecībā pret pašreizējo


        for (int i = 0; i < 76800; i++) {
//pašreizējā att punkta kordinaates zemes plaknē
//        short ygCur= yScreenMap[xeCur];// vertikālā koordināte
//short  xgCur = (short) ((xeCur-120)*(2.4167-0.002836*ygCur));

//ieprieksejā attēla punkta koordinātes tai pozīcijai atbilstošajā zemes plaknē
            short ygPrew = yScreenMap320[xePrew];// vertikālā koordināte, kameras koordinaates ir pagrieztas
            short xgPrew = (short) ((-yePrew + 120) * (2.4167 - 0.002836 * ygPrew));
            //ieprēkšējā attēla zemes plaknē, punktu pārnese pašreizējā attēla zemes plaknes koordinātēs
           // pagriež prew attēla koordinātu sistemu paraleli pašreizejā att sistemai t.i. pagriež punktus uz pretējo pusi
            // tā kā y ass 0 punkts nesakrīt ar rotācijas centru, tad jāpāriet, lai 0 punkts ir platformas rot. centrā un pēc tam atpakaļ
            short xgTrans = (short) (xgPrew*cos(-deltaFi)-(Y_OFFSET-ygPrew)*sin(-deltaFi)+deltax/0.7142);
            short ygTrans = (short)(Y_OFFSET-(xgPrew*sin(-deltaFi)+(Y_OFFSET-ygPrew)*cos(-deltaFi)+deltay/0.7142));

//atrod iepriekšējā attēla punkta novietojumu pašreizējā attēla ekrāna koordinātēs
if(ygTrans>=0&&ygTrans<720) {
    short xeTrans = yGroundMap320[ygTrans];
short yeTrans = (short) -(xgTrans/(2.4167 - 0.002836 * ygTrans)-120);

if(yeTrans>=0&&yeTrans<240){
    //no iepriekšējā kadra pārnestais punkts atrodas šī kadra laukumā
//spilgtuma vērtība apskatamajam punktam iepriekšējā kadrā
    byte prewVal = imgPrew.data[i];
    if (prewVal < 0) prewVal += 255;
// spilgtuma vērtība atbilstošajam punktam jaunajā kadrā

    //byte curVal = imgCur.data[xeTrans+yeTrans*320];
    //if (curVal < 0) curVal += 255;
//if(abs(curVal-prewVal)>30)
  if(ekst[i])
    res[xeTrans][yeTrans]=true;
//else
  //    res[xeTrans][yeTrans]=false;
}


}
            xePrew++;
            if (xePrew >= 320) {
                xePrew = 0;
                yePrew++;
            }

        }
        Log.d("Attels"," return nesakritiba");

        return res;
    }


    final double ALFA = Math.toRadians(28);
    final double SCREEN_HEIGHT = 75;
    final double SCREEN_H_PX = 160;
    final double BETA = toRadians(54);//kameras leņķis

    void calculateYGroundMap160() { // izveido tabulu ,kur indeksi ir zemes koordin, vērtības- ekrāna kordin
        yGroundMap160 = new short[720];
        double platatiLenkis = toRadians(90)-ALFA+BETA;
        double sauraisLenkis = toRadians(90)-ALFA-BETA;
        double normLenkis  = toRadians(90)+BETA;
        double malaG = GROUND_FRAME_HEIGTH*sin(platatiLenkis)/sin(2*ALFA);

        double ygUp = malaG*sin(ALFA)/sin(normLenkis);
        double normH = ygUp*sin(sauraisLenkis)/sin(ALFA);
        double h1 = SCREEN_HEIGHT/2/tan(ALFA);
        for (int i = 0; i < 720; i++) {

            double f = ygUp-i;
            double yemm = SCREEN_HEIGHT/2 - (h1*cos(BETA)* f / (normH + f*sin(BETA)));
double yepx = yemm*SCREEN_H_PX/SCREEN_HEIGHT;

            yGroundMap160[i] = (short) Math.round(yepx);
           //  Log.d("Attels"," groundY = "+i+ " screenY = "+yGroundMap160[i]);
            // System.out.println(" groundY = "+i+ " screenY = "+yGroundMap320[i]);

        }
    }
    void calculateYScreenMap160() {//izveido tabulu kur indeksi ir ekraana y koordinaate, bet veertibas ir zemes y koordinate px
        yScreenMap160 = new short[160];
        double platatiLenkis = toRadians(90)-ALFA+BETA;
        double sauraisLenkis = toRadians(90)-ALFA-BETA;
        double normLenkis  = toRadians(90)+BETA;
        double malaG = GROUND_FRAME_HEIGTH*sin(platatiLenkis)/sin(2*ALFA);

        double ygUp = malaG*sin(ALFA)/sin(normLenkis);
        double normH = ygUp*sin(sauraisLenkis)/sin(ALFA);
        double h1 = SCREEN_HEIGHT/2/tan(ALFA);

        for (int i = 0; i < 160; i++) {

            double fi = atan((SCREEN_HEIGHT*(80d-i)/SCREEN_H_PX )/h1);
            double alfa = ALFA - fi;
            double yG = malaG * Math.sin(alfa) / Math.sin(toRadians(180)-sauraisLenkis - alfa);
            yScreenMap160[i] = (short) Math.round(yG);// pāriet no px uz mm;

          //   Log.d("Attels"," groundY = "+i+ " screenY = "+yScreenMap160[i]);
           // System.out.println(" screenY = "+i+ " groundY = "+yScreenMap160[i]+" fi: "+fi/radInOneDegree+" alfa: "+alfa/radInOneDegree);

        }

    }


    boolean[][] nesakritibaIPM160(RawImageWithPosition imgPrew, RawImageWithPosition imgCur) {

        boolean[][] ekst = atrastEkstremus160x120(imgPrew.data,new boolean[160][120]);

        boolean [][] res = new boolean[160][120];
        short xeCur = 0;
        short yeCur = 0;
      //  short xePrew = 0;
        //short yePrew = 0;
        double cosa = cos( 1.570796327-imgCur.omega);//pagriež pašreizējā skatu punkta koordinātes, lai tās atbilstu globalajam
        double sina = Math.sin(1.570796327-imgCur.omega);
        double deltax = (imgPrew.x - imgCur.x) * cosa - (imgPrew.y - imgCur.y) * sina; //koordināu sist rotācija, lai parietu uz pedējā attēla koordinātēm
        double deltay = (imgPrew.x - imgCur.x) * sina + (imgPrew.y - imgCur.y) * cosa;
        double deltaFi = imgCur.gyroOmega - imgPrew.gyroOmega;  // iepriekšējā skatu punkta  leņķis attiecībā pret pašreizējo


      //  for (int i = 0; i < 307200; i=i+4) {
for(int xePrew =0; xePrew<160;xePrew++){
 //   Log.d("Attels","  x "+xePrew);

    for(int yePrew =0; yePrew<120;yePrew++){


//pašreizējā att punkta kordinaates zemes plaknē
//        short ygCur= yScreenMap[xeCur];// vertikālā koordināte
//short  xgCur = (short) ((xeCur-120)*(2.4167-0.002836*ygCur));

//ieprieksejā attēla punkta koordinātes tai pozīcijai atbilstošajā zemes plaknē
            short ygPrew = yScreenMap160[xePrew];// vertikālā koordināte, kameras koordinaates ir pagrieztas
            short xgPrew = (short) ((-yePrew + 60) * (4.833333 - 0.0056713 * ygPrew));
            //ieprēkšējā attēla zemes plaknē, punktu pārnese pašreizējā attēla zemes plaknes koordinātēs
            // pagriež prew attēla koordinātu sistemu paraleli pašreizejā att sistemai t.i. pagriež punktus uz pretējo pusi
            // tā kā y ass 0 punkts nesakrīt ar rotācijas centru, tad jāpāriet, lai 0 punkts ir platformas rot. centrā un pēc tam atpakaļ
            short xgTrans = (short) (xgPrew*cos(-deltaFi)-(Y_OFFSET-ygPrew)*sin(-deltaFi)+deltax);
            short ygTrans = (short)(Y_OFFSET-(xgPrew*sin(-deltaFi)+(Y_OFFSET-ygPrew)*cos(-deltaFi)+deltay));

//atrod iepriekšējā attēla punkta novietojumu pašreizējā attēla ekrāna koordinātēs
            if(ygTrans>=0&&ygTrans<720) {
                short xeTrans = yGroundMap160[ygTrans];
                short yeTrans = (short) -(xgTrans/(4.833333 - 0.0056713 * ygTrans)-60);

                if(yeTrans>=0&&yeTrans<120){
                    //no iepriekšējā kadra pārnestais punkts atrodas šī kadra laukumā
//spilgtuma vērtība apskatamajam punktam iepriekšējā kadrā
               //     byte prewVal = imgPrew.data[i];
                 //   if (prewVal < 0) prewVal += 255;
// spilgtuma vērtība atbilstošajam punktam jaunajā kadrā

                    //byte curVal = imgCur.data[xeTrans+yeTrans*320];
                    //if (curVal < 0) curVal += 255;
//if(abs(curVal-prewVal)>30)
                    if(ekst[xePrew][yePrew])
                        res[xeTrans][yeTrans]=true;
//else
                    //    res[xeTrans][yeTrans]=false;
                }


            }
          //  xePrew++;
           // if (xePrew >= 160) {
             //  i=i+640*3+4;
               // xePrew = 0;
                //yePrew++;
           // }

        }
}
        Log.d("Attels"," return nesakritiba");

        return res;
    }
    void atrastKrasuTabulu() {
        krasuTabula = new int[257][257];
        int uu, vv;
        for (int u = -128; u < 128; u++) {
            if (u < 0) uu = u + 127;
            else uu = u - 128;
            for (int v = -128; v < 128; v++) {
                if (v < 0) vv = v + 127;
                else vv = v - 128;


                int hue2 = 361;
                if (!(Math.abs(vv) < 5 && Math.abs(uu) < 5)) {
                    double hue = Math.atan2(vv, uu);
                    hue = Math.toDegrees(hue);
                    hue2 = (int) hue;
                    if (hue2 < 0)
                        hue2 = 360 + hue2;
                    hue2 = hue2 - 90;
                    if (hue2 < 0)
                        hue2 = 360 + hue2;
                    if (hue2 > 360) {
                        // System.out.println(hue2);
                        hue2 = 360;
                    }
                    hue2 = hue2 / 20;
                    hue2*=20;
                }
                krasuTabula[u + 128][v + 128] = hue2;
            }
        }

    }
    public int yuvToRGB(int y, int v, int u) {
        //int red = (y + (v) * 114 / 100)/2 ;
        //int green = ((y - 39 * (u) / 100 - (v) * 58 / 100) + 255)/2 ;
        //int blue = (y + 2 * (u)) / 3;
        //int red = (int)((y + (1.370705 * (v-128)))/2.4);
        //int green = (int)((y - (0.698001 * (v-128)) - (0.337633 * (u-128)))/2.1);
        // int blue = (int)((y + (1.732446 * (u-128)))/2.8);

        //int red = (int)(y + (1.370705 * (v-128)));
        // int green = (int)(y - (0.698001 * (v-128)) - (0.337633 * (u-1128)));
        // blue = (int)(y + (1.732446 * (u-128)));
        if (u < 0) u = u + 127;
        else u = u - 128;
        if (v < 0) v = v + 127;
        else v = v - 128;


        int red = y + u + (u >> 2) + (u >> 3) + (u >> 5);
        int green = y - (v >> 2) + (v >> 4) + (v >> 5) - (u >> 1) + (u >> 3) + (u >> 4) + (u >> 5);
        int blue = y + v + (v >> 1) + (v >> 2) + (v >> 6);


        if (red < 0 || red > 255) {
            red = 255;
        }
        if (green < 0 || green > 255) {
            green = 255;
        }
        if (blue < 0 || blue > 255) {
            blue = 255;
        }
        int krasa = Color.rgb(red, green, blue);

        return krasa;
    }

    //Attels apakssklase
    class HorizontalaLinija {
        int sakumaX;
        int y;
        int beiguX;

        HorizontalaLinija(int sakumaX, int y, int beiguX) {
            this.sakumaX = sakumaX;
            this.y = y;
            this.beiguX = beiguX;

        }

        int getGarums() {
            int garums = 0;
            garums = beiguX - sakumaX;
            return garums;
        }
    }


}
