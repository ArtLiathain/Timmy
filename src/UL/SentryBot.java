/*******************************************************************************
 * Copyright (c) 2001-2013 Mathew A. Nelson and Robocode contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://robocode.sourceforge.net/license/epl-v10.html
 *******************************************************************************/
package UL;


import robocode.*;
import robocode.util.Utils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.*;
import static robocode.util.Utils.normalRelativeAngleDegrees;


/**
 * BorderGuard - is a sample robot that demonstrates how the BorderSentry interface can be used for
 * creating a robot that guards the border area of the battle field, and which is efficient against
 * robots trying to hide at corners and robots sneaking around near the borders.
 * <p>
 * This robot is somewhat advanced due to:<br>
 * 1) it uses linear targeting for predicting how to hit robots that moves in straight lines.<br>
 * 2) it will only fire at a robot, if it figures out that its bullets will do damage to that
 * particular robot, e.g. when the robots predicted future position will be within the sentry border
 * attack range.<br>
 * 3) it has an efficient scanner that keeps the scan angle as little as possible to get as new
 * scanned data for enemy robots as possible.<br>
 * 4) it picks a target robot to fire at, by calculating the differene in X and Y movements between scans. It stores the
 *  bigger of the two values. This algorithm used to identify wall crawling/strafing robots which execute large
 *  lateral moves i.e move a large number of units each time. <br>
 * 5) the robot generates along the edge of the battle field and stays in tha positon .<br>
 * <p>
 * Lots of improvements can be made to this robot by copying it - making it even stronger. The
 * intention with of this sample robot is to serve as a more advanced example of how a AdvancedRobot
 * can be made, and how it can be structured as most sample robots are far simpler.
 * <p>
 * 
 * Credits goes to the hard working Robocoders at the RoboWiki. :-)
 * <p>
 * 
 * This robot makes use of the Oldest Scanned melee scanner from the RoboWiki:<br>
 * http://robowiki.net/wiki/Melee_Radar
 * <p>
 * 
 * In addition, it makes use of the Exact Non-iterative Solution for Linear Targeting from the
 * RoboWiki:<br>
 * http://robowiki.net/wiki/Linear_Targeting
 * 
 * @author Flemming N. Larsen
 * 
 * @version 1.0
 * 
 * @since 1.9.0.0
 */
public class SentryBot extends AdvancedRobot implements BorderSentry {

	// Constants
		static int corner = 0;
	boolean inCorner = false;
	boolean stopWhenSeeRobot = false; // See goCorner()
	final double FIREPOWER = 3; // Max. power => violent as this robot can afford it!
	final double HALF_ROBOT_SIZE = 18; // Robot size is 36x36 units, so the half size is 18 units

	// Map containing data for all scanned robots.
	// The key to the map is a robot name and the value is an object containing robot data.
	final Map<String, RobotData> enemyMap;

	// Scanning direction, where the radar turns to the right with positive values, and turns
	// to the left with negative values.
	double scanDir = 1;

	// Oldest scanned robot. Can be null.
	RobotData oldestScanned;

	// Target robot for the gun. Can be null meaning that there is currently no target robot.
	RobotData target;

	// Last time when the robot shifted its direction
	long lastDirectionShift;

	// Current direction, where 1 means ahead (forward) and -1 means back
	int direction = 1;

	/**
	 * Constructs this robot.
	 */
	public SentryBot() {
		// We initialize a specialized HashMap that uses a linked list for the access order.
		// This means that the last accessed robot entry is listed first, when we iterate over its
		// values. This robot always sweep the radar towards the oldest scanned robot.
		enemyMap = new LinkedHashMap<String, RobotData>(5, 2, true);
	}

	/**
	 * Main method that is called by the game when the robot engage in the next round of a battle.
	 */
	@Override
	public void run() {
		// Do initialization stuff here before the loop
		initialize();
			

		// Loop forever. If the robot does not take action, the game will disable our robot!
		while (true) {
			// Handle a single turn...

			// Handle the radar that scans enemy robots
			handleRadar();
			// Handle the gun by turning it and fire at our target
			handleGun();
		
		  

			// Scan for other robots. Note that this method will execute all pending commands for
			// the next turn. Hence, scan() ends the turn for our robot.
			scan();
		}
	}

