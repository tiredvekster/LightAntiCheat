package kireiko.dev.utils.math;



public class RayLine {

    private double x;
    private double z;

    public RayLine(double x, double z) {
        this.x = x;
        this.z = z;
    }

    public double x() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double z() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    @Override
    public String toString() {
        return "RayLine{" +
                "x=" + x +
                ", z=" + z +
                '}';
    }
}