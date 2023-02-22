package UL;
import robocode.*;
import java.awt.Color;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * Zoidborg - a robot by UL_Zoidborg
 */
public class Zoidborg extends Robot
{
	double prevEnergy = 100, distance, bearing;
	int movementDirection = 1,gunDirection = 1;
	//The main loop with initial scan, don't forget to se the colours
	public void run() {
		setColors(Color.pink,Color.white,Color.red);
		turnRadarRight(360);
		while(true) {
		move();
		}
	}
	//This is the method with robots movement
	public void move(){
		double heading = getHeading();
		double quadrant = 0;
		//This part makes the bot to bounce off under an angle when it gets too close to the walls.
		if (!((heading >= 80) && (heading <= 100)) && !((heading >= 260) && (heading <= 270)))
			quadrant = Math.tan(heading * Math.PI /180);
		/*The direction of the tank rotation changes depending on the wall and angle 
		under which it approaches the wall to restrict the time required for this move*/
		if(getX() <= 50)
		{
			movementDirection = -movementDirection;
			if(quadrant > 0.1)
				turnRight(90);
			else if(quadrant < -0.1)
				turnLeft(90);
			ahead(150*movementDirection);
		}
		else if(getX() >= 750)
		{
			movementDirection = -movementDirection;
			if(quadrant < -0.1)
				turnLeft(90);
			else if(quadrant > 0.1)
				turnRight(90);
			ahead(150*movementDirection);
		}
		else if(getY() <= 50)
		{			
			movementDirection = -movementDirection;
			if(quadrant > 0.1)
				turnLeft(90);
			else if(quadrant < -0.1)
				turnRight(90);
			ahead(150*movementDirection);
		}
		else if(getY() >= 750)
		{
			movementDirection = -movementDirection;
			if(quadrant < -0.1)
				turnRight(90);
			else if(quadrant > 0.1)
				turnLeft(90);
			ahead(150*movementDirection);
		}
		/*This is the core evading movement, the pattern changes 
		depending on the distance from the opponent. The tank scans
		180 degree area to keep track of the opponent.*/
		else if(distance > 150)
		{
			gunDirection = -gunDirection;
			movementDirection = -movementDirection;
			turnRight(bearing+90-30*movementDirection);
			ahead((distance/4+25)*movementDirection);
			turnGunRight(180*gunDirection);
		}
		else
		{
			gunDirection = -gunDirection;
			turnRight(bearing+90-30*movementDirection);
			movementDirection = -movementDirection;
			ahead((distance/4+40)*movementDirection);
			turnGunRight(180*gunDirection);

		}

	}
	//This is how the tank reacts when it scans an enemy robot
	public void onScannedRobot(ScannedRobotEvent e) 
	{
		//Execute following code only if the scanned robot in not a sentry
		if(!e.isSentryRobot())
		{
		//Keep track of changes in opponents energy(shooting)
	    double oppEnergyChange = prevEnergy-e.getEnergy();
		//Get the bearing and distance of the enemy robot
		bearing = e.getBearing();
		distance = e.getDistance();
		//Reverse movement when enemy shoots
		if (oppEnergyChange > 0 && oppEnergyChange <= 3)
			movementDirection = -movementDirection;
		//Change the power of the bullet depending on the distance from enemy tank
		int i;
		if(distance <=250)
			i = 3;
		else if(distance <=550)
			i = 2;
		else 
			i = 1;
		if(getEnergy() < 20 && (e.getEnergy()*2) >= getEnergy())
			i/=2;
	    fire(i);
		//Update the previous energy with a new value
	    prevEnergy = e.getEnergy();
		}
	}
	//Victory dance!
	public void onWin(WinEvent e) 
	{
		for (int i = 0; i < 50; i++) 
		{
			turnRight(30);
			turnLeft(30);
		}
	}
}
