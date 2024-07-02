package me.vekster.lightanticheat.check.checks.player.skinblinker;

import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.buffer.Buffer;
import me.vekster.lightanticheat.check.checks.player.PlayerCheck;
import me.vekster.lightanticheat.event.packetrecive.LACAsyncPacketReceiveEvent;
import me.vekster.lightanticheat.event.packetrecive.packettype.PacketType;
import me.vekster.lightanticheat.event.playermove.LACAsyncPlayerMoveEvent;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.util.hook.plugin.FloodgateHook;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * SkinBlinker hack
 */
public class SkinBlinkerA extends PlayerCheck implements Listener {
    public SkinBlinkerA() {
        super(CheckName.SKINBLINKER_A);
    }

    @EventHandler
    public void onAsyncPacketReceive(LACAsyncPacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.CLIENT_INFORMATION)
            return;

        LACPlayer lacPlayer = event.getLacPlayer();
        Player player = event.getPlayer();
        Buffer buffer = getBuffer(player, true);

        if (!isCheckAllowed(player, lacPlayer, true))
            return;

        if (FloodgateHook.isBedrockPlayer(player, true))
            return;

        if (System.currentTimeMillis() - buffer.getLong("lastMovement") < 333)
            buffer.put("packets", buffer.getInt("packets") + 1);

        long currentTime = System.currentTimeMillis();
        if (currentTime - buffer.getLong("startTime") <= 2000)
            return;
        buffer.put("startTime", currentTime);

        int packets = buffer.getInt("packets");
        buffer.put("packets", 0);

        if (packets < 12) {
            buffer.put("flags", 0);
            return;
        }
        buffer.put("flags", buffer.getInt("flags") + 1);

        if (buffer.getInt("flags") < 2)
            return;
        buffer.put("flags", 0);

        if (System.currentTimeMillis() - buffer.getLong("lastMovement") >= 1800)
            return;

        Scheduler.runTask(true, () -> {
            callViolationEvent(player, lacPlayer, null);
        });
    }

    @EventHandler
    public void onMovement(LACAsyncPlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();

        if (Math.abs(from.getYaw()) - to.getYaw() <= 5 &&
                Math.abs(from.getPitch()) - to.getPitch() <= 0.5)
            return;

        if (distance(event.getFrom(), event.getTo()) == 0)
            return;

        Buffer buffer = getBuffer(event.getPlayer(), true);
        buffer.put("lastMovement", System.currentTimeMillis());
    }

}
