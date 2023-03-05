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
        } else {

        }
    }

    /**
     * Evaluates if a given point is ahead of the robot, or behind
     *
     * @param pointX [double] X-Coordinate of point
     * @param pointY [double] Y-Coordinate of point
     * @return [boolean]
     * <br>True - The point is in front of the robot
     * <br>False - The point is behind the robot
     */
    private boolean isAhead(double pointX, double pointY) {
        double[] sides = getTriangleSides(pointX, pointY);

        return (sides[2] <= sides[0] + sides[1]);
    }

    /**
     * Evaluates the size of the sides of a triangle drawn between <b>a given point on the map</b>,
     * <b>the robots position</b> and, <b>the point on the wall</b> the robot is looking at.
     * @param pointX [double] X-Coordinate of given point
     * @param pointY [double] Y-Coordinate of given point
     * @return [double array] Squares of side lengths
     * <br>Index 0: Bot - Wall
     * <br>Index 1: Bot - Point
     * <br>Index 2: Point - Wall
     */
    private double[] getTriangleSides(double pointX, double pointY) {
        double[] pointXY = {pointX, pointY}, wallXY = getWallPoint(), myXY = {getX(), getY()}, sidesSquared = new double[3];

        for (int i = 0; i < 2; i++) {
            sidesSquared[0] += Math.pow((myXY[i] - wallXY[i]), 2);
            sidesSquared[1] += Math.pow((myXY[i] - pointXY[i]), 2);
            sidesSquared[2] += Math.pow((pointXY[i] - wallXY[i]), 2);
        }

        return sidesSquared;
    }

    /**
     * Evaluates what point on the border the robot is looking at
     *
     * @return [double array]
     * <br>Index 0: X-Coordinate
     * <br>Index 1: Y-Coordinate
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
     * @return [double] Angle in degrees
     */
    private double getStandardHeading() {
        return getStandardHeading(getHeading());
    }

    /**
     * Converts angle from compass measurements to unit circle
     *
     * @param heading [double] Angle in degrees
     * @return [double] Angle in degrees
     */
    private double getStandardHeading(double heading) {
        if (heading < 90) {
            return 90 - heading;
        } else {
            return 450 - heading;
        }
    }

    private void goTo(double destX, double destY) {
        double myHeading = getStandardHeading();
        double[] myXY = {getX(), getY()}, destXY = {destX, destY}, wallXY = getWallPoint();
        boolean forwards = isAhead(destX, destY);
        double aSquared = 0, bSquared = 0, cSquared = 0;

        for (int i = 0; i < 2; i++) {
            aSquared += Math.pow((myXY[i] - wallXY[i]), 2);
            bSquared += Math.pow((myXY[i] - destXY[i]), 2);
            cSquared += Math.pow((destXY[i] - wallXY[i]), 2);
        }

    }

    /**
     * Finds the coordinates of the opposite corner to Sentry Bot
     *
     * @return [double array] <br>
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
     * @param angleOfRotation [double] Magnitude of change in angle, in degrees
     */
    private void findSentry(double angleOfRotation) {
        while (sentryQuad == -1) {
            turnRadarRight(angleOfRotation);
        }
    }

    /**
     * Using unit circle measurements, evaluates which quadrant a point is in.
     *
     * @param posX [double] Point's X coordinate
     * @param posY [double] Point's Y coordinate
     * @return [int]
     * <br>Top Right: 0
     * <br>Top Left: 1
     * <br>Bottom Left: 2
     * <br>Bottom Right: 3
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
