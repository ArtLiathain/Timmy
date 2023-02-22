package ch.zuehlke.mhau;

public class ScannedRobot {
    private final String name;
    private final double bearing;
    private final double distance;
    private final double energy;
    private final double heading;
    private final double velocity;

    public ScannedRobot(String name, double bearing, double distance, double energy, double heading, double velocity) {
        this.name = name;
        this.bearing = bearing;
        this.distance = distance;
        this.energy = energy;
        this.heading = heading;
        this.velocity = velocity;
    }

    public double getBearing() {
        return bearing;
    }

    public double getDistance() {
        return distance;
    }

    public double getEnergy() {
        return energy;
    }

    public double getHeading() {
        return heading;
    }

    public double getVelocity() {
        return velocity;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "ScannedRobot{" +
                "name='" + name + '\'' +
                ", bearing=" + bearing +
                ", distance=" + distance +
                ", energy=" + energy +
                ", heading=" + heading +
                ", velocity=" + velocity +
                '}';
    }
}
