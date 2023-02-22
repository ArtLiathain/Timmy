package ch.zuehlke.semu;

import java.awt.Color;
import java.awt.geom.Point2D;

import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class TheUndertakerV2 extends AdvancedRobot {

    private String target;

    public void run() {
        setBodyColor(Color.BLACK);
        setRadarColor(Color.RED);
        setBulletColor(Color.GREEN);
        setScanColor(Color.WHITE);

        setAdjustGunForRobotTurn(false);
        setAdjustRadarForGunTurn(false);
        setAdjustRadarForRobotTurn(false);

        while(true) {
            turnRadarRightRadians(Double.POSITIVE_INFINITY);
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        if (target == null && !event.isSentryRobot()) {
            target = event.getName();
        }

        if (target.equals(event.getName())) {
            turnRadarTowardsEnemy(event);

            turnTowardsEnemy(event);

            aimAtTarget(event);

            execute();
        }

    }

    public void turnTowardsEnemy(ScannedRobotEvent event) {
        double enemyPosition =	event.getBearingRadians() + getHeadingRadians();
        double toTurn = enemyPosition - getHeadingRadians();

        setTurnRightRadians(Utils.normalRelativeAngle(toTurn));
        setAhead(event.getDistance() * 0.5);
    }

    public void turnRadarTowardsEnemy(ScannedRobotEvent event) {
        double enemyPosition =	event.getBearingRadians() + getHeadingRadians();
        setTurnRadarRightRadians((Utils.normalRelativeAngle(enemyPosition - getRadarHeadingRadians())));
    }

    @Override
    public void onRobotDeath(RobotDeathEvent event) {
        if (target != null && event.getName().equals(target)) {
            target = null;
        }
    }

    public void aimAtTarget(ScannedRobotEvent event) {
        if (target == null) {
            return;
        }

        double angle = Math.toRadians((getHeading() + event.getBearing()) % 360);
        int enemyX = (int) (getX() + Math.sin(angle) * event.getDistance());
        int enemyY= (int) (getY() + Math.cos(angle) * event.getDistance());

        double firePower = 3;
        if (event.getDistance() > 300) {
            firePower = 1.1;
        } else if (event.getDistance() > 200) {
            firePower = 2.0;
        }

        long time = (long)(event.getDistance() * 0.1);
        long predictionX = (long)(enemyX + Math.sin(Math.toRadians(event.getHeading())) * event.getVelocity() * time);
        long predictionY = (long)(enemyY + Math.cos(Math.toRadians(event.getHeading())) * event.getVelocity() * time);

        double absDeg = absoluteBearing(getX(), getY(), predictionX, predictionY);

        setTurnGunRight(normalizeBearing(absDeg - getGunHeading()));

        setFire(firePower);
    }

    double normalizeBearing(double angle) {
        while (angle >  180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }

    double absoluteBearing(double x1, double y1, double x2, double y2) {
        double xo = x2 - x1;
        double yo = y2 - y1;
        double hyp = Point2D.distance(x1, y1, x2, y2);
        double arcSin = Math.toDegrees(Math.asin(xo / hyp));
        double bearing = 0;

        if (xo > 0 && yo > 0) { // both pos: lower-Left
            bearing = arcSin;
        } else if (xo < 0 && yo > 0) { // x neg, y pos: lower-right
            bearing = 360 + arcSin; // arcsin is negative here, actually 360 - ang
        } else if (xo > 0 && yo < 0) { // x pos, y neg: upper-left
            bearing = 180 - arcSin;
        } else if (xo < 0 && yo < 0) { // both neg: upper-right
            bearing = 180 - arcSin; // arcsin is negative here, actually 180 + ang
        }

        return bearing;
    }

}