	/**
	 * This method is called by the game when your robot sees another robot, i.e. when the robot's
	 * radar scan "hits" another robot.
	 * 
	 * @param event
	 *            is a ScannedRobotEvent event.
	 */


		@Override
	public void onScannedRobot(ScannedRobotEvent event) {
		// Check that the scanned robot is not a sentry robot
		if (!event.isSentryRobot()) {
			// The scanned robot is not a sentry robot...

			// Update the enemy map
			updateEnemyMap(event);

			// Update the scan direction
			updateScanDirection(event);

			// Update enemy target positions
			updateEnemyTargetPositions();
		}
	}

	/**
	 * This method is called by the game when another robot dies.
	 * 
	 * @param event
	 *            is the RobotDeathEvent that occurs, when another robot dies, which contains data
	 *            for the robot that died.
	 */
	@Override
	public void onRobotDeath(RobotDeathEvent event) {
		// Gets the name of the robot that died
		final String deadRobotName = event.getName();

		// Remove the robot data for the robot that died from the enemy map
		enemyMap.remove(deadRobotName);

		// Remove the data entry for the oldest scanned robot, if we have such an entry
		if (oldestScanned != null && oldestScanned.name.equals(deadRobotName)) {
			oldestScanned = null;
		}
		if (target != null && target.name.equals(event.getName())) {
			target = null;
		}
	}

	/**
	 * This method is called by the every time the robot is painted. In order to see the painting,
	 * make sure to enable the Paint button on the robot console for this robot.
	 * 
	 * @param g
	 *            is the {@link Graphics2D} object, which is the graphics context used for painting
	 *            various shapes like rectangles, circles, lines etc. on top of the battlefield for
	 *            debugging graphics.
	 */
	@Override
	public void onPaint(Graphics2D g) {
		// Set the line width to 2 pixels
		g.setStroke(new BasicStroke(2f));

		// Prepare colors for painting the scanned coordinate and target coordinate
		Color color1 = new Color(0x00, 0xFF, 0x00, 0x40); // Green with 25% alpha blending
		Color color2 = new Color(0xFF, 0xFF, 0x00, 0x40); // Yellow with 25% alhpa blending

		// Paint a two circles for each robot in the enemy map. One circle where the robot was
		// scanned the last time, and another circle where our robot must point the gun in order to
		// hit it (target coordinate). In addition, a line is drawn between these circles.
		for (RobotData robot : enemyMap.values()) {
			paintCircle(g, robot.scannedX, robot.scannedY, color1); // scanned coordinate
			paintCircle(g, robot.targetX, robot.targetY, color2); // target coordinate
			g.setColor(color1);
			g.drawLine((int) robot.scannedX, (int) robot.scannedY, (int) robot.targetX, (int) robot.targetY);
		}

		// Paint a two circles for the target robot. One circle where the robot was
		// scanned the last time, and another circle where our robot must point the gun in order to
		// hit it (target coordinate). In addition, a line is drawn between these circles.
		if (target != null) {
			color1 = new Color(0xFF, 0x7F, 0x00, 0x40); // Orange with 25% alpha blending
			color2 = new Color(0xFF, 0x00, 0x00, 0x80); // Red with 50% alpha blending

			paintCircle(g, target.scannedX, target.scannedY, color1); // scanned coordinate
			paintCircle(g, target.targetX, target.targetY, color2); // target coordinate
			g.setColor(color1);
			g.drawLine((int) target.scannedX, (int) target.scannedY, (int) target.targetX, (int) target.targetY);
		}
	}

