package me.vekster.lightanticheat.event.playermove;

import me.vekster.lightanticheat.player.LACPlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerMoveEvent;

public class LACPlayerMoveEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final PlayerMoveEvent event;
    private final Player player;
    private final LACPlayer lacPlayer;
    private final Location to;
    private final Location from;
    private final Boolean isPlayerFlying;
    private final Boolean isPlayerInsideVehicle;
    private final Boolean isPlayerGliding;
    private final Boolean isPlayerRiptiding;

    public LACPlayerMoveEvent(PlayerMoveEvent event, Player player,
                              LACPlayer lacPlayer, Location from, Location to) {
        this.event = event;
        this.player = player;
        this.lacPlayer = lacPlayer;
        this.from = from;
        this.to = to;
        this.isPlayerFlying = player.isFlying();
        this.isPlayerInsideVehicle = player.isInsideVehicle();
        this.isPlayerGliding = lacPlayer.isGliding();
        this.isPlayerRiptiding = lacPlayer.isRiptiding();
    }

    public PlayerMoveEvent getEvent() {
        return event;
    }

    public Player getPlayer() {
        return player;
    }

    public LACPlayer getLacPlayer() {
        return lacPlayer;
    }

    public Location getFrom() {
        return from;
    }

    public Location getTo() {
        return to;
    }

    public boolean isPlayerFlying() {
        return isPlayerFlying;
    }

    public boolean isPlayerInsideVehicle() {
        return isPlayerInsideVehicle;
    }

    public boolean isPlayerGliding() {
        return isPlayerGliding;
    }

    public boolean isPlayerRiptiding() {
        return isPlayerRiptiding;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
