package UL;
import robocode.*;
import robocode.Robot;
import robocode.util.Utils;

import java.awt.*;

import static java.lang.Math.PI;
import static java.lang.Math.abs;

public class  Timmy extends Robot {
    public int sentryX = -1;
    public int sentryY = -1;
    int xIndex = -2;
    int yIndex = -1;
    boolean SentryScanned = false;

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
            return locations;
        }
        else if(sentryX == -1 && sentryY == 1){
            this.out.println("TOP LEFT");
            double[] locations = {paddingside,tempy,midX - paddingmid, tempy,paddingside,midY + paddingmid};
            return locations;
        }
        else if(sentryX == 1 && sentryY == -1){
            this.out.println("BOTTOM RIGHT");
            double[] locations = {tempx,paddingside,midX + paddingmid, paddingside,tempx,midY - paddingmid};
            return locations;
        }
        else if(sentryX == 1 && sentryY == 1){
            this.out.println("TOP RIGHT");
            double[] locations = {tempx, tempy, tempx ,midY + paddingmid ,midX + paddingmid, tempy};
            return locations;
        }
        return null;
    }

    public void run(){
        superScan();
        move(getPositions(50, 100));
    }
    boolean scan = false;
    //inspiration form UL COFFEE
    public void superScan(){
        int max = 0;
        int degrees = 30;
        scan = true;
        while(scan){
            turnRadarRight(degrees);
            max += degrees;

            if(max > 360){
                break;
            }
        }
    }

    // yoinked from https://robowiki.net/wiki/GoTo and modified for Robot
    //further yoinked modified version from ULCoffee

    private void goTo(double x, double y) {
        double x_dest = x - getX();
        double y_dest = y - getY();

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
        ahead(distance/2);
        //shoot shithere
        //this makes timmy die more
        fire(1.1);
        ahead(distance/2);
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
            scan = false;
        }
        else if(event.isSentryRobot() && SentryScanned){

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