	/**
	 * Initializes this robot before a new round in a battle.
	 */
	private void initialize() {
		// Let the robot body, gun, and radar turn independently of each other
		setAdjustRadarForGunTurn(true);
		setAdjustGunForRobotTurn(true);

		// Set robot colors
		setBodyColor(new Color(0x5C, 0x33, 0x17)); // Chocolate Brown
		setGunColor(new Color(0x45, 0x8B, 0x74)); // Aqua Marine
		setRadarColor(new Color(0xD2, 0x69, 0x1E)); // Orange Chocolate
		setBulletColor(new Color(0xFF, 0xD3, 0x9B)); // Burly wood
		setScanColor(new Color(0xCA, 0xFF, 0x70)); // Olive Green
		

		

	}

	/**
	 * This method handles the radar that scans for enemy robots.
	 */
	private void handleRadar() {
		// Set the radar to turn infinitely to the right if the scan direction is positive;
		// otherwise the radar is moved to the left, if the scan direction is negative.
		// Notice that onScannedRobot(ScannedRobotEvent) is responsible for determining the scan
		// direction.
		setTurnRadarRightRadians(scanDir * Double.POSITIVE_INFINITY);
	}

	/**
	 * Method that handles the gun be turning it and fire at a target.
	 */
	private void handleGun() {
		// Update our target robot to fire at
		updateTarget();
		// Update the gun direction
		updateGunDirection();
		// Fires the gun, when it is ready
		fireGunWhenReady();
	}




	/**
	 * Method the updates the enemy map based on new scan data for a scanned robot.
	 * 
	 * @param event
	 *            is a ScannedRobotEvent event containing data about a scanned robot.
	 */
	private void updateEnemyMap(ScannedRobotEvent event) {
		// Gets the name of the scanned robot
		final String scannedRobotName = event.getName();

		// Get robot data for the scanned robot, if we have an entry in the enemy map
		RobotData scannedRobot = enemyMap.get(scannedRobotName);

		// Check if data entry exists for the scanned robot
		if (scannedRobot == null) {
			// No data entry exists => Create a new data entry for the scanned robot
			scannedRobot = new RobotData(event);
			// Put the new data entry into the enemy map
			enemyMap.put(scannedRobotName, scannedRobot);
		} else {
			// Data entry exists => Update the current entry with new scanned data
			scannedRobot.update(event);
		}
	}

	/**
	 * Method that updates the direction of the radar based on new scan data for a scanned robot.
	 * 
	 * @param event
	 *            is a ScannedRobotEvent event containing data about a scanned robot.
	 */
	private void updateScanDirection(ScannedRobotEvent event) {
		// Gets the name of the scanned robot
		final String scannedRobotName = event.getName();

		// Change the scanning direction if and only if we have no record for the oldest scanned
		// robot or the scanned robot IS the oldest scanned robot (based on the name) AND the enemy
		// map contains scanned data entries for ALL robots (the size of the enemy map is equal to
		// the number of opponent robots found by calling the getOthers() method).
		if ((oldestScanned == null || scannedRobotName.equals(oldestScanned.name)) && enemyMap.size() == getOthers()) {

			// Get the oldest scanned robot data from our LinkedHashMap, where the first value
			// contains the oldest accessed entry, which is the robot we need to get.
			RobotData oldestScannedRobot = enemyMap.values().iterator().next();

			// Get the recent scanned position (x,y) of the oldest scanned robot
			double x = oldestScannedRobot.scannedX;
			double y = oldestScannedRobot.scannedY;

			// Get the heading of our robot
			double ourHeading = getRadarHeadingRadians();

			// Calculate the bearing to the oldest scanned robot.
			// The bearing is the delta angle between the heading of our robot and the other robot,
			// which can be a positive or negative angle.
			double bearing = bearingTo(ourHeading, x, y);

			// Update the scan direction based on the bearing.
			// If the bearing is positive, the radar will be moved to the right.
			// If the bearing is negative, the radar will be moved to the left.
			scanDir = bearing;
		}
	}

