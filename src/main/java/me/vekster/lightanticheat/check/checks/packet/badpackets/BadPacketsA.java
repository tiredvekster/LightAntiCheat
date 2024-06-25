package me.vekster.lightanticheat.check.checks.packet.badpackets;

import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.checks.packet.PacketCheck;
import me.vekster.lightanticheat.event.packetrecive.LACAsyncPacketReceiveEvent;
import me.vekster.lightanticheat.event.packetrecive.packettype.PacketType;
import me.vekster.lightanticheat.player.LACPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Self-Damage
 */
public class BadPacketsA extends PacketCheck implements Listener {
    public BadPacketsA() {
        super(CheckName.BADPACKETS_A);
    }

    @EventHandler
    public void onAsyncPacketReceive(LACAsyncPacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.USE_ENTITY)
            return;

        Player player = event.getPlayer();
        LACPlayer lacPlayer = event.getLacPlayer();
        if (!isCheckAllowed(player, lacPlayer, true))
            return;

        if (event.getEntityId() < 0 || event.getEntityId() == player.getEntityId())
            flag(player, lacPlayer);
    }

}
