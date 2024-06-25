package me.vekster.lightanticheat.check.checks.combat.autoclicker;

import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.buffer.Buffer;
import me.vekster.lightanticheat.check.checks.combat.CombatCheck;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Consistent clicks
 */
public class AutoClickerA extends CombatCheck implements Listener {
    public AutoClickerA() {
        super(CheckName.AUTOCLICKER_A);
    }

    @EventHandler
    public void onHit(PlayerInteractEvent event) {
        if (isExternalNPC(event)) return;
        if (event.getAction() == Action.PHYSICAL)
            return;
        Player player = event.getPlayer();
        LACPlayer lacPlayer = LACPlayer.getLacPlayer(player);

        Scheduler.entityThread(player, () -> {
            if (!isCheckAllowed(player, lacPlayer))
                return;

            Buffer buffer = getBuffer(player);
            long currentTime = System.currentTimeMillis();
            if (!buffer.isExists("lastHit")) {
                buffer.put("lastHit", currentTime);
                return;
            }
            long interval = currentTime - buffer.getLong("lastHit");
            if (!buffer.isExists("lastInterval")) {
                buffer.put("lastHit", currentTime);
                buffer.put("lastInterval", interval);
                return;
            }

            long lastInterval = buffer.getLong("lastInterval");
            buffer.put("lastHit", currentTime);
            buffer.put("lastInterval", interval);

            long intervalDelta = Math.abs(interval - lastInterval);
            if (intervalDelta != 49 && intervalDelta != 50 && intervalDelta != 51) {
                buffer.put("flags", 0);
                return;
            }
            buffer.put("flags", buffer.getInt("flags") + 1);
            if (buffer.getInt("flags") <= 5)
                return;

            if (buffer.isExists("lastFlag") && currentTime - buffer.getLong("lastFlag") < 3000)
                return;
            buffer.put("lastFlag", currentTime);

            callViolationEvent(player, lacPlayer, event);
        });
    }

}
