package me.vekster.lightanticheat.check.checks.combat.autoclicker;

import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.buffer.Buffer;
import me.vekster.lightanticheat.check.checks.combat.CombatCheck;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.util.player.cps.CPSListener;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Max CPS
 */
public class AutoClickerB extends CombatCheck implements Listener {
    public AutoClickerB() {
        super(CheckName.AUTOCLICKER_B);
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

            if (CPSListener.getCurrentCps(player) < 47)
                return;

            Buffer buffer = getBuffer(player);
            long currentTime = System.currentTimeMillis();
            if (buffer.isExists("lastFlag") && currentTime - buffer.getLong("lastFlag") < 2000)
                return;
            buffer.put("lastFlag", currentTime);

            callViolationEvent(player, lacPlayer, event);
        });
    }

}
