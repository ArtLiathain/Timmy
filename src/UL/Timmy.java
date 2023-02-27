package UL;
import org.apache.bcel.generic.ReturnaddressType;
import robocode.*;
import robocode.Robot;
import robocode.util.Utils;

import java.awt.*;
import java.util.Random;

import static java.lang.Math.PI;
import static java.lang.Math.abs;

public class Timmy extends Robot {
    public int sentryX = -1;
    public int sentryY = -1;
    int xIndex = -2;
    int yIndex = -1;
    boolean SentryScanned = false;
    double[] radarP = new double[2];
    Random random = new Random();
    double gunPoint;
    int bound = 5;

    double scannerRotation[] = new double[2];
    public void move(double[] locations){
    while(getEnergy() > 0) {

        if (xIndex < 4) {
            xIndex += 2;
            yIndex += 2;
        } else {
            xIndex = 0;
            yIndex = 1;
        }
        goTo(locations[xIndex], locations[yIndex]);
        moveGun();
        normalScan(radarP);
    }
    }

    public void moveGun(){
        double realGunPoint = getGunHeading();
        if(realGunPoint == gunPoint){
            return;
        }
        else if(realGunPoint > gunPoint+20){
            turnGunLeft(30);
        }
        else if(realGunPoint < gunPoint-20){
            turnGunRight(30);
        }


    }
    public double[] getPositions(double paddingside, double paddingmid){
        double SentryBorder = getSentryBorderSize();
        double border_x = getBattleFieldWidth();
        double border_y = getBattleFieldHeight();
        double midX = border_x/2;
        double midY = border_y/2;
        double tempx = border_x - paddingside;
        double tempy = border_y -paddingside;

        if(sentryX == -1 &&  sentryY == -1){
            this.out.println("BOTTOM LECT");
            double[] locations = {paddingside,paddingside,paddingside, midY -paddingmid,midX -paddingmid,paddingside};
            radarP[0] = 0;
            radarP[1] = 90;
            gunPoint = 45;
            return locations;
        }
        else if(sentryX == -1 && sentryY == 1){
            this.out.println("TOP LEFT");
            double[] locations = {paddingside,tempy,midX - paddingmid, tempy,paddingside,midY + paddingmid};
            radarP[0] = 90;
            radarP[1] = 180;
            gunPoint = 135;
            return locations;
        }
        else if(sentryX == 1 && sentryY == -1){
            this.out.println("BOTTOM RIGHT");
            double[] locations = {tempx,paddingside,midX + paddingmid, paddingside,tempx,midY - paddingmid};
            radarP[0] = 270;
            radarP[1] = 360;
            gunPoint = 315;
            return locations;
        }
        else if(sentryX == 1 && sentryY == 1){
            this.out.println("TOP RIGHT");
            double[] locations = {tempx, tempy, tempx ,midY + paddingmid ,midX + paddingmid, tempy};
            radarP[0] = 180;
            radarP[1] = 270;
            gunPoint = 215;
            return locations;
        }
        return null;
    }

    public void run(){
        setAdjustRadarForRobotTurn(true);
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        superScan();
        move(getPositions(30, 100));
    }
    boolean scan = false;
    //inspiration form UL COFFEE
    public void superScan(){

        int max = 0;
        int degrees = 45;
        while(true){
            turnRadarRight(degrees);
            max += degrees;

            if(max > 360){
                break;
            }
        }
        double radarH = getRadarHeading();
        if(radarH > 180){
            turnRadarRight(360 - radarH);
        }
        else{
            turnRadarLeft(radarH);
        }
    }

    public void normalScan(double[] radarP){
        double radarH = getRadarHeading();
        if(radarP[1] == 360 && radarH ==0){
            radarH = 360;
        }
        int degrees = 45;
        if(radarH == radarP[0]){
            while(radarH < radarP[1]) {
                turnRadarRight(degrees);
                radarH += degrees;
            }
        } else if (radarH == radarP[1]) {
            while(radarH > radarP[0]) {
                turnRadarLeft(degrees);
                radarH -= degrees;
            }
        }
        else{
            while(radarH < radarP[0]) {
                turnRadarRight(degrees);
                radarH += degrees;
            }
        }
    }

    // yoinked from https://robowiki.net/wiki/GoTo and modified for Robot
    //further yoinked modified version from ULCoffee

    private void goTo(double x, double y) {
        double x_dest = x - getX();
        double y_dest = y - getY();
        out.println("going to" + x + " y:" + y);
        double goAngle = 0;

        double distance = Math.hypot(x_dest, y_dest);
        if (x_dest >= 0 && y_dest >= 0) {
            goAngle = Math.asin(x_dest / distance);
        }
        if (x_dest >= 0 && y_dest < 0) {
            goAngle = PI - Math.asin(x_dest / distance);
        }
        if (x_dest < 0 && y_dest < 0) {
            goAngle = PI + Math.asin(-x_dest / distance);
        }
        if (x_dest < 0 && y_dest >= 0) {
            goAngle = 2.0 * PI - Math.asin(-x_dest / distance);
        }

        // convert to degrees
        goAngle *= 57.2958;

        // compensate for teh current heading
        goAngle -= getHeading();

        // reduce turns if its possible
        if(goAngle < 0){
            goAngle += 360;
        }

        // easier to go teh other way if its turning right 3 times to go left
        if(goAngle > 270){
            goAngle -= 270;
            turnLeft(goAngle);
        }else {
            turnRight(goAngle);
        }

        if (random.nextInt(bound)%bound == 0){
            ahead(distance/2);
//            fire(1.1);
            ahead(distance/2);
        }
        else {
            ahead(distance);
        }

        //shoot shithere
        //this makes timmy die more


    }

    public void onScannedRobot(ScannedRobotEvent event){

        if(event.isSentryRobot() && !SentryScanned){

            double[] SentryPos = GetXY(event.getBearing(), getHeading(), event.getDistance());

            if (SentryPos[0] + getX() < 400){
                sentryX = 1;
            }
            if (SentryPos[1] + getY() < 400){
                sentryY = 1;
            }
            SentryScanned = true;
        }
        else if(!event.isSentryRobot()){

        }
    }

    public double[] GetXY(double bearing, double heading, double distance){
        double absBearing = bearing + heading;
        absBearing = Math.toRadians(absBearing);
        this.out.println(bearing + ":" + heading + ":" + distance);
        double[] Pos = {distance*Math.sin(absBearing),distance*Math.cos(absBearing)};
        return Pos;
    }

    //taken from rainbow warrior - pretty colours yay
    public void onWin(WinEvent event){
        while(true){
            dance();
        }
    }
    public void dance(){
        Color bColor =			new Color((int)(Math.random()*256),(int)(Math.random()*256),(int)(Math.random()*256));
        Color gColor = 			new Color((int)(Math.random()*256),(int)(Math.random()*256),(int)(Math.random()*256));
        Color rColor =			new Color((int)(Math.random()*256),(int)(Math.random()*256),(int)(Math.random()*256));
        Color bulletColor =  	new Color((int)(Math.random()*256),(int)(Math.random()*256),(int)(Math.random()*256));
        Color scanArcColor =  	new Color((int)(Math.random()*256),(int)(Math.random()*256),(int)(Math.random()*256));
        setColors(bColor,gColor,rColor,bulletColor, scanArcColor);
        turnRadarRight(45);
    }

}
