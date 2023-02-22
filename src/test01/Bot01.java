package test01;


import robocode.*;
import robocode.util.Utils;

import java.awt.Color;


// made using teh guts of otehr bots
public class Bot01 extends Robot {

    public void run() {
        setup();

        // fairly shamelessly taking this from some other bots
        // the radar is basically what controls the bot
        while(true){
            turnRadarRight(Double.POSITIVE_INFINITY);
        }
    }


    private void setup(){
        // set colours
        setBodyColor(Color.lightGray);
        setRadarColor(Color.yellow);
        // all will bow down to teh might of COFFEE!!!!!!
        setBulletColor(Color.decode("#C0FFEE"));
        setScanColor(Color.GREEN);


        setAdjustRadarForRobotTurn(true);
        setAdjustGunForRobotTurn(true);
        //setAdjustRadarForGunTurn(true);
    }

    private String target;
    private double target_distance;
    private double target_distance_last = Double.MAX_VALUE;

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        // no need to deal with these
        if (event.isSentryRobot()){
            return;
        }

        // reduce calls as much as possible
        String name = event.getName();
        double distance =  event.getDistance();

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


        if (target.equals(name)) {
            // update distance
            target_distance = distance;

            double bearing = event.getBearing();
            double heading = getHeading();
            double heading_gun = getGunHeading();

            // taken inspiration from teh sample tracker firing
            double absoluteBearing = heading + bearing;
            double bearingFromGun = Utils.normalRelativeAngleDegrees(absoluteBearing - heading_gun);

            // turn gun to target

            double power;
            if (distance > 300) {
                // if the bullets energy is above 1 it does bonus damage, so basically free damage
                power = 1.1;
            } else if (distance > 200) {
                power = 2;
            } else {
                // just in case the rules change use the enum
                power = Rules.MAX_BULLET_POWER;
            }


            turnGunRight(bearingFromGun);
            if (getGunHeat() <= 1.3) {
                fire(power);
            }

            // chase down target
            turnRight(Utils.normalRelativeAngleDegrees(bearing));

            ahead(distance * 0.5);

        }
    }

    @Override
    public void onRobotDeath(RobotDeathEvent event) {
        if (event.getName().equals(target)) {
            target = null;
            target_distance = Integer.MAX_VALUE;
        }
    }

    /*
    // this didnt work out
    public void onHitWall(HitWallEvent e) {
        double x = getX();
        double y = getY();
        double border = getSentryBorderSize();
        double width = getBattleFieldWidth();
        double height = getBattleFieldHeight();



        this.out.printf("At %fx %fy going to ", x, y);

        goTo(height/2, width/2);
    }

    // yoinked from teh wiki https://robowiki.net/wiki/GoTo
    private void goTo(double x, double y) {

        this.out.printf("%fx %fy\n", x, y);
        double goAngle = Utils.normalRelativeAngle(Math.atan2(x, y) - getHeadingRadians());
        turnRightRadians(Math.atan(Math.tan(goAngle)));
        ahead(Math.cos(goAngle) * Math.hypot(x, y));

    }

     */

}
