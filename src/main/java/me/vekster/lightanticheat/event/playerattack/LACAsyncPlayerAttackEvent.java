package me.vekster.lightanticheat.event.playerattack;

import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.util.hook.server.folia.FoliaUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class LACAsyncPlayerAttackEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final LACPlayer lacPlayer;
    private final int entityId;

    public LACAsyncPlayerAttackEvent(Player player, LACPlayer lacPlayer, int entityId) {
        super(!FoliaUtil.isFolia());

        this.player = player;
        this.lacPlayer = lacPlayer;
        this.entityId = entityId;
    }

    public Player getPlayer() {
        return player;
    }

    public LACPlayer getLacPlayer() {
        return lacPlayer;
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
