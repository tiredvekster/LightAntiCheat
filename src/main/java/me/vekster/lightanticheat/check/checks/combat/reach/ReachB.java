package me.vekster.lightanticheat.check.checks.combat.reach;

import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.buffer.Buffer;
import me.vekster.lightanticheat.check.checks.combat.CombatCheck;
import me.vekster.lightanticheat.event.playerattack.LACPlayerAttackEvent;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.player.cache.PlayerCache;
import me.vekster.lightanticheat.player.cache.history.HistoryElement;
import me.vekster.lightanticheat.util.async.AsyncUtil;
import me.vekster.lightanticheat.util.hook.plugin.simplehook.EliteMobsHook;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Vector reach
 */
public class ReachB extends CombatCheck implements Listener {
    public ReachB() {
        super(CheckName.REACH_B);
    }

    @EventHandler
    public void onHit(LACPlayerAttackEvent event) {
        if (!event.isEntityAttackCause())
            return;
        LACPlayer lacPlayer = event.getLacPlayer();
        PlayerCache cache = lacPlayer.cache;
        Player player = event.getPlayer();
        Entity entity = event.getEntity();

        if (!isCheckAllowed(player, lacPlayer))
            return;

        double distance = distanceToHitbox(player, entity);
        if (distance == -1.0)
            return;

        double maxReach = 3.0;

        double eventBackwardsDistance = 0;
        if (distance(cache.history.onEvent.location.get(HistoryElement.FROM), entity.getLocation()) <
                distance(player.getLocation(), entity.getLocation()))
            eventBackwardsDistance = distance(cache.history.onEvent.location.get(HistoryElement.FROM), player.getLocation());
        double packetBackwardsDistance = 0;
        if (distance(cache.history.onPacket.location.get(HistoryElement.FROM), entity.getLocation()) <
                distance(player.getLocation(), entity.getLocation()))
            packetBackwardsDistance = distance(cache.history.onPacket.location.get(HistoryElement.FROM), player.getLocation());
        double backwardsDistance = Math.max(eventBackwardsDistance, packetBackwardsDistance);
        maxReach += backwardsDistance;
        maxReach += backwardsDistance * (lacPlayer.getPing() / 1000.0 * 20.0);

        if (entity instanceof Player) {
            Player target = (Player) entity;
            PlayerCache targetCache = LACPlayer.getLacPlayer(target).cache;
            double targetEventBackwardsDistance = 0;
            if (distance(targetCache.history.onEvent.location.get(HistoryElement.FROM), player.getLocation()) <
                    distance(target.getLocation(), player.getLocation()) + 0.1)
                targetEventBackwardsDistance = distance(targetCache.history.onEvent.location.get(HistoryElement.FROM), target.getLocation());
            double targetPacketBackwardsDistance = 0;
            if (distance(targetCache.history.onPacket.location.get(HistoryElement.FROM), player.getLocation()) <
                    distance(target.getLocation(), player.getLocation()) + 0.1)
                targetPacketBackwardsDistance = distance(targetCache.history.onPacket.location.get(HistoryElement.FROM), target.getLocation());
            double targetBackwardsDistance = Math.max(targetEventBackwardsDistance, targetPacketBackwardsDistance);
            maxReach += targetBackwardsDistance;
        } else if (!entity.isOnGround()) {
            if (EliteMobsHook.isPluginInstalled()) {
                maxReach += 0.35;
            } else if (!flyingEntities.contains(entity.getType())) {
                Block block = AsyncUtil.getBlock(entity.getLocation());
                if (block == null || !block.isLiquid())
                    maxReach += 0.25;
            }
        }

        maxReach = Math.min(maxReach, 6.5);

        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE)
            maxReach += 2.5;

        if (entity instanceof Projectile)
            maxReach += 0.5;

        Location eyeLocation = player.getEyeLocation();
        Location entityLocation = entity.getLocation();
        if (distanceHorizontal(eyeLocation, entityLocation) * 1.5 < distanceAbsVertical(eyeLocation, entityLocation))
            maxReach += 0.5;

        maxReach += 0.45;

        if (distance <= maxReach)
            return;

        Buffer buffer = getBuffer(player);
        buffer.put("flags", buffer.getInt("flags") + 1);
        if (buffer.getInt("flags") <= 1)
            return;

        if (getItemStackAttributes(player, "PLAYER_ENTITY_INTERACTION_RANGE") != 0 ||
                getPlayerAttributes(player).getOrDefault("PLAYER_ENTITY_INTERACTION_RANGE", 0.0) > 0.01)
            buffer.put("attribute", System.currentTimeMillis());
        if (System.currentTimeMillis() - buffer.getLong("attribute") < 2000)
            return;

        callViolationEvent(player, event.getLacPlayer(), event.getEvent());
    }

}
