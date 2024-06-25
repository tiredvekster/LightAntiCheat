package me.vekster.lightanticheat.event.playerbreakblock;

import me.vekster.lightanticheat.player.LACPlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;

public class LACPlayerBreakBlockEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private BlockBreakEvent event;
    private Player player;
    private LACPlayer lacPlayer;
    private Block block;

    public LACPlayerBreakBlockEvent(BlockBreakEvent event, Player player, LACPlayer lacPlayer, Block block) {
        this.event = event;
        this.player = player;
        this.lacPlayer = lacPlayer;
        this.block = block;
    }

    public BlockBreakEvent getEvent() {
        return event;
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

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
