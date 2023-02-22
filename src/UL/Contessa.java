package UL;
import robocode.*;
import java.awt.Color;
import static robocode.util.Utils.normalRelativeAngleDegrees;

public class Contessa extends Robot{

double x;
double y; 
double dist;
double myHeading;
double myBearing;
double lastEnergy = 0; 
double enemyBearing; 
boolean leftside; 
double distFromBot;  
int direction = 1;
int dodgeCount; 

	public void run() {
		setAdjustGunForRobotTurn(true);
		dist = 46; 
		out.println(dist);
		x = getX(); 
		y = getY(); 
		myHeading = getHeading();
		/* Based on where the bot spawns, it will choose which direction it will start the search
		  This prevents times when the bot will be scanning a wall, allowing it to find opponents faster
		  */
		 
		if(x>400 && y > 400){ 			//Bot spawns in 1st quadrant 
			if(myHeading >= 45 && myHeading < 225){
				turnRadarRight(-360); 
			}else{
				turnRadarLeft(-360);
			}
		}else if(x>400 && y<400){		//Bot spawns in 2nd quadrant
			if(myHeading < 315 && myHeading > 135){
				turnRadarLeft(-360);
			}else{
				turnRadarRight(-360); 
			}
		}else if(x<400 && y<400){		//Bot spawns in 3rd quadrant
			if(myHeading >= 45 && myHeading < 225){
				turnRadarRight(-360);
			}else{
				turnRadarLeft(-360);
			}
		}else{							// Bot spawns in 4th quadrant or exactly in the middle
			if(myHeading < 315 && myHeading > 135){
				turnRadarLeft(-360);
			}else{
				turnRadarRight(-360); 
			}			
		}
		
		while(true){
			turnGunRight(10*direction);
			out.println("Turning gun " + direction);
		}
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		if(	e.isSentryRobot()){
			return; 
		}
		enemyBearing = e.getBearing();
		out.println("Bearing is " + enemyBearing);
		distFromBot = e.getDistance(); 
		out.println("Distance is " + distFromBot);
		
	
		//Tracking code
		double absoluteBearing = getHeading() + e.getBearing();
		double bearingFromGun = normalRelativeAngleDegrees(absoluteBearing - getGunHeading());
		
		if (Math.abs(bearingFromGun) <= 3) {
			turnGunRight(bearingFromGun);
			if (getGunHeat() == 0 && e.isSentryRobot() == false) {
				smartFire(e.getDistance());
			}
		} else {
			turnGunRight(bearingFromGun);
		}
			
		if(e.getVelocity()==0 && getGunHeat() == 0 && getEnergy()>15){
			out.println("Shooting sitting duck");
			fire(3);
		}
		
		if(distFromBot > 100 && getGunHeat() != 0){
			//Turns the tank perpendicular to the opponent 
			if (90 > enemyBearing&& enemyBearing>= 0){		//1st quadrant
				turnLeft(90 - enemyBearing); 
				turnGunRight(0);
			}else if(180 > enemyBearing&& enemyBearing>= 90){ // 2nd quadrant
				turnRight (enemyBearing - 90); 
				turnGunLeft(0);
			}else if(-90 > enemyBearing && enemyBearing>= -180){//3rd quadrant 
				turnLeft(enemyBearing + 90);
				turnGunRight(0);
			}else{													//4th quadrant 
				turnRight(90 + enemyBearing);
				turnGunLeft(0);
			}
		
			/*detects if small energy is lost from the opponent's tank and moves half the length of the tank 
			could be shots from enemy, or might be low-power bullets and wall collisions
			gu enuf doe */
		
			if( (lastEnergy = lastEnergy - e.getEnergy()) <= 3 && lastEnergy > 0){
				if(dodgeCount == 2){
					dist = dist * -1; 
					dodgeCount = 0;
					} 
				ahead(dist);
				dodgeCount++; 
				if(dist >0){ // The tank moves forward 
					out.println("Moving foward");
					if(enemyBearing < 0){ //Enemy on Left side
						direction = -1;
						out.println("Enemy on left");
					}else{
						direction = 1; 
						out.println("Enemy on right");
					}
				}else{//Tank moves backwards 
				out.println("Moving backwards");
					if(enemyBearing < 0){
						direction = 1; 
						out.println("Enemy on left");
					}else{
						direction = -1;
						out.println("Enemy on right");
					}
				}
			}
			
		}	
	
		lastEnergy = e.getEnergy();	
	}

	public void onHitByBullet(HitByBulletEvent e) {
		
		
	}
	
	public void onHitRobot (HitRobotEvent e){
		
	}
	
	public void onHitWall(HitWallEvent e) {
		if(Math.abs(e.getBearing()) <= 90){
			turnRight(e.getBearing()); 
			ahead(-100);
		}else{
			turnRight(e.getBearing());
			ahead(100); 
		}
	}	
	
	public void smartFire(double robotDistance) {
		if (robotDistance > 400) {
			fire(1);
		} else if (robotDistance > 200) {
			fire(2);
		} else {
			fire(3);
		}
	}
		
	
}
