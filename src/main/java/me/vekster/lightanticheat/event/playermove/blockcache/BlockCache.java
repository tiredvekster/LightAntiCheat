package me.vekster.lightanticheat.event.playermove.blockcache;

import me.vekster.lightanticheat.util.detection.CheckUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class BlockCache {

    public BlockCache(Player player, Location location) {
        this.withinBlocks = CheckUtil.getWithinBlocks(player, location);
        this.withinMaterials = new HashSet<>();
        boolean toWithinBlocksPassable = true;
        for (Block block : this.withinBlocks) {
            this.withinMaterials.add(block.getType());
            if (toWithinBlocksPassable && !CheckUtil.isActuallyPassable(block))
                toWithinBlocksPassable = false;
        }
        this.withinBlocksPassable = toWithinBlocksPassable;

        this.downBlocks = CheckUtil.getDownBlocks(player, location, 0.21);
        this.downMaterials = new HashSet<>();
        boolean toDownBlocksPassable = true;
        for (Block block : this.downBlocks) {
            this.downMaterials.add(block.getType());
            if (toDownBlocksPassable && !CheckUtil.isActuallyPassable(block))
                toDownBlocksPassable = false;
        }
        this.downBlocksPassable = toDownBlocksPassable;
    }

    public final Set<Block> withinBlocks;
    public Set<Material> withinMaterials;
    public Boolean withinBlocksPassable;
    public Set<Block> downBlocks;
    public Set<Material> downMaterials;
    public Boolean downBlocksPassable;

}
