package me.vekster.lightanticheat.api;

import org.bukkit.entity.Player;

import java.util.Set;

public interface LACApi {
    static LACApi getInstance() {
        return InstanceHolder.getApi();
    }

    Set<String> getCheckNames(CheckType checkType);

    boolean disableDetection(Player player, String checkName);

    boolean disableDetection(Player player, String checkName, long durationMils);

    boolean enableDetection(Player player, String checkName);

    DetectionStatus getDetectionStatus(Player player, String checkName);
}
