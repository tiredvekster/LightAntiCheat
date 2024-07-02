package me.vekster.lightanticheat.event.playerbreakblock;

import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.util.hook.server.folia.FoliaUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class LACAsyncPlayerBreakBlockEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private LACPlayer lacPlayer;
    private Block block;
    private Location location;
    private Location eyeLocation;

    public LACAsyncPlayerBreakBlockEvent(LACPlayerBreakBlockEvent event) {
        super(!FoliaUtil.isFolia());

        this.player = event.getPlayer();
        this.lacPlayer = event.getLacPlayer();
        this.block = event.getBlock();
        this.location = event.getPlayer().getLocation().clone();
        this.eyeLocation = event.getPlayer().getLocation().clone();
    }

    public Player getPlayer() {
        return player;
    }

    public LACPlayer getLacPlayer() {
        return lacPlayer;
    }

    public Block getBlock() {
        return block;
    }

    public Location getLocation() {
        return location.clone();
    }

    public Location getEyeLocation() {
        return eyeLocation.clone();
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