	/**
	 * Updates the target positions for all enemies. The target position is the position our robot
	 * must fire at in order to hit the target robot. This robot uses Linear Targeting (Exact
	 * Non-iterative Solution) as described on the RoboWiki here:
	 * http://robowiki.net/wiki/Linear_Targeting
		 */
	private void updateEnemyTargetPositions() {
		// Go thru all robots in the enemy map
		Collection<RobotData> values = enemyMap.values();
		for (RobotData enemy : values) {

			// Variables prefixed with e- refer to enemy and b- refer to bullet
			double bV = Rules.getBulletSpeed(FIREPOWER);
			double eX = enemy.scannedX;
			double eY = enemy.scannedY;
			double eV = enemy.scannedVelocity;
			double eH = enemy.scannedHeading;

			// These constants make calculating the quadratic coefficients below easier
			double A = (eX - getX()) / bV;
			double B = (eY - getY()) / bV;
			double C = eV / bV * Math.sin(eH);
			double D = eV / bV * Math.cos(eH);

			// Quadratic coefficients: a*(1/t)^2 + b*(1/t) + c = 0
			double a = A * A + B * B;
			double b = 2 * (A * C + B * D);
			double c = (C * C + D * D - 1);

			// If the discriminant of the quadratic formula is >= 0, we have a solution meaning that
			// at some time, t, the bullet will hit the enemy robot if we fire at it now.
			double discrim = b * b - 4 * a * c;
			if (discrim >= 0) {
				// Reciprocal of quadratic formula. Calculate the two possible solution for the
				// time, t
				double t1 = 2 * a / (-b - Math.sqrt(discrim));
				double t2 = 2 * a / (-b + Math.sqrt(discrim));

				// Choose the minimum positive time or select the one closest to 0, if the time is
				// negative
				double t = Math.min(t1, t2) >= 0 ? Math.min(t1, t2) : Math.max(t1, t2);

				// Calculate the target position (x,y) for the enemy. That is the point that our gun
				// should point at in order to hit the enemy at the time, t.
				double targetX = eX + eV * t * Math.sin(eH);
				double targetY = eY + eV * t * Math.cos(eH);

				// Assume enemy stops at walls. Hence, we limit that target position at the walls.
				double minX = HALF_ROBOT_SIZE;
				double minY = HALF_ROBOT_SIZE;
				double maxX = getBattleFieldWidth() - HALF_ROBOT_SIZE;
				double maxY = getBattleFieldHeight() - HALF_ROBOT_SIZE;

				enemy.targetX = limit(targetX, minX, maxX);
				enemy.targetY = limit(targetY, minY, maxY);
			}
		}
	}


		// Set the target robot to be the one that exhibits the largest change in X or Y from the previous scan
		// Each robot has a delta value which is either the change in X or Y
		// maxDelta decides which robot becomes the target
		// If a robots delta is greater than maxDelta it becomes the new target
		// robots inside  safe zone will not be damaged by fire from the sentry robot

	private void updateTarget() {
		// Set target to null, meaning that we have no target robot yet
		target = null;

		// Create a list over possible target robots that is a copy of robot data from the enemy map
		List<RobotData> targets = new ArrayList<RobotData>(enemyMap.values());

		// Run thru all the possible target robots and remove those that are outside the attack
		// range for this border sentry robot as our robot cannot do harm to robots outside its
		// range.
		Iterator<RobotData> it = targets.iterator();
		while (it.hasNext()) {
			RobotData robot = it.next();

		}


		// Set the target robot to be the one that exhibits the largest change in X or Y from the previous scan
		// Each robot has a delta value which is either the change in X or Y
		// maxDelta decides which robot becomes the target
		// If a robots delta is greater than maxDelta it becomes the new target
		// robots inside  safe zone will not be damaged by fire from the sentry robot

		
	
		double maxDelta=0;
	
		for (RobotData robot : targets) {
			double dist = distanceTo(robot.targetX, robot.targetY);
		    //The change in X and Y is calculated based on the difference between scanned and target
			double diffY = robot.scannedY-robot.targetY;
			double diffX = robot.scannedX-robot.targetX;
			
			//if movement is in a negative directon convert it to a positive value
			if (diffY<0) { 	diffY=diffY*-1;	}
			diffY++;
			
			if (diffX<0) { 	diffX=diffX*-1;	}
			diffX++;
					

		//	delta is set to diffX, if Diff Y is larger set it to diffY value 
			double delta=diffX;
			if (diffY>diffX){delta=diffY;}
			
        // if the delta value for the robot is greater than maxDelta then this robot becomes the new targte
			if (delta>maxDelta)
			{
				maxDelta=delta;			
				target=robot;
			}
			
		
		

			
			    
	
		}

		// If we still haven't got a target robot, then take the first one from our list of target
		// robots if the list is not empty.
		if (target == null && targets.size() > 0) {
			target = targets.get(0);
		}
	}

