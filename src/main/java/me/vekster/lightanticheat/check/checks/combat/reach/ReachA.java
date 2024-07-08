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
import me.vekster.lightanticheat.version.VerUtil;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Horizontal reach
 */
public class ReachA extends CombatCheck implements Listener {
    public ReachA() {
        super(CheckName.REACH_A);
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

        if (VerUtil.getWidth(entity) == 10 && VerUtil.getHeight(entity) == 10)
            return;

        double hixboxOffset = Math.sqrt(Math.pow(VerUtil.getWidth(entity) / 2.0, 2) + Math.pow(VerUtil.getWidth(entity) / 2.0, 2));
        double distance = distanceHorizontal(player.getEyeLocation(), entity.getLocation()) - hixboxOffset;

        double maxReach = 3.0;

        double eventBackwardsDistance = 0;
        if (distanceHorizontal(cache.history.onEvent.location.get(HistoryElement.FROM), entity.getLocation()) <
                distanceHorizontal(player.getLocation(), entity.getLocation()) + 0.1)
            eventBackwardsDistance = distanceHorizontal(cache.history.onEvent.location.get(HistoryElement.FROM), player.getLocation());
        double packetBackwardsDistance = 0;
        if (distanceHorizontal(cache.history.onPacket.location.get(HistoryElement.FROM), entity.getLocation()) <
                distanceHorizontal(player.getLocation(), entity.getLocation()) + 0.1)
            packetBackwardsDistance = distanceHorizontal(cache.history.onPacket.location.get(HistoryElement.FROM), player.getLocation());
        double backwardsDistance = Math.max(eventBackwardsDistance, packetBackwardsDistance);
        maxReach += backwardsDistance;
        maxReach += backwardsDistance * (lacPlayer.getPing() / 1000.0 * 20.0);

        if (entity instanceof Player) {
            Player target = (Player) entity;
            PlayerCache targetCache = LACPlayer.getLacPlayer(target).cache;
            double targetEventBackwardsDistance = 0;
            if (distanceHorizontal(targetCache.history.onEvent.location.get(HistoryElement.FROM), player.getLocation()) <
                    distanceHorizontal(target.getLocation(), player.getLocation()) + 0.2)
                targetEventBackwardsDistance = distanceHorizontal(targetCache.history.onEvent.location.get(HistoryElement.FROM), target.getLocation());
            double targetPacketBackwardsDistance = 0;
            if (distanceHorizontal(targetCache.history.onPacket.location.get(HistoryElement.FROM), player.getLocation()) <
                    distanceHorizontal(target.getLocation(), player.getLocation()) + 0.2)
                targetPacketBackwardsDistance = distanceHorizontal(targetCache.history.onPacket.location.get(HistoryElement.FROM), target.getLocation());
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

        maxReach = Math.min(maxReach, 7.5);

        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE)
            maxReach += 2.5;

        if (entity instanceof Projectile)
            maxReach += 0.5;

        maxReach += 0.675;

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
