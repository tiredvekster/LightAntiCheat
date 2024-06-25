package me.vekster.lightanticheat.check.checks.interaction.blockbreak;

import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.buffer.Buffer;
import me.vekster.lightanticheat.check.checks.interaction.InteractionCheck;
import me.vekster.lightanticheat.event.playerbreakblock.LACAsyncPlayerBreakBlockEvent;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.player.cache.PlayerCache;
import me.vekster.lightanticheat.player.cache.history.HistoryElement;
import me.vekster.lightanticheat.util.hook.simplehook.AureliumSkillsHook;
import me.vekster.lightanticheat.util.hook.simplehook.EnchantsSquaredHook;
import me.vekster.lightanticheat.util.hook.simplehook.McMMOHook;
import me.vekster.lightanticheat.util.hook.simplehook.VeinMinerHook;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Horizontal block break reach
 */
public class BlockBreakB extends InteractionCheck implements Listener {
    public BlockBreakB() {
        super(CheckName.BLOCKBREAK_B);
    }

    @EventHandler
    public void onAsyncBlockBreak(LACAsyncPlayerBreakBlockEvent event) {
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

        Scheduler.runTask(true, () -> {
            if (AureliumSkillsHook.isPrevented(player) ||
                    VeinMinerHook.isPrevented(player) ||
                    McMMOHook.isPrevented(block.getType()))
                return;

            if (EnchantsSquaredHook.hasEnchantment(player, "Excavation", "Deforestation", "Harvesting"))
                return;

            callViolationEvent(player, lacPlayer, null);
        });
    }

}
