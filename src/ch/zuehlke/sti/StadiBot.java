package ch.zuehlke.sti;

import robocode.AdvancedRobot;
import robocode.HitRobotEvent;
import robocode.ScannedRobotEvent;

import java.awt.*;


/**
 * SpinBot - a sample robot by Mathew Nelson.
 * <p>
 * Moves in a circle, firing hard when an enemy is detected.
 *
 * @author Mathew A. Nelson (original)
 * @author Flemming N. Larsen (contributor)
 */
public class StadiBot extends AdvancedRobot {

    /**
     * SpinBot's run method - Circle
     */
    public void run() {
        // Set colors
        setBodyColor(Color.black);
        setGunColor(Color.yellow);
        setRadarColor(Color.gray);
        setScanColor(Color.green);

        // Loop forever
        int turns = 0;
        while (true) {
            ahead(10000);
        }
    }

    /**
     * onScannedRobot: Fire hard!
     */
    public void onScannedRobot(ScannedRobotEvent e) {
        if (!e.isSentryRobot()) {
            fire(3);
        }
    }

    /**
     * onHitRobot:  If it's our fault, we'll stop turning and moving,
     * so we need to turn again to keep spinning.
     */
    public void onHitRobot(HitRobotEvent e) {
        if (e.getBearing() > -10 && e.getBearing() < 10) {
            fire(3);
        }
        if (e.isMyFault()) {
            turnRight(10);
        }
    }
}
