package me.vekster.lightanticheat.event.playerattack;

import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.util.detection.CheckUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class LACPlayerAttackEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private EntityDamageByEntityEvent event;
    private Player player;
    private LACPlayer lacPlayer;
    private Entity entity;
    private boolean isEntityAttackCause;

    public LACPlayerAttackEvent(EntityDamageByEntityEvent event, Player player, LACPlayer lacPlayer, Entity entity) {
        this.event = event;
        this.player = player;
        this.lacPlayer = lacPlayer;
        this.entity = entity;
        this.isEntityAttackCause = event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK;
    }

    public EntityDamageByEntityEvent getEvent() {
        return event;
    }

    public Player getPlayer() {
        return player;
    }

    public LACPlayer getLacPlayer() {
        return lacPlayer;
    }

    public Entity getEntity() {
        return entity;
    }

    public boolean isEntityAttackCause() {
        return isEntityAttackCause;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
