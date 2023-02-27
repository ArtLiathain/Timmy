package UL;
import robocode.*;
import robocode.Robot;

import java.awt.*;
import java.util.Arrays;
import java.util.Random;

import static java.lang.Math.PI;
import static java.lang.Math.abs;

public class Timmy extends Robot {
    private int sentryQuadX = -1;
    private int sentryQuadY = -1;
    private int enemyQuadX = -1;
    private int enemyQuadY = -1;
    private int xIndex = -2;
    private int yIndex = -1;
    private boolean SentryScanned = false;
    private double[] radarP = new double[2];
    private Random random = new Random();
    private double gunPoint;
    private int bound = 5;
    private boolean enemyScanned = false;

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
    // Does all the shit, pls don't read
    public double[] getPositions(double paddingside, double paddingmid){
        double SentryBorder = getSentryBorderSize();
        double border_x = getBattleFieldWidth();
        double border_y = getBattleFieldHeight();
        double midX = border_x/2;
        double midY = border_y/2;
        double tempx = border_x - paddingside;
        double tempy = border_y -paddingside;

        if(sentryQuadX == -1 &&  sentryQuadY == -1){
            this.out.println("BOTTOM LECT");
            double[] locations = {paddingside,paddingside,paddingside, midY -paddingmid,midX -paddingmid,paddingside};
            radarP[0] = 0;
            radarP[1] = 90;
            gunPoint = 45;
            return locations;
        }
        else if(sentryQuadX == -1 && sentryQuadY == 1){
            this.out.println("TOP LEFT");
            double[] locations = {paddingside,tempy,midX - paddingmid, tempy,paddingside,midY + paddingmid};
            radarP[0] = 90;
            radarP[1] = 180;
            gunPoint = 135;
            return locations;
        }
        else if(sentryQuadX == 1 && sentryQuadY == -1){
            this.out.println("BOTTOM RIGHT");
            double[] locations = {tempx,paddingside,midX + paddingmid, paddingside,tempx,midY - paddingmid};
            radarP[0] = 270;
            radarP[1] = 360;
            gunPoint = 315;
            return locations;
        }
        else if(sentryQuadX == 1 && sentryQuadY == 1){
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
        double[] destination = getSafePoint();
        goTo(destination[0], destination[1]);
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
        out.println("Going to x: " + x + " y:" + y);
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

        // reduce turns if it's possible
        if(goAngle < 0){
            goAngle += 360;
        }

        // easier to go teh other way if it's turning right 3 times to go left
        if(goAngle > 270){
            goAngle -= 270;
            turnLeft(goAngle);
        }else {
            turnRight(goAngle);
        }

        if (random.nextInt(bound)%bound == 0){
            ahead(distance);
        }
        else {
            ahead(distance);
        }

        //shoot shit here
        //this makes timmy die more


    }

    public void onScannedRobot(ScannedRobotEvent event) {
        dance();

        if(event.isSentryRobot() && !SentryScanned){

            double[] SentryPos = GetXY(event.getBearing(), getHeading(), event.getDistance());

            if (SentryPos[0] < 400){
                sentryQuadX = 1;
            }
            if (SentryPos[1]  < 400){
                sentryQuadY = 1;
            }
            System.out.println("Sentry Scanned");
            SentryScanned = true;
        }
        else if(!event.isSentryRobot()){
            double[] enemyPos = GetXY(event.getBearing(), getHeading(), event.getDistance());

            if (enemyPos[0] > 400){
                enemyQuadX = 1;
            }
            if (enemyPos[1] > 400){
                enemyQuadY = 1;
            }
            System.out.println("Enemy Scanned");
            enemyScanned = true;
        }
    }

    public double[] getSafePoint() {
        double[] sentryQuad = {sentryQuadX  * -1, sentryQuadY  * -1};
        double[] homeQuad = {sentryQuadX, sentryQuadY};
        double[] enemyQuad = {enemyQuadX, enemyQuadY};
        System.out.println("sentryQuad: " + Arrays.toString(sentryQuad) + System.lineSeparator() +
                "homeQuad: " + Arrays.toString(homeQuad) + System.lineSeparator() +
                "enemyQuad: " + Arrays.toString(enemyQuad));

        double[][] quads = {{-1,1}, {1,1}, {-1,-1}, {1,-1}};
        for (double[] quad : quads) {
            System.out.println("Quad: " + Arrays.toString(quad));
            if ((!Arrays.equals(sentryQuad, quad)) && (!Arrays.equals(homeQuad, quad)) && (!Arrays.equals(enemyQuad, quad))) {
                if (Arrays.equals(quad, quads[0])) {
                    return new double[] {50, 750};
                } else if (Arrays.equals(quad, quads[1])) {
                    return new double[] {750, 750};
                } else if (Arrays.equals(quad, quads[2])) {
                    return new double[] {50, 50};
                } else if (Arrays.equals(quad, quads[3])) {
                    return new double[] {750, 50};
                }
            }
        }
        return new double[1];
    }

    public double[] GetXY(double bearing, double heading, double distance){
        double absBearing = bearing + heading;
        absBearing = Math.toRadians(absBearing);
        this.out.println(bearing + ":" + heading + ":" + distance);
        return new double[]{distance*Math.sin(absBearing) + getX(),distance*Math.cos(absBearing) + getY()};
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
    }

}
