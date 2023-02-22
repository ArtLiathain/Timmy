package UL;
import robocode.*;
import java.awt.Color;

public class UL_RainbowWarrior extends Robot
{
	/* ----------- Data feed variables ------------*/
	
	double[] opponentPastPos = new double[2];
	double[] opponentPos = new double[2];
	double[] sentryPosition = new double[2];
	double[] time = new double[2];
	double[] globalVector = new double[2];
	double driveTime;
	double power;
	double accuracy;
	String target = "";
	
	int robotCount;
	int WIDTH, HEIGHT;
	int count = 0;
	boolean sentryFound;
	boolean enemyFound;
	boolean enemyVectorCalculated;
	boolean targetDead;
	double[] drive = new double[2];
	
	public void run() {
		// give opponent position some values to prevent crashing
		
		opponentPastPos[0] = 0.0;
		opponentPastPos[1] = 0.0;
		time[0] = 0.0;
		time[1] = 0.0;
		driveTime = 0.0;
		power = 2.0;
		accuracy = 0.0;
		targetDead = false;
		
		sentryFound = false;
		enemyFound = false;
		enemyVectorCalculated = false;
		WIDTH = (int)getBattleFieldWidth();
		HEIGHT = (int)getBattleFieldHeight();
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);
		
		// Main loop
		while(true) {
			//radarPing(opponentPos[0], opponentPos[1]);
			turnRadarRight(Double.POSITIVE_INFINITY);
			
		}
	}

	/* ---------- Data Collection Methods -------------- */
	
	// returns the XY coordinates of a scanned enemy robot
	private double[] getEnemyPosition(ScannedRobotEvent e) {
		double enemyAbsoluteAngle = (getHeading() + e.getBearing() + 360)%360;
		double enemyRelativeAngle = 0;
		enemyRelativeAngle = enemyAbsoluteAngle % 90;
		if(enemyAbsoluteAngle%180 ==  enemyRelativeAngle%180){
			enemyRelativeAngle = 90 - enemyAbsoluteAngle%180;
		}
		double x = e.getDistance()*Math.cos(enemyRelativeAngle* Math.PI/180);
		double y = e.getDistance()*Math.sin(enemyRelativeAngle* Math.PI/180);
		
		if(enemyAbsoluteAngle >= 0 && enemyAbsoluteAngle < 90){
			
		}else if(enemyAbsoluteAngle >= 90 && enemyAbsoluteAngle < 180){
			y = -y;
		}else if(enemyAbsoluteAngle >= 180 && enemyAbsoluteAngle < 270){
			x = -x;
			y = -y;
		}else if(enemyAbsoluteAngle >= 270 && enemyAbsoluteAngle < 360){
			x = -x;
		}
		double enemyX = getX() + x;
		double enemyY = getY() + y;
		double[] result = {enemyX, enemyY};
		return result;
	}

	// sets the targetDead boolean to true, enabling other robots to now be targeted
	public void onRobotDeath(RobotDeathEvent e) {
		if(e.getName().equals(target)) targetDead = true;
	}
	
	// radar is constantly rotating, this method checks what type of robot it has scanned and takes appropriate action
	public void onScannedRobot(ScannedRobotEvent e){
		if(e.isSentryRobot()){
			if(!sentryFound){
				sentryPosition = getEnemyPosition(e);
				sentryFound = true;
			}
		} else if(e.getName().equals(target)) {
			updateData(e);
			initiateDestruction();
		} else if(target.equals("")) {
			target = e.getName();
			updateData(e);
			initiateDestruction();
		} else {
			// target has died or closer
			if (targetDead || getDistance(opponentPos) > e.getDistance()) {
				opponentPastPos[0] = 0.0;
				opponentPastPos[1] = 0.0;
				driveTime = 0.0;
				power = 2.0;
				accuracy = 0.0;
				targetDead = false;
				enemyFound = false;
				target = e.getName();
				updateData(e);
				initiateDestruction();
			}
		}
	}
	
	// initiates destruction :)
	// fires a shot, then calculates where to move based on the enemy's location relative to the sentry
	// If they are in a location where we can't move to an optimal position, we then simply move away from
	// the enemy.
	public void initiateDestruction() {
		smartShot();
		if(enemyFound && sentryFound){
			drive = enemyInFrontOfSentry();
			if(drive[0] > WIDTH - 10 || drive[0] < 50 || drive[1] > HEIGHT - 50 || drive[1] < 50){
				drive[0] = (sentryPosition[0] < WIDTH/2) 
						? WIDTH - opponentPos[0]/2 
						: WIDTH/2 - opponentPos[0]/2 ;
				drive[1] = (sentryPosition[1] < WIDTH/2) 
						? HEIGHT  - opponentPos[1]/2 
						: HEIGHT/2 - opponentPos[1]/2 ;
			}
		}else{
			drive[0] = WIDTH - opponentPos[0];
			drive[1] = HEIGHT - opponentPos[1];
		}
		driveTo(Math.round(drive[0]), Math.round(drive[1]));
	}
	
	// updates our stored time, vector, and robot position
	public void updateData(ScannedRobotEvent e) {
		if (enemyFound) enemyVectorCalculated = true;
		// update enemy data
		enemyFound = true;
		opponentPastPos[0] = opponentPos[0];
		opponentPastPos[1] = opponentPos[1];
		opponentPos = getEnemyPosition(e);
		globalVector = getVector(opponentPastPos,opponentPos);
		time[0] = time[1];
		time[1] = getTime();
		driveTime = time[1] - time[0];
	}
	
	/* ------------------- Math Helper Methods -------------------*/
	
	// calculates a vector based off two positions
	private double[] getVector(double[] pos, double pos2[]) {
		double[] result = new double[2];
		result[0] = pos2[0] - pos[0];
		result[1] = pos2[1] - pos[1];
		return result;
	}
	
	// calculates the magnitude of a vector
	private double getMagnitude(double v[]) {
		return Math.sqrt(Math.pow(Math.abs(v[0]), 2) + Math.pow(Math.abs(v[1]), 2));
	}
	
	// calculates the distance between a location and our robot using the getMagnitude() method
	private double getDistance(double v[]) {
		v[0] -= getX();
		v[1] -= getY();
		double result = getMagnitude(v);
		return result;
	}
	
	/* ------------------- Movement Methods ----------------------*/
	
	// returns a point on a line that is aligned with the sentry and the enemy
	public double[] enemyInFrontOfSentry(){
		double y2 = sentryPosition[1];
		double y1 = opponentPos[1];
		double x2 = sentryPosition[0];
		double x1 = opponentPos[0];
		double angleAsDegrees;
		double angleAsRadians;
		double yy = y2 - y1;
		double xx = x2 - x1;
		if(xx != 0){
			angleAsDegrees = Math.atan2(yy, xx) * 180/Math.PI;
			angleAsDegrees = (360 - (angleAsDegrees + 90))%360;
		}else{
			if(y2 > y1){
				angleAsDegrees = 180;
			}else{
				angleAsDegrees = 0;
			}
		}
		angleAsRadians = angleAsDegrees * Math.PI/180;
		double x3 = 190;
		double y3 = 190;
		if(angleAsDegrees >= 0 && angleAsDegrees < 90){
			
		}else if(angleAsDegrees >= 90 && angleAsDegrees < 180){
			y3 = -y3;
		}else if(angleAsDegrees >= 180 && angleAsDegrees < 270){
			x3 = -x3;
			y3 = -y3;
		}else if(angleAsDegrees >= 270 && angleAsDegrees < 360){
			x3 = -x3;
		}	
		double newX = x1 + Math.sin(angleAsRadians) + x3;
		double newY = y1 + Math.cos(angleAsRadians) + y3;
		return new double[]{newX, newY};
	}
	
	// drives to an XY coordinate, either in reverse or ahead, based on which angle is shorter to turn
	public void driveTo(double xTarget, double yTarget) {
		double x = getX(), y = getY(), heading = getHeading();
		boolean charge = true;
		double tempDistance = 75;
		if(xTarget == x && yTarget != y) {
			if (heading < 270 && heading > 90) {
				if(heading < 180)		turnLeft(heading - 180);
				else 				turnRight(180 - heading);
				if(yTarget < y)		
					ahead(tempDistance);
				else			
					back(tempDistance);
			} else {
				if (heading > 269)	turnRight(360 - heading);
				else 				turnLeft(heading);
				if(yTarget < y)	
					back(tempDistance);
				else	
					ahead(tempDistance);
			}
		} 
		else if (xTarget != x && yTarget == y) {
			if(heading < 180) {
				if(heading < 90)	turnRight(90 - heading);
				else				turnLeft(heading - 90);
				if(xTarget > x)		
					ahead(tempDistance);
				else	
					back(tempDistance);
			} else {
				if(heading > 270)	turnLeft(heading - 270);
				else				turnRight(270 - heading);
				if(xTarget > x)	
					back(tempDistance);
				else
					ahead(tempDistance);
			}
		} 
			else if (xTarget != x && yTarget != y) {
			double distance = Math.sqrt(Math.pow(Math.abs(xTarget - x), 2) + Math.pow(Math.abs(yTarget - y), 2));
			double [] v = {xTarget - x, yTarget - y};
			double [] v2 = new double[2];
			v2[0] = Math.cos(Math.toRadians(heading)) * v[0] - Math.sin(Math.toRadians(heading)) * v[1];
			v2[1] = Math.sin(Math.toRadians(heading)) * v[0] + Math.cos(Math.toRadians(heading)) * v[1];
			//
			double temp = (v2[1]/distance > 1) ? 0.999999999999 : v2[1]/distance;
			if(v2[1]/distance  < -1) temp = -0.999999999999;
			double angle = Math.toDegrees(Math.acos(temp));			
			if(angle > 90) {
				charge = false;
				if(v2[0] > 0) 	turnLeft(180 - angle);
				else			turnRight(180 - angle);
			} else {
				if(v2[0] > 0) 	turnRight(angle);
				else			turnLeft(angle);
			}
			if(charge)	ahead(tempDistance);
			else		back(tempDistance);
			
		}
	}
	
	/* ------------------ Targeting Methods ---------------------*/
	
	// calculates the time it takes to fire a bullet over a distance
	public long getBulletTime(double power, double distance){
		long result = (long) (distance/(20 - (3*power)));
		return result;
	}
	
	// change the accuracy on hit
	public void onBulletHit(BulletHitEvent event) {
		accuracy = 1.0;
	}
	
	// change the accuracy on hit
	public void onBulletMissed(BulletMissedEvent event) {
		accuracy = -1.0;
	}
	
	// get the time it takes to rotate the turret
	public long getTurretRotationTime(double[] v, double[] ourLocation) {
		long result;
		double heading = getGunHeading();
		
		double distance = Math.sqrt(Math.pow(Math.abs(v[0] - ourLocation[0]), 2) + Math.pow(Math.abs(v[1] - ourLocation[1]), 2));
		double [] v2 = new double[2];		
		// rotate v2 by heading
		v2[0] = Math.cos(Math.toRadians(heading)) * v[0] - Math.sin(Math.toRadians(heading)) * v[1];
		v2[1] = Math.sin(Math.toRadians(heading)) * v[0] + Math.cos(Math.toRadians(heading)) * v[1];
		
		// cosine law, simplified
		double angle = Math.toDegrees(Math.acos(v2[1]/distance));
		result = (long) (angle/20);
		return result;
		
	}
	
	// shoot at a XY location
	public void shootAt(double [] v0, double power)
	{
		double x = getX(), y = getY(), heading = getGunHeading();
		
		double distance = Math.sqrt(Math.pow(Math.abs(v0[0] - x), 2) + Math.pow(Math.abs(v0[1] - y), 2));
		double [] v = {v0[0] - x, v0[1] - y};
		double [] v2 = new double[2];		
		v2[0] = Math.cos(Math.toRadians(heading)) * v[0] - Math.sin(Math.toRadians(heading)) * v[1];
		v2[1] = Math.sin(Math.toRadians(heading)) * v[0] + Math.cos(Math.toRadians(heading)) * v[1];
		double temp = (v2[1]/distance > 1) ? 0.999999999999 : v2[1]/distance;
		if(v2[1]/distance  < -1) temp = -0.999999999999;
		double angle = Math.toDegrees(Math.acos(temp));
		if(v2[0] > 0) 	turnGunRight(angle);
		else			turnGunLeft(angle);
		fire(power);
	}
	
	// determines, based on distance, whether a basic or predictive shot should be fired, and what power to use
	private void smartShot() {
		boolean regularShot = true;
		double [] vect = {opponentPos[0]-getX(), opponentPos[1]-getY()};
		double distance = getMagnitude(vect);
		power = accuracy + 4.0 - (distance/WIDTH)*4;
		if(enemyVectorCalculated) {
			if(distance < 300 && distance > 50) {
				regularShot = false;
			}
		} 
		if(regularShot) {
			shootAt(opponentPos,power);
		} else {
			predictiveShot();
		}
	}
	
	// calculates where the target should be based on its stored vector, 
	private void predictiveShot() {
		double[] ourCurrentLoc = {getX(), getY()};
		double[] result = new double[2];
		// setting an initial target
		double distance = Math.sqrt(Math.pow(Math.abs(opponentPos[0] - ourCurrentLoc[0]), 2) + Math.pow(Math.abs(opponentPos[1] - ourCurrentLoc[1]), 2));
		// getting an initial time based off the target
		long time = getBulletTime(power, distance) + getTurretRotationTime(opponentPos, ourCurrentLoc);
		// Here write an iterative function using the result of the analysis method.
		result = analysis(time);
		shootAt(result,power);
	}
	
	// returns a coordinate for the based on the time input and their stored vector
	// adjusted to half for strategic reasons, as robots often stop
	private double[] analysis(long t) {
		double [] result = new double[2];
		result[0] = opponentPos[0] + (globalVector[0] * t/driveTime)/2;
		result[1] = opponentPos[1] + (globalVector[1] * t/driveTime)/2;
		return result;
	}
	
	/* --------------------- Victory Methods ----------------------*/
	
	// just for fun,.. don't really need an explanation...
	public void onWin(WinEvent event){
		while(true){
			dance();	
		}	
	}	
	public void dance(){
		Color bColor =			new Color((int)(Math.random()*256),(int)(Math.random()*256),(int)(Math.random()*256));
		Color gColor = 			new Color((int)(Math.random()*256),(int)(Math.random()*256),(int)(Math.random()*256));
		Color rColor =			new Color((int)(Math.random()*256),(int)(Math.random()*256),(int)(Math.random()*256));
		Color bulletColor =  	new Color((int)(Math.random()*256),(int)(Math.random()*256),(int)(Math.random()*256));
		Color scanArcColor =  	new Color((int)(Math.random()*256),(int)(Math.random()*256),(int)(Math.random()*256));
		setColors(bColor,gColor,rColor,bulletColor, scanArcColor);
		turnRadarRight(45);
	}
	
}