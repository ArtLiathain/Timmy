package test01;
import robocode.*;
import robocode.util.Utils;
import java.awt.Color;

// API help : https://robocode.sourceforge.io/docs/robocode/robocode/Robot.html

/**
 * ECPHBG - a robot by (your name here)
 */
public class ECPHBG extends Robot
{
 public void run() {
        setup();

        // fairly shamelessly taking this from some other bots
        // the radar is basically what controls the bot
        while(true){
            turnRadarRight(Double.POSITIVE_INFINITY);
        }
    }
	
	 private void setup(){
        // set colours
        setBodyColor(Color.lightGray);
        setRadarColor(Color.yellow);
        // all will bow down to teh might of COFFEE!!!!!!
        setBulletColor(Color.decode("#C0FFEE"));
        setScanColor(Color.GREEN);
        setAdjustRadarForRobotTurn(true);
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
    }	
	
	private String target;
    private double target_distance;

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        // no need to deal with these
        if (event.isSentryRobot()){
            return;
        }

        // reduce calls as much as possible
        String name = event.getName();
        double distance =  event.getDistance();

        // pick first target if current one is dead
        if (target == null) {
            target = name;
            target_distance = distance;
            this.out.println("Tracking " + name);
        }
		 // however if there is a closer target then go for that
        if (distance < target_distance) {
            target = name;
            target_distance = distance;
            this.out.println("Tracking " + name);
        }


        if (target.equals(event.getName())) {
            double bearing = event.getBearing();
            double heading = getHeading();
            double heading_radar = getRadarHeading();
            double heading_gun = getGunHeading();
            double heat = getGunHeat();

            // taken inspiration from teh sample tracker firing
            double absoluteBearing = heading + bearing;
            double bearingFromGun = Utils.normalRelativeAngleDegrees(absoluteBearing - heading_gun);

            // turn gun to target
            turnGunRight(bearingFromGun);
            // chase down target
            turnRight(bearingFromGun);
            ahead(distance * 0.75);

            if (heat == 0.0D) {
                if (distance > 300) {
                    // if the bullets energy is above 1 it does bonus damage, so basically free damage
                    fire(1.1);
                } else if (distance > 200) {
                    fire(2.0);
                } else {
                    // just in case the rules change use the enum
                    fire(Rules.MAX_BULLET_POWER);
                }
            }
        }
    }
	
	 @Override
    public void onRobotDeath(RobotDeathEvent event) {
        if (event.getName().equals(target)) {
            target = null;
            target_distance = Integer.MAX_VALUE;
        }
    }
	
}
