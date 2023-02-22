package test01;

import robocode.*;
import robocode.Robot;
import robocode.util.Utils;

import java.awt.*;

import static java.lang.Math.PI;

// API help : https://robocode.sourceforge.io/docs/robocode/robocode/Robot.html

/**
 * Bot03 - a robot by (your name here)
 */
public class Bot03 extends Robot {
    public void run() {
        setup();
        move();
    }

    private void setup() {
        // set colours
        setBodyColor(Color.lightGray);
        setRadarColor(Color.yellow);
        // all will bow down to teh might of COFFEE!!!!!!
        setBulletColor(Color.decode("#C0FFEE"));
        setScanColor(Color.GREEN);
        setAdjustRadarForRobotTurn(true);
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
    }

    // movement stuff here
    private void move() {
        int border = getSentryBorderSize();

        // stay this far in from teh edge
        double offset = 20;
        double h = getBattleFieldHeight();
        double w = getBattleFieldWidth();

        // these are the corners
        double[][] corners = new double[][]{
                {border + offset, border + offset},
                {border + offset, h/2},
                {border + offset,  h - (border + offset) },
                {w/2,  h - (border + offset) },
                {w- (border + offset),  h - (border + offset) },
                {w- (border + offset),  h/2 },
                {w- (border + offset),  border + offset},
                {w/2,  border + offset},
        };

        int moves = 0;
        while(getEnergy() > 0){
            // spin at teh start of every movement
            spinRadar();

            goTo(corners[moves % corners.length][0], corners[moves % corners.length][1]);
            moves += 1;
        }
    }

    // yoinked from https://robowiki.net/wiki/GoTo and modified for Robot
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

        ahead(distance);
    }

    boolean scan = true;
    private void spinRadar(){
        // do whatever ye want with teh radar, max spin is 360 though
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

    private String target;
    private double target_distance;

    // this is whenever teh scanner picks up something
    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        // no need to deal with these
        if (event.isSentryRobot()) {
            return;
        }

        // reduce calls as much as possible
        String name = event.getName();
        double distance = event.getDistance();

        // pick first target if current one is dead
        if (target == null) {
            target = name;
            target_distance = distance;
            this.out.println("Tracking " + name);
        }
        // however if there is a closer target then go for that
        if (distance < target_distance) {
            target = name;
            target_distance = distance;
            this.out.println("Tracking " + name);
        }

        // break any existing scan
        scan = false;


        if (target.equals(event.getName())) {
            double bearing = event.getBearing();
            double heading = getHeading();
            double heading_radar = getRadarHeading();
            double heading_gun = getGunHeading();
            double heat = getGunHeat();

            // taken inspiration from teh sample tracker firing
            double absoluteBearing = heading + bearing;
            double bearingFromGun = Utils.normalRelativeAngleDegrees(absoluteBearing - heading_gun);

            // turn gun to target
            turnGunRight(bearingFromGun);

            //if (heat == 0.0D) {
                if (distance > 300) {
                    // if the bullets energy is above 1 it does bonus damage, so basically free damage
                    fire(1.1);
                } else if (distance > 200) {
                    fire(2.0);
                } else {
                    // just in case the rules change use the enum
                    fire(Rules.MAX_BULLET_POWER);
                }
            //}
        }
    }

    public void onHitRobot (HitRobotEvent event){
        // cutr down version of teh scanned one
        double bearing = event.getBearing();
        double heading = getHeading();

        double heading_gun = getGunHeading();
        double heat = getGunHeat();

        // taken inspiration from teh sample tracker firing
        double absoluteBearing = heading + bearing;
        double bearingFromGun = Utils.normalRelativeAngleDegrees(absoluteBearing - heading_gun);

        // turn gun to target
        turnGunRight(bearingFromGun);


        fire(Rules.MAX_BULLET_POWER);

        back(30);
    }

    @Override
    public void onRobotDeath(RobotDeathEvent event) {
        if (event.getName().equals(target)) {
            target = null;
            target_distance = Integer.MAX_VALUE;
        }
    }

}