	/**
	 * Method that updates the gun direction to point at the current target.
	 */
	private void updateGunDirection() {
		// Only update the gun direction, if we have a current target
		if (target != null) {
			// Calculate the bearing between the gun and the target, which can be positive or
			// negative
			double targetBearing = bearingTo(getGunHeadingRadians(), target.targetX, target.targetY);
			// Set the gun to turn right the amount of radians defined by the bearing to the target
			setTurnGunRightRadians(targetBearing); // positive => turn right, negative => turn left
		}
	}

	/**
	 * Method that fires a bullet when the gun is ready to fire.
	 */
	private void fireGunWhenReady() {
		// We only fire the fun, when we have a target robot
		if (target != null) {
			// Only fire when the angle of the gun is pointing at our (virtual) target robot

			// Calculate the distance between between our robot and the target robot
			double dist = distanceTo(target.targetX, target.targetY);
			// Angle that "covers" the the target robot from its center to its edge
			double angle = Math.atan(HALF_ROBOT_SIZE / dist);

			// Check if the remaining angle (turn) to move the gun is less than our calculated cover
			// angle
			if (Math.abs(getGunTurnRemaining()) < angle) {
				// If so, our gun should be pointing at our target so we can hit it => fire!!
				setFire(FIREPOWER);
			}
		}
	}

	/**
	 * Method that checks if a coordinate (x,y) is outside the Border Sentry's attack range.
	 * No used in this implementation
	 * @param x
	 *            is the x coordinate.
	 * @param y
	 *            is the y coordinate.
	 * @return true if the coordinate is outside the attack range; false otherwise.
	 */
	private boolean isOutsideAttackRange(double x, double y) {
		double minBorderX = getSentryBorderSize();
		double minBorderY = getSentryBorderSize();
		double maxBorderX = getBattleFieldWidth() - getSentryBorderSize();
		double maxBorderY = getBattleFieldHeight() - getSentryBorderSize();

		return (x > minBorderX) && (y > minBorderY) && (x < maxBorderX) && (y < maxBorderY);
	}

	/**
	 * Method that returns a value that is guaranteed to be within a value range defined by a
	 * minimum and maximum value based on an input value.<br>
	 * If the input value is lesser than the minimum value, the returned value will be set to the
	 * minimum value.<br>
	 * If the input value is greater than the maximum value, the returned value will be set to the
	 * maximum value.<br>
	 * Otherwise the returned value will be equal to the input value.
	 * 
	 * @param value
	 *            is the input value to limit.
	 * @param min
	 *            is the allowed minimum value.
	 * @param max
	 *            is the allowed maximum value.
	 * @return is the limited input value that is guaranteed to be within the specified minimum and
	 *         maximum range.
	 */
	private double limit(double value, double min, double max) {
		return Math.min(max, Math.max(min, value));
	}

	/**
	 * Methods that returns the distance to a coordinate (x,y) from our robot.
	 * 
	 * @param x
	 *            is the x coordinate.
	 * @param y
	 *            is the y coordinate.
	 * @return the distance to the coordinate (x,y).
	 */
	private double distanceTo(double x, double y) {
		return Math.hypot(x - getX(), y - getY());
	}

	/**
	 * Method that returns the angle to a coordinate (x,y) from our robot.
	 * 
	 * @param x
	 *            is the x coordinate.
	 * @param y
	 *            is the y coordinate.
	 * @return the angle to the coordinate (x,y).
	 */
	private double angleTo(double x, double y) {
		return Math.atan2(x - getX(), y - getY());
	}

