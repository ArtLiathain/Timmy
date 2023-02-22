/**
 * Copyright (c) 2001-2018 Mathew A. Nelson and Robocode contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://robocode.sourceforge.net/license/epl-v10.html
 */
package ch.zuehlke.anko;


import ch.zuehlke.helpers.Helper;
import robocode.AdvancedRobot;
import robocode.HitRobotEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class AndresBot extends AdvancedRobot {

    int turnDirection = 1;

    int scannedX = 0;
    int scannedY = 0;

    int destinationX = 0;
    int destinationY = 0;

    
    public void run() {
        // Set colors
        setRadarColor(Color.black);
        setScanColor(Color.yellow);

        // Loop forever
        while (true) {
            move();
        }
    }

    private void goToRandomLocation() {
        if (destinationX == 0) {
            destinationX = rand.nextInt() % ((int)getBattleFieldWidth() - (getSentryBorderSize() * 2)) ;
            destinationX += (int)getSentryBorderSize();
            destinationY = rand.nextInt() % ((int)getBattleFieldHeight() - getSentryBorderSize() * 2);
            destinationY += (int)getSentryBorderSize();
        }
        if (rand.nextInt() % 2 == 0) {
            if (rand.nextInt() % 2 == 0 && scannedX != 0) {
                destinationX = scannedX;
                destinationY = scannedY;
            } else {
                destinationX = rand.nextInt() % ((int) getBattleFieldWidth() - (getSentryBorderSize() * 2));
                destinationX += (int) getSentryBorderSize();
                destinationY = rand.nextInt() % ((int) getBattleFieldHeight() - getSentryBorderSize() * 2);
                destinationY += (int) getSentryBorderSize();
            }
        }


        goTo(destinationX, destinationY);
    }

      //Move towards an x and y coordinate

    void goTo(double x, double y) {
        double dist = 20;
        double angle = Math.toDegrees(Helper.absbearing(getX(), getY(), x, y));
        double r = turnTo(angle);
        ahead(dist * r);
    }


    //Turns the shortest angle possible to come to a heading, then returns the direction the
     //the bot needs to move in.

    int turnTo(double angle) {
        double ang;
        int dir;
        ang = Helper.normaliseBearing(getHeading() - angle);
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

    private void move() {
        setNewColors();
        goToRandomLocation();
        scan();
        execute();
    }

    private static Random rand = new Random();
    private void setNewColors() {
        setBodyColor(new Color(rand.nextInt()%24));
        setGunColor(new Color(rand.nextInt()%24));
        setBulletColor(new Color(rand.nextInt()%24));
    }

    /**
     * onScannedRobot: Fire hard!
     */
    public void onScannedRobot(ScannedRobotEvent e) {
        if (!e.isSentryRobot()) {
            double angle = Math.toRadians((getHeading() + e.getBearing()) % 360);
            scannedX = (int)(getX() + Math.sin(angle) * e.getDistance());
            scannedY = (int)(getY() + Math.cos(angle) * e.getDistance());
            double absoluteBearing = this.getHeading() + e.getBearing();
            double bearingFromGun = Utils.normalRelativeAngleDegrees(absoluteBearing - this.getGunHeading());
            if (Math.abs(bearingFromGun) <= 3.0D) {
                this.turnGunRight(bearingFromGun);
                if (this.getGunHeat() == 0.0D) {
                    this.fire(Math.min(3.0D - Math.abs(bearingFromGun), this.getEnergy() - 0.1D));
                }
            } else {
                this.turnGunRight(bearingFromGun);
            }

            if (bearingFromGun == 0.0D) {
                this.scan();
            }

            if ((rand.nextInt() % 3) == 0) move();
        }


    }

    /**
     * onHitRobot:  If it's our fault, we'll stop turning and moving,
     * so we need to turn again to keep spinning.
     */
    public void onHitRobot(HitRobotEvent e) {
        if (e.getBearing() >= 0.0D) {
            this.turnDirection = 1;
        } else {
            this.turnDirection = -1;
        }

        this.turnRight(e.getBearing());
        if (e.getEnergy() > 16.0D) {
            this.fire(3.0D);
        } else if (e.getEnergy() > 10.0D) {
            this.fire(2.0D);
        } else if (e.getEnergy() > 4.0D) {
            this.fire(1.0D);
        } else if (e.getEnergy() > 2.0D) {
            this.fire(0.5D);
        } else if (e.getEnergy() > 0.4D) {
            this.fire(0.1D);
        }

        if ((rand.nextInt() % 3) == 0) move();
    }
}
