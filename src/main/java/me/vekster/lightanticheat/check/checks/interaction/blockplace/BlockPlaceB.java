package me.vekster.lightanticheat.check.checks.interaction.blockplace;

import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.buffer.Buffer;
import me.vekster.lightanticheat.check.checks.interaction.InteractionCheck;
import me.vekster.lightanticheat.event.playerplaceblock.LACAsyncPlayerPlaceBlockEvent;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.player.cache.PlayerCache;
import me.vekster.lightanticheat.player.cache.history.HistoryElement;
import me.vekster.lightanticheat.util.hook.plugin.simplehook.EnchantsSquaredHook;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Horizontal block place reach
 */
public class BlockPlaceB extends InteractionCheck implements Listener {
    public BlockPlaceB() {
        super(CheckName.BLOCKPLACE_B);
    }

    @EventHandler
    public void onAsyncBlockPlace(LACAsyncPlayerPlaceBlockEvent event) {
        Player player = event.getPlayer();
        LACPlayer lacPlayer = event.getLacPlayer();

        if (!isCheckAllowed(player, lacPlayer, true))
            return;

        Block block = event.getBlock();
        Location blockLocation = block.getLocation();
        double distance = distanceHorizontal(event.getEyeLocation(), blockLocation);
        distance -= 0.707107;

        double maxDistance = 6.0;

        PlayerCache cache = lacPlayer.cache;
        double eventBackwardsDistance = 0;
        if (distanceHorizontal(cache.history.onEvent.location.get(HistoryElement.FROM), blockLocation) <
                distanceHorizontal(event.getLocation(), blockLocation))
            eventBackwardsDistance = distanceHorizontal(cache.history.onEvent.location.get(HistoryElement.FROM), event.getLocation());
        double packetBackwardsDistance = 0;
        if (distanceHorizontal(cache.history.onPacket.location.get(HistoryElement.FROM), blockLocation) <
                distanceHorizontal(event.getLocation(), blockLocation))
            packetBackwardsDistance = distanceHorizontal(cache.history.onPacket.location.get(HistoryElement.FROM), event.getLocation());
        double backwardsDistance = Math.max(eventBackwardsDistance, packetBackwardsDistance);
        maxDistance += backwardsDistance;
        maxDistance += backwardsDistance * (lacPlayer.getPing() / 1000.0 * 20.0);
        maxDistance = Math.min(maxDistance, 8.5);

        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE)
            maxDistance += 1.5;

        if (distance < maxDistance)
            return;

        Buffer buffer = getBuffer(player, true);
        buffer.put("flags", buffer.getInt("flags") + 1);
        if (buffer.getInt("flags") <= 2)
            return;

        if (getAttribute(player, "PLAYER_BLOCK_INTERACTION_RANGE") != 0)
            buffer.put("attribute", System.currentTimeMillis());
        if (System.currentTimeMillis() - buffer.getLong("attribute") < 2500)
            return;

        Scheduler.runTask(true, () -> {
            if (EnchantsSquaredHook.hasEnchantment(player, "Illuminated", "Harvesting"))
                return;

            callViolationEvent(player, lacPlayer, null);
        });
    }

}
