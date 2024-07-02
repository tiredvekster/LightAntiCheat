package me.vekster.lightanticheat.event.packetrecive;

import me.vekster.lightanticheat.event.packetrecive.packettype.PacketType;
import me.vekster.lightanticheat.event.packetrecive.packettype.PacketTypeRecognizer;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.util.hook.server.folia.FoliaUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class LACAsyncPacketReceiveEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final LACPlayer lacPlayer;
    private final PacketType packetType;
    private final int entityId;

    public LACAsyncPacketReceiveEvent(Player player, LACPlayer lacPlayer, Object nmsPacket) {
        super(!FoliaUtil.isFolia());

        this.player = player;
        this.lacPlayer = lacPlayer;
        this.packetType = PacketTypeRecognizer.getPacketType(nmsPacket);
        this.entityId = PacketTypeRecognizer.getEntityId(nmsPacket);
    }

    public Player getPlayer() {
        return player;
    }

    public LACPlayer getLacPlayer() {
        return lacPlayer;
    }

    public PacketType getPacketType() {
        return packetType;
    }

    public int getEntityId() {
        return entityId;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
