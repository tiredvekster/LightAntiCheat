package me.vekster.lightanticheat.event.playerattack;

import me.vekster.lightanticheat.player.LACPlayer;
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
        this.isEntityAttackCause = isEntityAttackCause(event.getCause());
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

    private static boolean isEntityAttackCause(EntityDamageEvent.DamageCause cause) {
        String causeName = cause.name();
        return causeName.equals("ENTITY_ATTACK") || causeName.equals("ENTITY_SWEEP_ATTACK");
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
