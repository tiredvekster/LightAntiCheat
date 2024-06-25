package me.vekster.lightanticheat.player.violation;

import me.vekster.lightanticheat.check.CheckName;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerViolations {

    private final Map<CheckName, Integer> VIOLATIONS = new ConcurrentHashMap<>();

    public int getViolations(CheckName checkName) {
        return VIOLATIONS.getOrDefault(checkName, 0);
    }

    public void setViolations(CheckName checkName, int value) {
        VIOLATIONS.put(checkName, value);
    }

    public void increaseViolations(CheckName checkName, int value) {
        VIOLATIONS.put(checkName, getViolations(checkName) + value);
    }

    public long violationLogTime = 0;
    public long punishmentLogTime = 0;
    public long violationDebugTime = 0;
    public long punishmentDebugTime = 0;
    public long violationDiscordTime = 0;
    public long punishmentDiscordTime = 0;

}
