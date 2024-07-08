package me.vekster.lightanticheat.check.checks.combat.killaura;

import me.vekster.lightanticheat.Main;
import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.buffer.Buffer;
import me.vekster.lightanticheat.check.checks.combat.CombatCheck;
import me.vekster.lightanticheat.event.playerattack.LACAsyncPlayerAttackEvent;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.player.cache.PlayerCache;
import me.vekster.lightanticheat.player.cache.history.HistoryElement;
import me.vekster.lightanticheat.player.cache.history.PlayerCacheHistory;
import me.vekster.lightanticheat.util.cooldown.CooldownUtil;
import me.vekster.lightanticheat.util.hook.plugin.FloodgateHook;
import me.vekster.lightanticheat.util.precise.AccuracyUtil;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import me.vekster.lightanticheat.version.VerUtil;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

/**
 * Hitbox
 */
public class KillAuraB extends CombatCheck implements Listener {
    public KillAuraB() {
        super(CheckName.KILLAURA_B);
    }

    @EventHandler
    public void onAsyncHit(LACAsyncPlayerAttackEvent event) {
        LACPlayer lacPlayer = event.getLacPlayer();
        Player player = event.getPlayer();
        Location eyeLocation = player.getEyeLocation().clone();
        double yawChange = getYawChange(eyeLocation, lacPlayer);

        if (!isCheckAllowed(player, lacPlayer))
            return;

        if (lacPlayer.isGliding() || lacPlayer.isRiptiding())
            return;

        Buffer buffer = getBuffer(player, true);
        Entity entity = null;
        int entityId = event.getEntityId();
        boolean exists = buffer.isExists(String.valueOf(entityId));

        if (exists) {
            entity = buffer.getEntity(String.valueOf(entityId));
        } else {
            for (Entity entity1 : CooldownUtil.getAllEntitiesAsync(lacPlayer.cooldown, player)) {
                if (entity1.getEntityId() != entityId)
                    continue;
                entity = entity1;
                break;
            }
        }
        if (entity == null)
            return;
        if (!exists)
            buffer.put(String.valueOf(entityId), entity);

        if (VerUtil.getWidth(entity) > 2.0)
            return;
        if (VerUtil.getWidth(entity) == 10 && VerUtil.getHeight(entity) == 10)
            return;

        if (distanceToHitbox(player, entity) != -1)
            return;

        Location entityLocation = entity.getLocation();
        double entityHalfWidth = VerUtil.getWidth(entity) / 2.0;
        float angle = Float.MAX_VALUE;

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Location location = entityLocation.add(x * entityHalfWidth, 0, z * entityHalfWidth);
                Vector vector = location.toVector().setY(0.0D).subtract(eyeLocation.toVector().setY(0.0D));
                angle = Math.min(angle, eyeLocation.getDirection().setY(0.0D).angle(vector) * 57.2958F);
            }
        }

        double halfDiagonal = Math.sqrt(Math.pow(entityHalfWidth, 2) * 2);
        double distance = distanceHorizontal(eyeLocation, entityLocation) - halfDiagonal;
        double extraOffset = 0.0;
        if (distance < 1.0)
            return;
        PlayerCacheHistory<Location> eventHistory = lacPlayer.cache.history.onEvent.location;
        PlayerCacheHistory<Location> packetHistory = lacPlayer.cache.history.onPacket.location;
        Location location = player.getLocation();
        if (distanceHorizontal(eventHistory.get(HistoryElement.FROM), entityLocation) - distanceHorizontal(location, entityLocation) > 0.2 ||
                distanceHorizontal(packetHistory.get(HistoryElement.FROM), entityLocation) - distanceHorizontal(location, entityLocation) > 0.2 ||
                distance(eventHistory.get(HistoryElement.FROM), entityLocation) - distance(location, entityLocation) > 0.3 ||
                distance(packetHistory.get(HistoryElement.FROM), entityLocation) - distance(location, entityLocation) > 0.3) {
            if (distance <= 1.2)
                return;
            if (distance <= 1.3)
                extraOffset += 15.0;
            else if (distance <= 1.4)
                extraOffset += 10.0;
            else if (distance <= 1.5)
                extraOffset += 5.0;
        } else {
            if (distance <= 1.1)
                extraOffset += 9.0;
            else if (distance <= 1.2)
                extraOffset += 6.0;
            else if (distance <= 1.3)
                extraOffset += 3.0;
        }


        double maxAngle = Math.atan(halfDiagonal / distance) * 57.2958F;

        if (distanceHorizontal(eventHistory.get(HistoryElement.FIRST), eventHistory.get(HistoryElement.FROM)) >
                (0.21585 + 0.28061) / 2)
            maxAngle += 25;
        if (FloodgateHook.isProbablyPocketEditionPlayer(player, true))
            maxAngle += 15;
        if (FloodgateHook.isBedrockPlayer(player))
            maxAngle += 15;

        double yawChange1 = getYawChange(eyeLocation, lacPlayer);
        float finalAngle = angle;
        Entity finalEntity = entity;
        double finalExtraOffset = extraOffset;
        double finalMaxAngle = maxAngle;
        Scheduler.runTaskLater(player, () -> {
            if (distanceToHitbox(player, finalEntity) != -1)
                return;

            double yawChange2 = getYawChange(eyeLocation, lacPlayer);
            double yawChange3 = getYawChange(player.getEyeLocation(), lacPlayer);
            float resultAngle = finalAngle - (float) max(yawChange, yawChange1, yawChange2, yawChange3);
            if (resultAngle <= finalMaxAngle * 1.1 + 5 + finalExtraOffset)
                return;

            if (System.currentTimeMillis() - buffer.getLong("lastFlagTime") <= 200) return;
            else buffer.put("lastFlagTime", System.currentTimeMillis());

            if (AccuracyUtil.isViolationCancel(getCheckSetting(), buffer))
                return;

            if (getItemStackAttributes(player, "PLAYER_SWEEPING_DAMAGE_RATIO") != 0 ||
                    getPlayerAttributes(player).getOrDefault("PLAYER_SWEEPING_DAMAGE_RATIO", 0.0) > 0.01)
                buffer.put("attribute", System.currentTimeMillis());
            if (System.currentTimeMillis() - buffer.getLong("attribute") < 3500)
                return;

            callViolationEventIfRepeat(player, lacPlayer, null, buffer, Main.getBufferDurationMils() - 1000L);
        }, 1);
    }

    private static double getYawChange(Location eyeLocation, LACPlayer lacPlayer) {
        float yaw = yaw(eyeLocation.getYaw());
        PlayerCache.History history = lacPlayer.cache.history;
        return Math.min(Math.abs(yaw - yaw(history.onEvent.location.get(HistoryElement.FROM).getYaw())),
                Math.abs(yaw - yaw(history.onPacket.location.get(HistoryElement.FROM).getYaw())));
    }

    private static float yaw(float yaw) {
        yaw = yaw % 360;
        return yaw >= 0 ? yaw : 360 - yaw;
    }

    private static double max(double first, double second, double third, double fourth) {
        return Math.max(
                Math.max(first, second),
                Math.max(third, fourth)
        );
    }

}
