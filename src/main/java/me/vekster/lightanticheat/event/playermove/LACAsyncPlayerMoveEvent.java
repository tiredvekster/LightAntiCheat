package me.vekster.lightanticheat.event.playermove;

import me.vekster.lightanticheat.event.playermove.blockcache.BlockCache;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.util.hook.server.folia.FoliaUtil;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Set;

public class LACAsyncPlayerMoveEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private final Player player;
    private final LACPlayer lacPlayer;
    private final Location to;
    private final Location from;
    private final Boolean isPlayerClimbing;
    private final Boolean isPlayerInWater;
    private final Boolean isPlayerFlying;
    private final Boolean isPlayerInsideVehicle;
    private final Boolean isPlayerGliding;
    private final Boolean isPlayerRiptiding;
    private final BlockCache fromBlockCache;
    private final BlockCache toBlockCache;

    public LACAsyncPlayerMoveEvent(LACPlayerMoveEvent event) {
        super(!FoliaUtil.isFolia());

        this.player = event.getPlayer();
        this.lacPlayer = event.getLacPlayer();
        this.from = event.getFrom();
        this.to = event.getTo();
        this.isPlayerFlying = event.isPlayerFlying();
        this.isPlayerInsideVehicle = event.isPlayerInsideVehicle();
        this.isPlayerGliding = event.isPlayerGliding();
        this.isPlayerRiptiding = event.isPlayerRiptiding();
        this.fromBlockCache = lacPlayer.cache.fromBlockCache;
        if (FoliaUtil.isStable(event.getPlayer())) {
            this.toBlockCache = new BlockCache(player, to);
            isPlayerClimbing = lacPlayer.isClimbing();
            isPlayerInWater = lacPlayer.isInWater();
        } else {
            toBlockCache = fromBlockCache;
            isPlayerClimbing = false;
            isPlayerInWater = false;
        }
        lacPlayer.cache.fromBlockCache = this.toBlockCache;
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

    public boolean isPlayerClimbing() {
        return isPlayerClimbing;
    }

    public boolean isPlayerInWater() {
        return isPlayerInWater;
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

    public Set<Block> getFromWithinBlocks() {
        return fromBlockCache.withinBlocks;
    }

    public Set<Material> getFromWithinMaterials() {
        return fromBlockCache.withinMaterials;
    }

    public Boolean isFromWithinBlocksPassable() {
        return fromBlockCache.withinBlocksPassable;
    }

    public Set<Block> getFromDownBlocks() {
        return fromBlockCache.downBlocks;
    }

    public Set<Material> getFromDownMaterials() {
        return fromBlockCache.downMaterials;
    }

    public Boolean isFromDownBlocksPassable() {
        return fromBlockCache.downBlocksPassable;
    }

    public Set<Block> getToWithinBlocks() {
        return toBlockCache.withinBlocks;
    }

    public Set<Material> getToWithinMaterials() {
        return toBlockCache.withinMaterials;
    }

    public Boolean isToWithinBlocksPassable() {
        return toBlockCache.withinBlocksPassable;
    }

    public Set<Block> getToDownBlocks() {
        return toBlockCache.downBlocks;
    }

    public Set<Material> getToDownMaterials() {
        return toBlockCache.downMaterials;
    }

    public Boolean isToDownBlocksPassable() {
        return toBlockCache.downBlocksPassable;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
        if (cancelled) {
            Scheduler.runTask(true, () -> {
                if (cancelled) {
                    FoliaUtil.teleportPlayer(getPlayer(), getFrom());
                }
            });
        }
    }
}
