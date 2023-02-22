package ch.zuehlke.semu;

import java.awt.Color;

import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class TheUndertaker extends AdvancedRobot {

        public void run() {
            setBodyColor(Color.BLACK);
            setRadarColor(Color.RED);
            setBulletColor(Color.GREEN);
            setScanColor(Color.WHITE);

            setAdjustGunForRobotTurn(true);
            setAdjustRadarForGunTurn(true);

            while(true){
                turnRadarRightRadians(Double.POSITIVE_INFINITY);
            }
        }

        private String target;

        @Override
        public void onScannedRobot(ScannedRobotEvent event) {
            if (target == null && !event.isSentryRobot()) {
                target = event.getName();
            }

            /**
             if (getX() <= getSentryBorderSize()) {
             goTo(getSentryBorderSize() + getX(), getY());
             }
             else if (getX() > (getBattleFieldWidth() - 2 * getSentryBorderSize())) {
             goTo(getX() - getSentryBorderSize(), getY());
             }
             else if (getY() < getSentryBorderSize()) {
             goTo(getX(), getSentryBorderSize() + getY());
             }
             else if (getY() > (getBattleFieldHeight() - 2 * getSentryBorderSize())) {
             goTo(getX(), getY()  - getSentryBorderSize());
             }
             **/

            if (target.equals(event.getName())) {
                double enemyPosition =	event.getBearingRadians() + getHeadingRadians();
                setTurnRadarRightRadians((Utils.normalRelativeAngle(enemyPosition - getRadarHeadingRadians())));

                double toTurn = enemyPosition - getHeadingRadians();
                setTurnGunRightRadians(Utils.normalRelativeAngle((enemyPosition - getGunHeadingRadians() + toTurn)));

                setTurnRightRadians(Utils.normalRelativeAngle(toTurn));
                setAhead(event.getDistance() * 0.5);

                if (event.getDistance() > 300) {
                    setFire(1.1);
                } else if (event.getDistance() > 200) {
                    setFire(2.0);
                } else {
                    setFire(3);
                }
                execute();
            }
        }

        @Override
        public void onRobotDeath(RobotDeathEvent event) {
            if (target != null && event.getName().equals(target)) {
                target = null;
            }
        }

}