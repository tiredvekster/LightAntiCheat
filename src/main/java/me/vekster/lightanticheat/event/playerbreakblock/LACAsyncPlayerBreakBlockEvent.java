package me.vekster.lightanticheat.event.playerbreakblock;

import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.util.hook.server.folia.FoliaUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class LACAsyncPlayerBreakBlockEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private LACPlayer lacPlayer;
    private Block block;
    private String blockWorld;
    private int blockX;
    private int blockY;
    private int blockZ;
    private Material blockType;
    private Location location;
    private Location eyeLocation;

    public LACAsyncPlayerBreakBlockEvent(LACPlayerBreakBlockEvent event) {
        super(!FoliaUtil.isFolia());

        this.player = event.getPlayer();
        this.lacPlayer = event.getLacPlayer();
        this.block = event.getBlock();
        this.blockWorld = event.getBlock().getWorld().getName();
        this.blockX = event.getBlock().getX();
        this.blockY = event.getBlock().getY();
        this.blockZ = event.getBlock().getZ();
        this.blockType = event.getBlock().getType();
        this.location = event.getPlayer().getLocation().clone();
        this.eyeLocation = event.getPlayer().getLocation().clone();
    }

    public Player getPlayer() {
        return player;
    }

    public LACPlayer getLacPlayer() {
        return lacPlayer;
    }

    /**
     * @deprecated Unsafe on Folia async handlers. Use primitive snapshot getters instead.
     */
    @Deprecated
    public Block getBlock() {
        return block;
    }

    public String getBlockWorld() {
        return blockWorld;
    }

    public int getBlockX() {
        return blockX;
    }

    public int getBlockY() {
        return blockY;
    }

    public int getBlockZ() {
        return blockZ;
    }

    public Material getBlockType() {
        return blockType;
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