	/**
	 * Method that returns the bearing to a coordinate (x,y) from the position and heading of our
	 * robot. The bearing is the delta angle between the heading of our robot and the angle of the
	 * specified coordinate.
	 * 
	 * @param x
	 *            is the x coordinate.
	 * @param y
	 *            is the y coordinate.
	 * @return the angle to the coordinate (x,y).
	 */
	private double bearingTo(double heading, double x, double y) {
		return Utils.normalRelativeAngle(angleTo(x, y) - heading);
	}

	/**
	 * Method that paints a filled circle at the specified coordinate (x,y) and given color. The
	 * circle will have a radius of 20 pixels (meaning that the diameter will be 40 pixels).
	 * 
	 * @param gfx
	 *            is the graphics context to draw within.
	 * @param x
	 *            is the x coordinate for the center of the circle.
	 * @param y
	 *            is the y coordinate for the center of the circle.
	 * @param color
	 *            is the color of the filled circle.
	 */
	private void paintCircle(Graphics2D gfx, double x, double y, Color color) {
		// Set the pen color
		gfx.setColor(color);
		// Paint a filled circle (oval) that has a radius of 20 pixels with a center at the input
		// coordinates.
		gfx.fillOval((int) x - 20, (int) y - 20, 40, 40);
	}

	/**
	 * This class is used for storing data about a robot that has been scanned.<br>
	 * The data is mainly a snapshot of specific scanned data like the scanned position (x,y),
	 * velocity and heading, put also the calculated predicted target position of the robot when our
	 * robot needs to fire at the scanned robot.<br>
	 * Note that this class calculates the position (x,y) of the scanned robot as our robot moves,
	 * and hence data like the angle and distance to the scanned robot will change over time. by
	 * using the position, it is easy to calculate a new angle and distance to the robot.
	 */
	class RobotData {
		final String name; // name of the scanned robot
		double scannedX; // x coordinate of the scanned robot based on the last update
		double scannedY; // y coordinate of the scanned robot based on the last update
		double scannedVelocity; // velocity of the scanned robot from the last update
		double scannedHeading; // heading of the scanned robot from the last update
		double targetX; // predicated x coordinate to aim our gun at, when firing at the robot
		double targetY; // predicated y coordinate to aim our gun at, when firing at the robot

		/**
		 * Creates a new robot data entry based on new scan data for a scanned robot.
		 * 
		 * @param event
		 *            is a ScannedRobotEvent event containing data about a scanned robot.
		 */
		RobotData(ScannedRobotEvent event) {
			// Store the name of the scanned robot
			name = event.getName();
			// Updates all scanned facts like position, velocity, and heading
			update(event);
			// Initialize the coordinates (x,y) to fire at to the updated scanned position
			targetX = scannedX;
			targetY = scannedY;
		}

		/**
		 * Updates the scanned data based on new scan data for a scanned robot.
		 * 
		 * @param event
		 *            is a ScannedRobotEvent event containing data about a scanned robot.
		 */
		void update(ScannedRobotEvent event) {
			// Get the position of the scanned robot based on the ScannedRobotEvent
			Point2D.Double pos = getPosition(event);
			// Store the scanned position (x,y)
			scannedX = pos.x;
			scannedY = pos.y;
			// Store the scanned velocity and heading
			scannedVelocity = event.getVelocity();
			scannedHeading = event.getHeadingRadians();
		}

		/**
		 * Returns the position of the scanned robot based on new scan data for a scanned robot.
		 * 
		 * @param event
		 *            is a ScannedRobotEvent event containing data about a scanned robot.
		 * @return the position (x,y) of the scanned robot.
		 */
		Point2D.Double getPosition(ScannedRobotEvent event) {
			// Gets the distance to the scanned robot
			double distance = event.getDistance();
			// Calculate the angle to the scanned robot (our robot heading + bearing to scanned
			// robot)
			double angle = getHeadingRadians() + event.getBearingRadians();

			// Calculate the coordinates (x,y) of the scanned robot
			double x = getX() + Math.sin(angle) * distance;
			double y = getY() + Math.cos(angle) * distance;

			// Return the position as a point (x,y)
			return new Point2D.Double(x, y);
		}
	}
}
