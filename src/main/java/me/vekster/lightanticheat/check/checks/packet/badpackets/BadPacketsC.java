package me.vekster.lightanticheat.check.checks.packet.badpackets;

import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.checks.packet.PacketCheck;
import me.vekster.lightanticheat.event.packetrecive.LACAsyncPacketReceiveEvent;
import me.vekster.lightanticheat.event.packetrecive.packettype.PacketType;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.player.cache.history.HistoryElement;
import me.vekster.lightanticheat.player.cooldown.element.EntityDistance;
import me.vekster.lightanticheat.util.cooldown.CooldownUtil;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Impassible SteerVehicle packet
 */
public class BadPacketsC extends PacketCheck implements Listener {
    public BadPacketsC() {
        super(CheckName.BADPACKETS_C);
    }

    @EventHandler
    public void onAsyncPacketReceive(LACAsyncPacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.STEER_VEHICLE)
            return;

        Player player = event.getPlayer();
        LACPlayer lacPlayer = event.getLacPlayer();
        if (!isCheckAllowed(player, lacPlayer, true))
            return;

        if (System.currentTimeMillis() - lacPlayer.joinTime < 2000)
            return;

        if (player.isInsideVehicle() || System.currentTimeMillis() - lacPlayer.cache.lastInsideVehicle < 500)
            return;
        if (!CooldownUtil.getNearbyEntitiesAsync(lacPlayer.cooldown, event.getPlayer(), EntityDistance.NEARBY).isEmpty() ||
                distance(lacPlayer.cache.history.onEvent.location.get(HistoryElement.FIRST), lacPlayer.cache.history.onEvent.location.get(HistoryElement.FROM)) == 0)
            return;

        Scheduler.runTaskLaterAsynchronously(() -> {
            if (!player.isOnline() || lacPlayer.leaveTime != 0)
                return;
            if (player.isInsideVehicle() || System.currentTimeMillis() - lacPlayer.cache.lastInsideVehicle < 500)
                return;
            if (!CooldownUtil.getNearbyEntitiesAsync(lacPlayer.cooldown, event.getPlayer(), EntityDistance.NEARBY).isEmpty() ||
                    distance(lacPlayer.cache.history.onEvent.location.get(HistoryElement.FIRST), lacPlayer.cache.history.onEvent.location.get(HistoryElement.FROM)) == 0)
                return;

            Scheduler.runTaskLaterAsynchronously(() -> {
                if (!player.isOnline() || lacPlayer.leaveTime != 0)
                    return;
                if (player.isInsideVehicle() || System.currentTimeMillis() - lacPlayer.cache.lastInsideVehicle < 500)
                    return;
                if (!CooldownUtil.getNearbyEntitiesAsync(lacPlayer.cooldown, event.getPlayer(), EntityDistance.NEARBY).isEmpty() ||
                        distance(lacPlayer.cache.history.onEvent.location.get(HistoryElement.FIRST), lacPlayer.cache.history.onEvent.location.get(HistoryElement.FROM)) == 0)
                    return;

                Scheduler.runTask(false, () -> {
                    if (!player.isOnline() || lacPlayer.leaveTime != 0)
                        return;
                    flag(player, lacPlayer);
                });
            }, 1);
        }, 1);
    }

}
