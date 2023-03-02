package UL;
import org.apache.bcel.generic.ReturnaddressType;
import robocode.*;
import robocode.Robot;
import robocode.util.Utils;

import java.awt.*;
import java.util.Arrays;
import java.util.Random;

import static java.lang.Math.PI;
import static java.lang.Math.abs;

public class TimmyAtUL extends Robot {
    private int sentryQuadX = -1;
    private int sentryQuadY = -1;
    private int enemyQuadX = -1;
    private int enemyQuadY = -1;
    private int xIndex = -2;
    private int yIndex = -1;
    private boolean SentryScanned = false;
    private double[] radarP = new double[2];
    private double gunPoint;
    private boolean enemyScanned = false;
    boolean isInTriangle = false;
    boolean hasAdjusted = false;
    double[] EnemyPos = new double[2];

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
        isInTriangle = true;
        normalScan();



    }
    }

    public double[] getPositions(double paddingside, double paddingmid){
        double SentryBorder = getSentryBorderSize();
        double border_x = getBattleFieldWidth();
        double border_y = getBattleFieldHeight();
        double midX = border_x/2;
        double midY = border_y/2;
        double tempx = border_x - paddingside;
        double tempy = border_y - paddingside;
        radarP[1] = 135;
        if(sentryQuadX == -1 &&  sentryQuadY == -1){
            this.out.println("BOTTOM LECT");
            double[] locations = {paddingside,paddingside,paddingside, midY -paddingmid,midX -paddingmid,paddingside};
            radarP[0] = 335;


            normalScan();
            return locations;
        }
        else if(sentryQuadX == -1 && sentryQuadY == 1){
            this.out.println("TOP LEFT");
            double[] locations = {paddingside,tempy,midX - paddingmid, tempy,paddingside,midY + paddingmid};


            radarP[0] = 65;
            normalScan();
            return locations;
        }
        else if(sentryQuadX == 1 && sentryQuadY == -1){
            this.out.println("BOTTOM RIGHT");
            double[] locations = {tempx,paddingside,midX + paddingmid, paddingside,tempx,midY - paddingmid};
            radarP[0] = 245;

            normalScan();
            return locations;
        }
        else if(sentryQuadX == 1 && sentryQuadY == 1){
            this.out.println("TOP RIGHT");
            double[] locations = {tempx, tempy, tempx ,midY + paddingmid ,midX + paddingmid, tempy};
            radarP[0] = 155;

            normalScan();
            return locations;
        }
        return null;
    }

    public void run(){
        setAdjustRadarForRobotTurn(true);
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        superScan();
//        double[] destination = getSafePoint();
//        goTo(destination[0], destination[1]);
        move(getPositions(30, 100));
    }

    public void predictEnemy(double distance, double enemyHeading, double enemyVelocity, double energy){
        double[] predictedPosition = new double[]{1,1};
        enemyVelocity *=8;
        this.out.println("Velocity " + enemyVelocity);
        if(enemyVelocity <= 2 && energy != 0){
            enemyVelocity += 50;
        }
        double xVelocity, yVelocity, newBearing;
        if(enemyHeading > 270){
            enemyHeading -= 270;
            xVelocity = Math.cos(enemyHeading)*enemyVelocity;
            predictedPosition[0] = EnemyPos[0] - xVelocity;
            yVelocity = Math.sin(enemyHeading)*enemyVelocity;
            predictedPosition[1] = yVelocity + EnemyPos[1];
        }
        else if(enemyHeading > 180){
            enemyHeading -= 180;
            xVelocity = Math.sin(enemyHeading)*enemyVelocity;
            predictedPosition[0] = EnemyPos[0] - xVelocity;
            yVelocity = Math.cos(enemyHeading)*enemyVelocity;
            predictedPosition[1] = EnemyPos[1] - yVelocity;
        }
        else if(enemyHeading > 90){
            enemyHeading -= 90;

            xVelocity = Math.cos(enemyHeading)*enemyVelocity;
            predictedPosition[0] = xVelocity + EnemyPos[0];
            yVelocity = Math.sin(enemyHeading)*enemyVelocity;
            predictedPosition[1] = EnemyPos[1] - yVelocity;
        }
        else {
            xVelocity = Math.sin(enemyHeading)*enemyVelocity;
            predictedPosition[0] = xVelocity + EnemyPos[0];
            yVelocity = Math.cos(enemyHeading)*enemyVelocity;
            predictedPosition[1] = EnemyPos[1] + yVelocity;
        }
        newBearing = Math.atan((Math.abs(predictedPosition[0] - getX()))/Math.abs((predictedPosition[1] -  getY())))* 57.2958;
        if(predictedPosition[0] > getX()){
            if(predictedPosition[1] < getY()){
                //bottom tight
                newBearing = 180 - newBearing;
            }
            //top Right
        }
        else{
            //top left
            if(predictedPosition[1] > getY()){
                newBearing = 360 - newBearing;
            }
            else {
                newBearing += 180;
            }
        }
        this.out.println("Old Postion" + EnemyPos[0] + " Y: " + EnemyPos[1]);
        this.out.println("Predicted Postion: " + predictedPosition[0] + " " + predictedPosition[1] + "\nBearing: " + newBearing);
        shootGun(newBearing, distance);


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
    }

    public void normalScan(){
        dance();
        double radarD = getRadarHeading();
        int radarH = (int) Math.round(radarD);
        if(!hasAdjusted) {
            turnRadarRight(radarP[0] - getRadarHeading());
            hasAdjusted = true;
            return;
        }
        if(radarH == radarP[0]){
            turnRadarRight(radarP[1]);
            }
        else{
            turnRadarLeft(radarP[1]);
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

        ahead(distance);

        //shoot shithere
        //this makes timmy die more


    }

    public void onScannedRobot(ScannedRobotEvent event){
        if(event.isSentryRobot() && !SentryScanned){

            double[] SentryPos = GetXY(event.getBearing(), getHeading(), event.getDistance());

            if (SentryPos[0] < 400){
                sentryQuadX = 1;
            }
            if (SentryPos[1] < 400){
                sentryQuadY = 1;
            }
            SentryScanned = true;
        }
        else if(!event.isSentryRobot() && isInTriangle && enemyScanned){
            double targetBearing = event.getBearing();
            double velocity = event.getVelocity();
            double heading = event.getHeading();
            double distance = event.getDistance();
            EnemyPos = GetXY(targetBearing, getHeading(), distance);
            this.out.println("Enemy BEaring" + event.getBearing());
            predictEnemy(distance, heading, velocity, event.getEnergy());
//            shootGun(event.getBearing(), event.getDistance());
        }
        else if(!enemyScanned){
            double[] enemyPos = GetXY(event.getBearing(), getHeading(), event.getDistance());

            if (enemyPos[0] > 400){
                enemyQuadX = 1;
            }
            if (enemyPos[1] > 400){
                enemyQuadY = 1;
            }

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

    public void shootGun(double bearing, double distance){
//        double gunHeading = getGunHeading();
//        double heading = getHeading();
//        double absoluteBearing = heading + bearing;
//        double bearingFromGun = Utils.normalRelativeAngleDegrees(absoluteBearing - gunHeading);
        double bearingFromGun = bearing - getGunHeading();
        turnGunRight(bearingFromGun);
        smartFire(distance);
    }

    //Ul contessa bullet power code
    public void smartFire(double robotDistance) {
        if (robotDistance > 400) {
            fire(1);
        } else if (robotDistance > 200) {
            fire(2);
        } else {
            fire(3);
        }
    }

    public double[] GetXY(double bearing, double heading, double distance){
        double absBearing = bearing + heading;
        absBearing = Math.toRadians(absBearing);
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
        turnRadarRight(45);
    }

}
