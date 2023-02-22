package ch.zuehlke.mhau;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static ch.zuehlke.helpers.Helper.*;

public class WhiteWarrior extends AdvancedRobot {

    Map<String, ScannedRobot> scannedRobots = new HashMap<>();
    boolean doFire = false;

    @Override
    public void run() {
        setBodyColor(Color.black);
        setGunColor(Color.white);
        setRadarColor(Color.red);
        setScanColor(Color.yellow);

        double battleFieldHeight = getBattleFieldHeight();
        double battleFieldWidth = getBattleFieldWidth();

        double widthMiddle = battleFieldWidth / 2;
        double heightMiddle = battleFieldHeight / 2;

        setAdjustGunForRobotTurn(false);
        setAdjustRadarForGunTurn(false);

        Random r = new Random();

        while (true) {
            if (getDistance(getX(), getY(), widthMiddle, heightMiddle) > 350) {
                goTo(widthMiddle, heightMiddle);
            } else {
                /*scannedRobots.values()
                        .stream()
                        .min(comparing(ScannedRobot::getEnergy)).ifPresent(scannedRobot -> {
                    System.out.println("going for " + scannedRobot);

                    turnTo(scannedRobot.getBearing());
                    setAhead(scannedRobot.getDistance());
                    setTurnGunLeft(scannedRobot.getBearing());
                    fire(1);

                });*/

                // setTurnGunLeft(r.nextInt(40) + 40);

                setAhead(r.nextInt(70) + 70);
                setTurnRight(r.nextInt(40) + 40);

                if (r.nextBoolean()) {

                    fire(r.nextInt(3));
                }

                scan();
            }

            execute();
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        /*String name = event.getName();
        double bearing = normaliseBearing(event.getBearingRadians());
        double energy = event.getEnergy();
        double heading = normaliseHeading(event.getHeadingRadians());
        double velocity = event.getVelocity();

        scannedRobots.put(name, new ScannedRobot(name, bearing, distance, energy, heading, velocity));*/
        double distance = event.getDistance();
        if (distance < 50) {
            doFire = true;
        } else {
            doFire = false;
        }
    }

    void goTo(double x, double y) {
        double dist = 20;
        double angle = Math.toDegrees(absbearing(getX(), getY(), x, y));
        double r = turnTo(angle);
        setAhead(dist * r);
    }

    //Turns the shortest angle possible to come to a heading, then returns the direction the
    //the bot needs to move in.
    int turnTo(double angle) {
        double ang;
        int dir;
        ang = normaliseBearing(getHeading() - angle);
        if (ang > 90) {
            ang -= 180;
            dir = -1;
        } else if (ang < -90) {
            ang += 180;
            dir = -1;
        } else {
            dir = 1;
        }
        setTurnLeft(ang);
        return dir;
    }

}
