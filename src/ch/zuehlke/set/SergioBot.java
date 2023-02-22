package ch.zuehlke.set;

import ch.zuehlke.helpers.Helper;
import robocode.Robot;
import robocode.ScannedRobotEvent;

import java.awt.*;

/**
 * SergioBot - the first
 * <p>
 * Does not much.
 *
 * @author Sergio Trentini
 */
public class SergioBot extends Robot {

    private double h;
    private double w;

    public void run() {

        h = getBattleFieldHeight();
        w = getBattleFieldWidth();
        int borderSize = getSentryBorderSize();
        double reducedFieldXMax = w - borderSize;
        double reducedFieldYMax = h - borderSize;

        setBodyColor(new Color(0, 200, 0));
        setGunColor(new Color(0, 150, 50));
        setRadarColor(new Color(0, 100, 100));
        setBulletColor(new Color(0, 200, 0));
        setScanColor(new Color(255, 200, 200));

        //setAdjustGunForRobotTurn(true); // Keep the gun still when we turn


        while (true) {

            if (isInBorderArea(reducedFieldXMax, reducedFieldYMax, borderSize)) {
                boolean wasMovingForward = goTo(h / 2, w / 2, w / 8);
                if (!wasMovingForward) {
                    turnLeft(180);
                }
            } else {
                ahead(100);
                //turnGunRight(360);
            }


        }
    }


    /**
     * Move towards an x and y coordinate
     */
    private boolean goTo(double x, double y, double dist) {
        double angle = Math.toDegrees(Helper.absbearing(getX(), getY(), x, y));
        boolean forward = turnTo(angle);
        ahead(dist * (forward ? 1 : -1));
        return forward;
    }


    /**
     * Turns the shortest angle possible to come to a heading, then returns the direction the
     * the bot needs to move in.
     */
    private boolean turnTo(double angle) {
        double ang;
        boolean forward;
        ang = Helper.normaliseBearing(getHeading() - angle);
        if (ang > 90) {
            ang -= 180;
            forward = false;
        } else if (ang < -90) {
            ang += 180;
            forward = false;
        } else {
            forward = true;
        }
        turnLeft(ang);
        return forward;
    }

    private boolean isInBorderArea(double reducedFieldXMax, double reducedFieldYMax, int borderSize) {

        double posX = getX();
        double posY = getY();

        return (posX < borderSize || posX > reducedFieldXMax) || (posY < borderSize || posY > reducedFieldYMax);
    }


    private boolean isTired() {
        double currentEnergy = getEnergy();
        return (currentEnergy < 20);
    }

    public void onScannedRobot(ScannedRobotEvent e) {

        double dist = e.getDistance();
        if (dist > h / 2) {
            return;
        }

        turnTo(e.getBearing());
        if (dist < 300) {
            turnTo(e.getBearing());
            if (!isTired()) {
                fire(1);
            }
        }
    }
}
