package me.vekster.lightanticheat.api.event;

import me.vekster.lightanticheat.check.CheckSetting;
import me.vekster.lightanticheat.player.LACPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Nullable;

public class LACViolationEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private String checkName;
    private Player player;
    private final CheckSetting CHECK_SETTING;
    private final LACPlayer LAC_PLAYER;
    private final Cancellable CANCELLABLE;
    private boolean cancelled;

    public LACViolationEvent(CheckSetting checkSetting, Player player, LACPlayer lacPlayer, @Nullable Cancellable cancellable) {
        this.checkName = checkSetting.name.name().toLowerCase();
        this.player = player;
        this.CHECK_SETTING = checkSetting;
        this.LAC_PLAYER = lacPlayer;
        this.CANCELLABLE = cancellable;
    }


    public String getCheckName() {
        return checkName;
    }

    public Player getPlayer() {
        return player;
    }

    public CheckSetting getCheckSettings() {
        return CHECK_SETTING;
    }

    public LACPlayer getAcPlayer() {
        return LAC_PLAYER;
    }

    @Nullable
    public Cancellable getCancellable() {
        return CANCELLABLE;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
