package UL;

import robocode.Robot;
import robocode.ScannedRobotEvent;

import java.lang.Math;
import java.util.Arrays;

public class Timmy4Ever extends Robot {
    private int sentryQuad = -1;
    private final double padding = 30;

    @Override
    public void run() {
        // Main method
        findSentry(45);
        double[] safePoint = getSafePoint();
        System.out.println("Should move forwards? (Not reverse): " + isAhead(safePoint[0], safePoint[1]));
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        boolean sentryScanned = false;
        double[] botXY = getXY(event.getBearing(), event.getDistance());
        if (!sentryScanned && event.isSentryRobot()) {
            // Sentry bot never moves, therefore this only needs to be executed once
            sentryScanned = true;
            sentryQuad = findQuadrant(botXY[0], botXY[1]);
            System.out.println("Sentry quadrant: " + sentryQuad);

            goTo(botXY[0], botXY[1]);
        } else {
            System.out.println("Coords of bot: " + Arrays.toString(getXY(event.getBearing(), event.getDistance())));
        }
    }

    /**
     * Evaluates if a given point is ahead of the robot, or behind
     *
     * @param pointX X-Coordinate of point
     * @param pointY Y-Coordinate of point
     * @return Boolean <br>
     * True - The point is in front of the robot<br>
     * False - The point is behind the robot
     */
    private boolean isAhead(double pointX, double pointY) {
        double[] pointXY = {pointX, pointY}, wallXY = getWallPoint(), myXY = {getX(), getY()};
        double aSquared = 0, bSquared = 0, cSquared = 0;

        for (int i = 0; i < 2; i++) {
            aSquared += Math.pow((myXY[i] - wallXY[i]), 2);
            bSquared += Math.pow((myXY[i] - pointXY[i]), 2);
            cSquared += Math.pow((pointXY[i] - wallXY[i]), 2);
        }

        return (cSquared <= aSquared + bSquared);
    }

    /**
     * Evaluates what point od the walls the robot is looking at
     *
     * @return Double array with following indices: <br>
     * Index 0: X-Coordinate<br>
     * Index 1: Y-Coordinate
     */
    private double[] getWallPoint() {
        double myHeading = getStandardHeading();
        double[] wallXY = new double[2], myXY = {getX(), getY()};

        boolean topWall = (myHeading > 45 && myHeading < 135),
                bottomWall = (myHeading > 225 && myHeading < 315),
                rightWall = (myHeading < 45 || myHeading > 315),
                leftWall = (myHeading < 225 || myHeading > 135);

        // Determines which wall Timmy is looking at, and sets the wall's X/Y coordinate accordingly (-1 if robot is facing neither)
        wallXY[0] = rightWall ? getBattleFieldWidth() : leftWall ? 0 : -1;
        wallXY[1] = topWall ? getBattleFieldHeight() : bottomWall ? 0 : -1;

        // Equation of the line from robot's heading
        double myHeadingRadians = Math.toRadians(myHeading);
        double m = Math.tan(myHeadingRadians), c = myXY[1] - (m * myXY[0]);

        // Finds corresponding X/Y coordinate using equation of line
        if (wallXY[1] == -1) {
            wallXY[1] = (m * wallXY[0]) + c;
        } else {
            wallXY[0] = (wallXY[1] - c) / m;
        }
        System.out.println("Looking at point (x: " + wallXY[0] + " y: " + wallXY[1] + ") on wall");
        return wallXY;
    }

    /**
     * Gets the heading of the robot using unit circle measurements
     *
     * @return The heading of the robot in degrees
     */
    private double getStandardHeading() {
        double myHeading = getHeading();
        if (myHeading < 90) {
            return 90 - myHeading;
        } else {
            return 450 - myHeading;
        }
    }

    private void goTo(double destX, double destY) {
        double myHeading = getStandardHeading();
        double[] myPos = {getX(), getY()};
        boolean forwards = isAhead(destX, destY);


    }

    /**
     * Finds destination point for Timmy, depending on the position of Sentry Bot
     *
     * @return Double Array, with following indices:
     * <br>
     * Index 0: X-Coordinate<br>
     * Index 1: Y-Coordinate
     */
    private double[] getSafePoint() {
        double[] safeXY = new double[2];
        double left = padding;
        double right = getBattleFieldWidth() - padding;
        double top = getBattleFieldHeight() - padding;
        double bottom = padding;

        switch (sentryQuad) {
            case 0: // Sentry is Top Right
                safeXY[0] = left;
                safeXY[1] = bottom;
                break;
            case 1: // Sentry is Top Left
                safeXY[0] = right;
                safeXY[1] = bottom;
                break;
            case 2: // Sentry is Bottom Left
                safeXY[0] = right;
                safeXY[1] = top;
                break;
            case 3: // Sentry is Bottom Right
                safeXY[0] = left;
                safeXY[1] = top;
                break;
        }
        return safeXY;
    }

    /**
     * Rotates radar until Sentry bot is found
     *
     * @param angleOfRotation Magnitude of change in angle - in degrees
     */
    private void findSentry(double angleOfRotation) {
        while (sentryQuad == -1) {
            turnRadarRight(angleOfRotation);
        }
    }

    /**
     * Using unit circle measurements, evaluates which quadrant a point is in.
     *
     * @param posX Point's X coordinate
     * @param posY Point's Y coordinate
     * @return Top Right: 0<br/>Top Left: 1<br/>Bottom Left: 2<br/>Bottom Right: 3
     */
    private int findQuadrant(double posX, double posY) {
        double midX = getBattleFieldWidth() / 2;
        double midY = getBattleFieldHeight() / 2;
        boolean topLeft = (posX <= midX && posY >= midY),
                topRight = (posX >= midX && posY >= midY),
                bottomLeft = (posX <= midX && posY <= midY);

        return topRight ? 0 :
                topLeft ? 1 :
                        bottomLeft ? 2 : 3;
    }

    private double[] getXY(double bearing, double distance) {
        double absBearing = bearing + getHeading();
        absBearing = Math.toRadians(absBearing);
        return new double[]{distance * Math.sin(absBearing) + getX(), distance * Math.cos(absBearing) + getY()};
    }
}
