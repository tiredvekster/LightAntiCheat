package me.vekster.lightanticheat.check.checks.combat.killaura;

import me.vekster.lightanticheat.Main;
import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.buffer.Buffer;
import me.vekster.lightanticheat.check.checks.combat.CombatCheck;
import me.vekster.lightanticheat.event.playerattack.LACAsyncPlayerAttackEvent;
import me.vekster.lightanticheat.event.playerattack.LACPlayerAttackEvent;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.util.hook.plugin.FloodgateHook;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * 1. Hitting more than one target per tick
 * 2. Hit while using a shield
 */
public class KillAuraD extends CombatCheck implements Listener {

    public KillAuraD() {
        super(CheckName.KILLAURA_D);
    }

    @EventHandler
    public void multiAuraAsync(LACAsyncPlayerAttackEvent event) {
        LACPlayer lacPlayer = event.getLacPlayer();
        Buffer buffer = getBuffer(event.getPlayer(), true);
        long currentTime = System.currentTimeMillis();

        if (currentTime - buffer.getLong("lastAsyncHit") > 35 - Math.min(lacPlayer.getPing() / 40, 10)) {
            buffer.put("lastAsyncHit", currentTime);
            return;
        }

        buffer.put("lastAsyncFlag", System.currentTimeMillis());
    }

    @EventHandler
    public void multiAura(LACPlayerAttackEvent event) {
        Player player = event.getPlayer();
        Buffer buffer = getBuffer(player, true);
        long currentTime = System.currentTimeMillis();

        if (currentTime - buffer.getLong("lastAsyncFlag") > Main.getBufferDurationMils() - 1000)
            return;

        LACPlayer lacPlayer = event.getLacPlayer();
        if (currentTime - buffer.getLong("lastHit") > 35 - Math.min(lacPlayer.getPing() / 40, 10)) {
            buffer.put("lastHit", currentTime);
            return;
        }

        if (!isCheckAllowed(player, lacPlayer))
            return;

        if (FloodgateHook.isProbablyPocketEditionPlayer(player))
            return;

        if (!(event.getEntity() instanceof LivingEntity))
            return;

        if (currentTime - buffer.getLong("lastFlag") <= 750) return;
        else buffer.put("lastFlag", currentTime);

        if (getItemStackAttributes(player, "PLAYER_SWEEPING_DAMAGE_RATIO") != 0 ||
                getPlayerAttributes(player).getOrDefault("PLAYER_SWEEPING_DAMAGE_RATIO", 0.0) > 0.01)
            buffer.put("attribute", System.currentTimeMillis());
        if (System.currentTimeMillis() - buffer.getLong("attribute") < 2000)
            return;

        Scheduler.runTask(true, () -> {
            callViolationEventIfRepeat(player, lacPlayer, null, buffer, 5000);
        });
    }

    @EventHandler
    public void shieldAsync(LACAsyncPlayerAttackEvent event) {
        Player player = event.getPlayer();
        if (!player.isBlocking() && !player.isSleeping() && !player.isDead())
            return;

        Buffer buffer = getBuffer(player, true);
        buffer.put("lastShieldAsyncFlag", System.currentTimeMillis());
    }

    @EventHandler
    public void shield(LACPlayerAttackEvent event) {
        Player player = event.getPlayer();
        Buffer buffer = getBuffer(player, true);
        if (System.currentTimeMillis() - buffer.getLong("lastShieldAsyncFlag") > Main.getBufferDurationMils() - 1000)
            return;

        LACPlayer lacPlayer = event.getLacPlayer();
        if (!isCheckAllowed(player, lacPlayer, true))
            return;

        if (!player.isBlocking() && !player.isSleeping() && !player.isDead())
            return;

        if (player.isBlocking() && lacPlayer.cache.blockingTicks < 2)
            return;

        Scheduler.runTask(true, () -> {
            callViolationEventIfRepeat(player, lacPlayer, null, getBuffer(player, true),
                    Main.getBufferDurationMils() - 1000);
        });
    }

}
