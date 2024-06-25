package kireiko.dev.utils.math.client;

public class MoveEngine {
    final public static double[] BASIC_SPEED = new double[]{
            0.63, // 0
            0.37,
            0.37,
            0.36,
            0.35,
            0.35,
            0.34,
            0.34,
            0.34,
            0.33,
            0.32,
            0.32,
            0.32 // 12
    };
    final public static double[] FLY_SPEED = new double[]{
            0.42, 0.34, 0.25, 0.17, 0.09, 0.01, -0.07, -0.15, -0.22, -0.3,
            -0.37, -0.44, -0.51, -0.58, -0.64, -0.71, -0.77, -0.84, -0.9,
            -0.96, -1.02, -1.08, -1.13, -1.24, -1.19, -1.3, -1.35, -1.4,
            -1.45, -1.5, -1.55, -1.6, -1.64, -1.69, -1.73, -1.78, -1.82,
            -1.86, -1.9, -1.94, -1.98, -2.02, -2.06, -2.1, -2.13, -2.17,
            -2.2, -2.24, -2.27, -2.3, -2.34, -2.37, -2.4, -2.43, -2.46,
            -2.49, -2.52, -2.54, -2.6, -2.57, -2.65, -2.62, -2.68, -2.7,
            -2.72, -2.75, -2.77, -2.79, -2.82, -2.84, -2.86, -2.88, -2.9,
            -2.92, -2.94, -2.96, -2.98, -3.0
    };
    public static double getSpeedByTick(int tick) {
        if (tick < 0 || tick > 12) {
            return BASIC_SPEED[12];
        }
        return BASIC_SPEED[tick];
    }
    public static double getFlySpeedByTick(int tick) {
        if (tick < 0 || tick > 76) {
            return FLY_SPEED[76];
        }
        return FLY_SPEED[tick];
    }
}