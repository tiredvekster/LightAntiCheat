package kireiko.dev.utils.math;

import lombok.NonNull;

public class ScalaredMath {

    public static double scaleVal(double value, double scale) {
        double scale2 = Math.pow(10, scale);
        return Math.ceil(value * scale2) / scale2;
    }
    public static boolean validRayLines(@NonNull RayLine a, @NonNull RayLine b, double radi) {
        double angleA = Math.atan2(a.z(), a.x());
        double angleB = Math.atan2(b.z(), b.x());
        
        double angleDiff = Math.abs(angleA - angleB);
        
        if (angleDiff > Math.PI) {
            angleDiff = 2 * Math.PI - angleDiff;
        }
        
        return angleDiff <= Math.toRadians(radi);
    }
    public static double getOnlyScale(double value) {
        return value - Math.floor(value);
    }
    
}
