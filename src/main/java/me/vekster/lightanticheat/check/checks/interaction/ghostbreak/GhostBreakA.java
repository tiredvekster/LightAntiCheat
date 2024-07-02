package me.vekster.lightanticheat.check.checks.interaction.ghostbreak;

import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.buffer.Buffer;
import me.vekster.lightanticheat.check.checks.interaction.InteractionCheck;
import me.vekster.lightanticheat.event.playerbreakblock.LACPlayerBreakBlockEvent;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.util.hook.plugin.simplehook.EnchantsSquaredHook;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Set;

/**
 * Breaking through blocks
 */
public class GhostBreakA extends InteractionCheck implements Listener {
    public GhostBreakA() {
        super(CheckName.GHOSTBREAK_A);
    }

    private static final Set<BlockFace> BLOCK_FACES = new HashSet<>();

    static {
        BLOCK_FACES.add(BlockFace.UP);
        BLOCK_FACES.add(BlockFace.DOWN);
        BLOCK_FACES.add(BlockFace.NORTH);
        BLOCK_FACES.add(BlockFace.SOUTH);
        BLOCK_FACES.add(BlockFace.WEST);
        BLOCK_FACES.add(BlockFace.EAST);
    }

    @EventHandler
    public void onBlockBreak(LACPlayerBreakBlockEvent event) {
        Player player = event.getPlayer();
        LACPlayer lacPlayer = event.getLacPlayer();

        if (!isCheckAllowed(player, lacPlayer))
            return;

        Block block = event.getBlock();
        Block secondBlock = null;

        for (BlockFace blockFace : BLOCK_FACES) {
            Block nearbyBlock = block.getRelative(blockFace);
            if (nearbyBlock.getType() == block.getType()) {
                if (secondBlock == null) {
                    secondBlock = nearbyBlock;
                    continue;
                } else {
                    return;
                }
            }
            if (!isOccluding(nearbyBlock))
                return;
        }

        if (secondBlock != null) {
            for (BlockFace blockFace : BLOCK_FACES) {
                Block nearbyBlock = secondBlock.getRelative(blockFace);
                if (nearbyBlock.getLocation().equals(block.getLocation()))
                    continue;
                if (!isOccluding(nearbyBlock))
                    return;
            }
        }

        for (BlockFace blockFace : BLOCK_FACES) {
            Block nearbyBlock = block.getRelative(blockFace);
            lacPlayer.sendBlockDate(nearbyBlock.getLocation(), nearbyBlock);
        }

        if (lacPlayer.getPing() > 400) {
            Buffer buffer = getBuffer(player);
            buffer.put("flags", buffer.getInt("flags") + 1);
            if (buffer.getInt("flags") <= 1)
                return;
        }

        if (EnchantsSquaredHook.hasEnchantment(player, "Excavation", "Deforestation", "Harvesting"))
            return;

        callViolationEvent(player, lacPlayer, event.getEvent());
    }

    private static boolean isOccluding(Block block) {
        Material type = block.getType();
        if (type.isOccluding())
            return true;
        String name = type.name();
        if (name.equalsIgnoreCase("GLASS") || name.endsWith("_GLASS"))
            return true;
        return false;
    }

}
