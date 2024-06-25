package me.vekster.lightanticheat.check.checks.packet.timer;

import me.vekster.lightanticheat.Main;
import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.buffer.Buffer;
import me.vekster.lightanticheat.check.checks.packet.PacketCheck;
import me.vekster.lightanticheat.event.playermove.LACAsyncPlayerMoveEvent;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Event counter
 */
public class TimerB extends PacketCheck implements Listener {
    public TimerB() {
        super(CheckName.TIMER_B);
    }

    //      Long interval:

    @EventHandler
    public void onAsyncMovementA(LACAsyncPlayerMoveEvent event) {
        LACPlayer lacPlayer = event.getLacPlayer();
        Player player = event.getPlayer();
        Buffer buffer = getBuffer(player, true);

        if (!isCheckAllowed(player, lacPlayer, true))
            return;

        if (!limitPackets('A', buffer, 1600L, (int) (20 * 1.6 * 5.5), 3))
            return;

        if (System.currentTimeMillis() - buffer.getLong("lastFlagTime") < 1500)
            return;
        buffer.put("lastFlagTime", System.currentTimeMillis());

        Scheduler.runTask(true, () -> {
            callViolationEvent(player, lacPlayer, null);
        });
    }

    @EventHandler
    public void onAsyncMovementB(LACAsyncPlayerMoveEvent event) {
        LACPlayer lacPlayer = event.getLacPlayer();
        Player player = event.getPlayer();
        Buffer buffer = getBuffer(player, true);

        if (!isCheckAllowed(player, lacPlayer, true))
            return;

        if (!limitPackets('B', buffer, 1200L, (int) (20 * 1.2 * 5.0), 4))
            return;

        if (System.currentTimeMillis() - buffer.getLong("lastFlagTime") < 1500)
            return;
        buffer.put("lastFlagTime", System.currentTimeMillis());

        Scheduler.runTask(true, () -> {
            callViolationEvent(player, lacPlayer, null);
        });
    }

    @EventHandler
    public void onAsyncMovementC(LACAsyncPlayerMoveEvent event) {
        LACPlayer lacPlayer = event.getLacPlayer();
        Player player = event.getPlayer();
        Buffer buffer = getBuffer(player, true);

        if (!isCheckAllowed(player, lacPlayer, true))
            return;

        if (!limitPackets('C', buffer, 800L, (int) (20 * 0.8 * 4.5), 5))
            return;

        if (System.currentTimeMillis() - buffer.getLong("lastFlagTime") < 1500)
            return;
        buffer.put("lastFlagTime", System.currentTimeMillis());

        Scheduler.runTask(true, () -> {
            callViolationEvent(player, lacPlayer, null);
        });
    }

    @EventHandler
    public void onAsyncMovementD(LACAsyncPlayerMoveEvent event) {
        LACPlayer lacPlayer = event.getLacPlayer();
        Player player = event.getPlayer();
        Buffer buffer = getBuffer(player, true);

        if (!isCheckAllowed(player, lacPlayer, true))
            return;

        if (!limitPackets('D', buffer, 571L, (int) (20 * 0.571 * 4.1), 7))
            return;

        if (System.currentTimeMillis() - buffer.getLong("lastFlagTime") < 1500)
            return;
        buffer.put("lastFlagTime", System.currentTimeMillis());

        Scheduler.runTask(true, () -> {
            callViolationEvent(player, lacPlayer, null);
        });
    }

    //      Short interval:

    @EventHandler
    public void onAsyncMovementE(LACAsyncPlayerMoveEvent event) {
        LACPlayer lacPlayer = event.getLacPlayer();
        Player player = event.getPlayer();
        Buffer buffer = getBuffer(player, true);

        if (!isCheckAllowed(player, lacPlayer, true))
            return;

        if (!limitPackets('E', buffer, 960L, (int) (20 * 1.6 * 5.5), 3))
            return;

        if (System.currentTimeMillis() - buffer.getLong("lastFlagTime") < 900)
            return;
        buffer.put("lastFlagTime", System.currentTimeMillis());

        Scheduler.runTask(true, () -> {
            callViolationEventIfRepeat(player, lacPlayer, null, buffer, Main.getBufferDurationMils() - 1000L);
        });
    }

    @EventHandler
    public void onAsyncMovementF(LACAsyncPlayerMoveEvent event) {
        LACPlayer lacPlayer = event.getLacPlayer();
        Player player = event.getPlayer();
        Buffer buffer = getBuffer(player, true);

        if (!isCheckAllowed(player, lacPlayer, true))
            return;

        if (!limitPackets('F', buffer, 480L, (int) (20 * 0.8 * 4.5), 5))
            return;

        if (System.currentTimeMillis() - buffer.getLong("lastFlagTime") < 900)
            return;
        buffer.put("lastFlagTime", System.currentTimeMillis());

        Scheduler.runTask(true, () -> {
            callViolationEventIfRepeat(player, lacPlayer, null, buffer, Main.getBufferDurationMils() - 1000L);
        });
    }

}
