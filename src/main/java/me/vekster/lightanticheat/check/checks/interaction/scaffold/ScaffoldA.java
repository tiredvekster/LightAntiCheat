package me.vekster.lightanticheat.check.checks.interaction.scaffold;

import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.buffer.Buffer;
import me.vekster.lightanticheat.check.checks.interaction.InteractionCheck;
import me.vekster.lightanticheat.event.playerplaceblock.LACAsyncPlayerPlaceBlockEvent;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.player.cache.PlayerCache;
import me.vekster.lightanticheat.player.cache.history.HistoryElement;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import me.vekster.lightanticheat.version.VerUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;


/**
 * Head rotation
 */
public class ScaffoldA extends InteractionCheck implements Listener {
    public ScaffoldA() {
        super(CheckName.SCAFFOLD_A);
    }

    @EventHandler
    public void onAsyncBlockPlace(LACAsyncPlayerPlaceBlockEvent event) {
        Player player = event.getPlayer();
        LACPlayer lacPlayer = event.getLacPlayer();
        PlayerCache cache = lacPlayer.cache;

        if (!isCheckAllowed(player, lacPlayer, true))
            return;

        if (!isScaffoldPlacement(player, event.getBlock(), event.getBlockAgainst()))
            return;

        for (Block withinBlock : getWithinBlocks(player)) {
            if (withinBlock.getType() != Material.AIR)
                return;
        }

        if (getEffectAmplifier(player, VerUtil.potions.get("LEVITATION")) > 0 ||
                getEffectAmplifier(player, PotionEffectType.SPEED) > 5)
            return;

        boolean flag = event.getLocation().getPitch() <= 34;

        if (!flag) {
            Location from = cache.history.onEvent.location.get(HistoryElement.FROM);
            Location first = cache.history.onEvent.location.get(HistoryElement.FIRST);
            if (rotation(from, event.getLocation()) && !rotation(from, first)) {
                flag = true;
            }
        }

        if (!flag)
            return;

        Buffer buffer = getBuffer(player, true);
        buffer.put("flags", buffer.getInt("flags") + 1);
        if (buffer.getInt("flags") <= 2)
            return;

        Scheduler.runTask(true, () -> {
            callViolationEventIfRepeat(player, lacPlayer, null, buffer, 1500);
        });
    }

    private static boolean rotation(Location from, Location to) {
        return from.getYaw() != to.getYaw() && from.getPitch() != to.getPitch();
    }

}
