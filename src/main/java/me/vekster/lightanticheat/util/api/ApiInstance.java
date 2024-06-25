package me.vekster.lightanticheat.util.api;

import me.vekster.lightanticheat.api.CheckType;
import me.vekster.lightanticheat.api.DetectionStatus;
import me.vekster.lightanticheat.api.LACApi;
import org.bukkit.entity.Player;

import java.util.Set;

public class ApiInstance implements LACApi {
    @Override
    public Set<String> getCheckNames(CheckType checkType) {
        return ApiUtil.getCheckNames(checkType);
    }

    @Override
    public boolean disableDetection(Player player, String checkName) {
        return ApiUtil.disableCheck(player, checkName);
    }

    @Override
    public boolean disableDetection(Player player, String checkName, long durationMils) {
        return ApiUtil.disableCheck(player, checkName, durationMils);
    }

    @Override
    public boolean enableDetection(Player player, String checkName) {
        return ApiUtil.enableCheck(player, checkName);
    }

    @Override
    public DetectionStatus getDetectionStatus(Player player, String checkName) {
        return ApiUtil.getCheckStatus(player, checkName);
    }
}
