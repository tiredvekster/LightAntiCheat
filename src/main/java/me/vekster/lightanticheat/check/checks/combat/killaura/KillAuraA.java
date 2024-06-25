package me.vekster.lightanticheat.check.checks.combat.killaura;

import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.buffer.Buffer;
import me.vekster.lightanticheat.check.checks.combat.CombatCheck;
import me.vekster.lightanticheat.event.playerattack.LACAsyncPlayerAttackEvent;
import me.vekster.lightanticheat.event.playermove.LACAsyncPlayerMoveEvent;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.player.cache.PlayerCache;
import me.vekster.lightanticheat.player.cache.history.HistoryElement;
import me.vekster.lightanticheat.player.cache.history.PlayerCacheHistory;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Head rotation pattern
 */
public class KillAuraA extends CombatCheck implements Listener {

    public KillAuraA() {
        super(CheckName.KILLAURA_A);
    }

    @EventHandler
    public void onAsyncHit(LACAsyncPlayerAttackEvent event) {
        LACPlayer lacPlayer = event.getLacPlayer();
        PlayerCache cache = lacPlayer.cache;
        Player player = event.getPlayer();
        Buffer buffer = getBuffer(player, true);

        if (!isCheckAllowed(player, lacPlayer, true)) {
            buffer.put("listen", 0L);
            return;
        }

        PlayerCacheHistory<Location> history = cache.history.onEvent.location;
        if (getHeadRotationChange(history.get(HistoryElement.SECOND), history.get(HistoryElement.FIRST)) >= 0.01 ||
                getHeadRotationChange(history.get(HistoryElement.FIRST), history.get(HistoryElement.FROM)) >= 0.01) {
            buffer.put("listen", 0L);
            return;
        }

        buffer.put("listen", System.currentTimeMillis());
    }

    @EventHandler
    public void onAsyncMovement(LACAsyncPlayerMoveEvent event) {
        Player player = event.getPlayer();
        Buffer buffer = getBuffer(player, true);
        if (System.currentTimeMillis() - buffer.getLong("listen") > 5000)
            return;
        buffer.put("listen", 0L);

        if (getHeadRotationChange(event.getFrom(), event.getTo()) <= 15) {
            buffer.put("flags", 0);
            return;
        }

        buffer.put("flags", buffer.getInt("flags") + 1);
        if (buffer.getInt("flags") <= 3)
            return;

        Scheduler.runTask(true, () -> {
            callViolationEvent(player, event.getLacPlayer(), null);
        });
    }

    private static double getHeadRotationChange(Location from, Location to) {
        return Math.sqrt(Math.pow(to.getYaw() - from.getYaw(), 2) + Math.pow(to.getPitch() - from.getPitch(), 2));
    }

}
