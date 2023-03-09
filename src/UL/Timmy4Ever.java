package UL;

import robocode.Robot;
import robocode.ScannedRobotEvent;

import java.lang.Math;

public class Timmy4Ever extends Robot {
    private Quad sentryQuad;

    enum Quad {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }

    enum Wall {
        TOP,
        LEFT,
        BOTTOM,
        RIGHT
    }

    private static class Point {
        private double x;
        private double y;

        public Point(double posX, double posY) {
            this.x = posX;
            this.y = posY;
        }

        public Point() {
        }

        /**
         * Calculates the distance to a given point
         * @param destXY [Point] Given point to calculate the distance to
         * @return [double] Length of the line
         */
        public double lengthTo(Point destXY) {
            double length = 0;

            length += Math.pow(this.x - destXY.getX(), 2);
            length += Math.pow(this.y - destXY.getY(), 2);

            return Math.sqrt(length);
        }

        public double getX() {
            return x;
        }

        public void setX(double posX) {
            this.x = posX;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }
    }

    @Override
    public void run() {
        // Main method
        findSentry(45);
        Point safePoint = getSafePoint();
        System.out.println("Should move forwards? " + isAhead(safePoint.getX(), safePoint.getY()));
        goTo(safePoint.getX(), safePoint.getY());
        getViewedWallPoint();
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
     *
     * @param pointX [double] X-Coordinate of given point
     * @param pointY [double] Y-Coordinate of given point
     * @return [double array] <b>Squares</b> of side lengths
     * <br>Index 0: Bot - Wall
     * <br>Index 1: Bot - Point
     * <br>Index 2: Point - Wall
     */
    private double[] getTriangleSides(double pointX, double pointY) {
        double[] pointXY = {pointX, pointY},
                wallXY = getViewedWallPoint(),
                myXY = {getX(), getY()},
                sidesSquared = new double[3];

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
    private double[] getViewedWallPoint() {
        double myHeading = getStandardHeading();
        double[] wallXY = new double[2];
        Point bot = new Point(getX(), getY());

                // Sets the wall's X/Y coordinate according to which wall the robot is looking at (Remains -1 if unchanged)
        switch (getViewedWall()) {
            case TOP:
                wallXY[1] = getBattleFieldHeight();
                break;
            case RIGHT:
                wallXY[0] = getBattleFieldWidth();
                break;
            case BOTTOM:
                wallXY[1] = 0;
                break;
            case LEFT:
                wallXY[0] = 0;
                break;
        }

        // Equation of the line from robot's heading
        double myHeadingRadians = Math.toRadians(myHeading);
        double m = Math.tan(myHeadingRadians), c = bot.getY() - (m * bot.getX());

        // Finds corresponding X/Y coordinate using equation of line
        if (wallXY[1] == -1) {
            wallXY[1] = (m * wallXY[0]) + c;
        } else {
            wallXY[0] = (wallXY[1] - c) / m;
        }

        return wallXY;
    }

    /**
     * Evaluates which wall the robot is looking at
     *
     * @return [enum Wall]
     */
    private Wall getViewedWall() {
        double myHeading = getStandardHeading();
        double[] myXY = {getX(), getY()},
                cornerTopRight = {getBattleFieldWidth(), getBattleFieldHeight()},
                cornerBottomRight = {getBattleFieldWidth(), 0},
                cornerBottomLeft = {0, 0},
                cornerTopLeft = {0, getBattleFieldHeight()};

        double headingTopRight = Math.toDegrees(Math.atan((cornerTopRight[1] - myXY[1]) / (cornerTopRight[0] - myXY[0]))),
                headingBottomRight = 360 + Math.toDegrees(Math.atan((cornerBottomRight[1] - myXY[1]) / (cornerBottomRight[0] - myXY[0]))),
                headingBottomLeft = 270 - Math.toDegrees(Math.atan((cornerBottomLeft[1] - myXY[1]) / (cornerBottomLeft[0] - myXY[0]))),
                headingTopLeft = 180 + Math.toDegrees(Math.atan((cornerTopLeft[1] - myXY[1]) / (cornerTopLeft[0] - myXY[0])));

        boolean topWall = (myHeading > headingTopRight && myHeading < headingTopLeft),
                leftWall = (myHeading > headingTopLeft && myHeading < headingBottomLeft),
                bottomWall = (myHeading > headingBottomLeft && myHeading < headingBottomRight);

        return topWall ? Wall.TOP : leftWall ? Wall.LEFT : bottomWall ? Wall.BOTTOM : Wall.RIGHT;
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
        double angle = getTurnAngle(destX, destY), distance;
        double[] myXY = {getX(), getY()}, destXY = {destX, destY};

        distance = Math.sqrt(Math.pow(myXY[0] - destXY[0], 2) + Math.pow(myXY[1] - destXY[1], 2));

        if (angle > 90) {
            angle = 180 - angle;
        }
    }

    /**
     * Returns the angle the robot must turn to face a given destination point
     *
     * @param destX [double] X-Coordinate of given point
     * @param destY [double] Y-Coordinate of given point
     * @return [double] Angle, in degrees
     */
    private double getTurnAngle(double destX, double destY) {
        double angleDegrees, numerator, denominator;
        double[] sides = getTriangleSides(destX, destY);

        numerator = sides[0] + sides[1] - sides[2];
        denominator = 2 * Math.sqrt(sides[0]) * Math.sqrt(sides[1]);
        angleDegrees = Math.toDegrees(Math.acos(numerator / denominator));

        return angleDegrees;
    }

    /**
     * Finds the coordinates of the opposite corner to Sentry Bot
     *
     * @return [Point]
     */
    private Point getSafePoint() {
        Point safeXY = new Point();
        double padding = 30;
        double right = getBattleFieldWidth() - padding;
        double top = getBattleFieldHeight() - padding;

        switch (sentryQuad) {
            case TOP_RIGHT:
                safeXY.setX(padding);
                safeXY.setY(padding);
                break;
            case TOP_LEFT:
                safeXY.setX(right);
                safeXY.setY(padding);
                break;
            case BOTTOM_LEFT:
                safeXY.setX(right);
                safeXY.setY(top);
                break;
            case BOTTOM_RIGHT:
                safeXY.setX(padding);
                safeXY.setY(top);
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
        while (sentryQuad == null) {
            turnRadarRight(angleOfRotation);
        }
    }

    /**
     * Using unit circle measurements, evaluates which quadrant a point is in.
     *
     * @param posX [double] Point's X coordinate
     * @param posY [double] Point's Y coordinate
     * @return [enum Quad]
     */
    private Quad findQuadrant(double posX, double posY) {
        double midX = getBattleFieldWidth() / 2;
        double midY = getBattleFieldHeight() / 2;
        boolean topLeft = (posX <= midX && posY >= midY),
                topRight = (posX >= midX && posY >= midY),
                bottomLeft = (posX <= midX && posY <= midY);

        if (topLeft) {
            return Quad.TOP_LEFT;
        } else if (topRight) {
            return Quad.TOP_RIGHT;
        } else if (bottomLeft) {
            return Quad.BOTTOM_LEFT;
        } else {
            return Quad.BOTTOM_RIGHT;
        }
    }

    private double[] getXY(double bearing, double distance) {
        double absBearing = bearing + getHeading();
        absBearing = Math.toRadians(absBearing);
        return new double[]{distance * Math.sin(absBearing) + getX(), distance * Math.cos(absBearing) + getY()};
    }
}
