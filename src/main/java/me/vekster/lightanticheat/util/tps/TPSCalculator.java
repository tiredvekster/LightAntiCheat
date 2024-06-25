package me.vekster.lightanticheat.util.tps;

import me.vekster.lightanticheat.util.annotation.SecureAsync;
import me.vekster.lightanticheat.util.scheduler.Scheduler;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TPSCalculator {

    private static final List<Double> TPS;
    private static final int CACHE_DURATION_IN_SEC = 30;
    private static final int CHECK_INTERVAL_IN_SEC = 5;
    private static long lastTickTime = 0;
    private static long lastTpsCheckTime = 0;
    private static double resultTps = 20.0;
    private static long lastResultTpsUpdate = 0;

    static {
        Double[] tps = new Double[CACHE_DURATION_IN_SEC / CHECK_INTERVAL_IN_SEC];
        Arrays.fill(tps, 20.0);
        TPS = Collections.synchronizedList(new LinkedList<>(Arrays.asList(tps)));
    }

    public static void loadTPSCalculator() {
        Scheduler.runTaskTimer(() -> {
            lastTickTime = System.currentTimeMillis();
        }, 1, 1);

        Scheduler.runTaskTimer(() -> {
            if (lastTpsCheckTime == 0) {
                lastTpsCheckTime = System.currentTimeMillis();
                return;
            }
            TPS.add((CHECK_INTERVAL_IN_SEC * 1000.0) / (System.currentTimeMillis() - lastTpsCheckTime) * 20.0);
            TPS.remove(0);
            lastTpsCheckTime = System.currentTimeMillis();
        }, CHECK_INTERVAL_IN_SEC * 20, CHECK_INTERVAL_IN_SEC * 20);
    }

    @SecureAsync
    public static double getTPS() {
        try {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastResultTpsUpdate < 1750)
                return resultTps;
            lastResultTpsUpdate = currentTime;
            resultTps = TPS.stream().mapToDouble(value -> value).average().orElse(20.0);
            return resultTps;
        } catch (NullPointerException e) {
            return 20.0;
        }
    }

    @SecureAsync
    public static long getTickDurationInMs() {
        try {
            return System.currentTimeMillis() - lastTickTime;
        } catch (NullPointerException e) {
            return 0;
        }
    }

}
