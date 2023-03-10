package UL;

import robocode.Robot;
import robocode.ScannedRobotEvent;

import java.lang.Math;

public class Timmy4Ever extends Robot {
    private Quad sentryQuad;
    private double borderX;
    private double borderY;

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
         *
         * @param point [Point] Second point
         * @return [double] Length of the line
         */
        public double lengthTo(Point point) {
            double length = 0;

            length += Math.pow(this.x - point.getX(), 2);
            length += Math.pow(this.y - point.getY(), 2);

            return Math.sqrt(length);
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
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
        borderX = getBattleFieldWidth();
        borderY = getBattleFieldHeight();
        // Main method
        findSentry(45);
        Point safePoint = getSafePoint();
        goTo(safePoint);
        goTo(new Point(400,400));
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        boolean sentryScanned = false;
        Point bot = getXY(event.getBearing(), event.getDistance());
        if (!sentryScanned && event.isSentryRobot()) {
            // Sentry bot never moves, therefore this only needs to be executed once
            sentryScanned = true;
            sentryQuad = findQuadrant(bot);
        } else {

        }
    }

    /**
     * Evaluates if a given point is ahead of the robot, or behind
     *
     * @param point [Point] Point to be evaluated
     * @return [boolean]
     * <br>True - The point is in front of the robot
     * <br>False - The point is behind the robot
     */
    private boolean isAhead(Point point) {
        double[] sides = getTriangleSides(point);
        squareAll(sides);

        return (sides[2] <= sides[0] + sides[1]);
    }

    /**
     * Evaluates if the robot should turn left to be aligned with a given point
     * @param point [Point] given point
     * @return [boolean]
     */
    private boolean shouldTurnLeft(Point point) {
        Point bot = new Point(getX(), getY());
        double myHeading = getStandardHeading(),
                m = Math.tan(Math.toRadians(myHeading)),
                c = bot.getY() - (m * bot.getX());
        boolean facingLeft = (myHeading > 90 && myHeading < 270),
                aboveBot = (point.getY() > (m * point.getX()) + c),
                aheadOfBot = isAhead(point),
                leftOfBot = !(facingLeft == aboveBot);

        return (aheadOfBot == leftOfBot);
    }

    /**
     * Evaluates the size of the sides of a triangle drawn between <b>a given point on the map</b>,
     * <b>the robots position</b> and, <b>the point on the wall</b> the robot is looking at.
     *
     * @param point [Point] Point that makes the 3rd corner of the triangle
     * @return [double array] Side lengths
     * <br>Index 0: Bot - Wall
     * <br>Index 1: Bot - Point
     * <br>Index 2: Point - Wall
     */
    private double[] getTriangleSides(Point point) {
        double[] sides = new double[3];

        Point wallPoint = getViewedWallPoint(),
                botPoint = new Point(getX(), getY());

        sides[0] = botPoint.lengthTo(wallPoint);
        sides[1] = botPoint.lengthTo(point);
        sides[2] = point.lengthTo(wallPoint);

        return sides;
    }

    /**
     * Squares all individual elements within an array
     *
     * @param array [double] Array to be squared
     */
    private void squareAll(double[] array) {
        for (int i = 0; i < array.length; i++) {
            array[i] = Math.pow(array[i], 2);
        }
    }

    /**
     * Evaluates what point on the border the robot is looking at
     *
     * @return [Point] Point on the wall the bot is looking at
     */
    private Point getViewedWallPoint() {
        double myHeading = getStandardHeading();
        Point wallPoint = new Point();
        Point bot = new Point(getX(), getY());

        // Equation of the line from robot's heading
        double myHeadingRadians = Math.toRadians(myHeading);
        double m = Math.tan(myHeadingRadians), c = bot.getY() - (m * bot.getX());

        // Sets the wall's X/Y coordinate according to which wall the robot is looking at (Remains -1 if unchanged)
        switch (getViewedWall()) {
            case TOP:
                wallPoint.setX((borderY - c) / m);
                wallPoint.setY(borderY);
                break;
            case RIGHT:
                wallPoint.setX(borderX);
                wallPoint.setY((m * borderX) + c);
                break;
            case BOTTOM:
                wallPoint.setX(-c / m);
                wallPoint.setY(0);
                break;
            case LEFT:
                wallPoint.setX(0);
                wallPoint.setY(c);
                break;
        }

        return wallPoint;
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

    private void goTo(Point destination) {
        double angle = getTurnAngle(destination), distance;
        Point bot = new Point(getX(), getY());

        distance = bot.lengthTo(destination);

        if (angle > 90) {
            angle = 180 - angle;
        }

        if (shouldTurnLeft(destination)) {
            turnLeft(angle);
        } else {
            turnRight(angle);
        }

        if (isAhead(destination)) {
            ahead(distance);
        } else {
            back(distance);
        }
    }

    /**
     * Returns the angle the robot must turn to face a given point
     *
     * @param point [Point] Destination point
     * @return [double] Angle, in degrees
     */
    private double getTurnAngle(Point point) {
        double angleDegrees, numerator, denominator;
        double[] sides = getTriangleSides(point);
        squareAll(sides);

        numerator = sides[0] + sides[1] - sides[2];
        denominator = 2 * Math.sqrt(sides[0]) * Math.sqrt(sides[1]);
        angleDegrees = Math.toDegrees(Math.acos(numerator / denominator));

        System.out.println("Turn angle: " + angleDegrees);

        return angleDegrees;
    }

    /**
     * Finds the coordinates of the opposite corner to Sentry Bot
     *
     * @return [Point]
     */
    private Point getSafePoint() {
        Point safePoint = new Point();
        double padding = 30;
        double right = getBattleFieldWidth() - padding;
        double top = getBattleFieldHeight() - padding;

        switch (sentryQuad) {
            case TOP_RIGHT:
                safePoint.setX(padding);
                safePoint.setY(padding);
                break;
            case TOP_LEFT:
                safePoint.setX(right);
                safePoint.setY(padding);
                break;
            case BOTTOM_LEFT:
                safePoint.setX(right);
                safePoint.setY(top);
                break;
            case BOTTOM_RIGHT:
                safePoint.setX(padding);
                safePoint.setY(top);
                break;
        }

        return safePoint;
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
     * Evaluates which quadrant a point is in.
     *
     * @param point [Point] Point to be evaluated
     * @return [enum Quad]
     */
    private Quad findQuadrant(Point point) {
        double midX = getBattleFieldWidth() / 2,
                midY = getBattleFieldHeight() / 2,
                x = point.getX(),
                y = point.getY();
        boolean topLeft = (x <= midX && y >= midY),
                topRight = (x >= midX && y >= midY),
                bottomLeft = (x <= midX && y <= midY);

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

    private Point getXY(double bearing, double distance) {
        double absBearing = bearing + getHeading();
        absBearing = Math.toRadians(absBearing);
        double[] pointArray = {distance * Math.sin(absBearing) + getX(), distance * Math.cos(absBearing) + getY()};
        return new Point(pointArray[0], pointArray[1]);
    }
}
