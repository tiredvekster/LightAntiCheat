package me.vekster.lightanticheat.event.playerplaceblock;

import me.vekster.lightanticheat.player.LACPlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockPlaceEvent;

public class LACPlayerPlaceBlockEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private BlockPlaceEvent event;
    private Player player;
    private LACPlayer lacPlayer;
    private Block block;
    private Block blockAgainst;
    private BlockState blockReplacedState;

    public LACPlayerPlaceBlockEvent(BlockPlaceEvent event, Player player, LACPlayer lacPlayer,
                                    Block block, Block blockAgainst, BlockState blockReplacedState) {
        this.event = event;
        this.player = player;
        this.lacPlayer = lacPlayer;
        this.block = block;
        this.blockAgainst = blockAgainst;
        this.blockReplacedState = blockReplacedState;
    }

    public BlockPlaceEvent getEvent() {
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

    public Block getBlockAgainst() {
        return blockAgainst;
    }

    public BlockState getBlockReplacedState() {
        return blockReplacedState;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
