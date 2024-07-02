package me.vekster.lightanticheat.check.checks.interaction.airplace;

import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.buffer.Buffer;
import me.vekster.lightanticheat.check.checks.interaction.InteractionCheck;
import me.vekster.lightanticheat.event.playerplaceblock.LACPlayerPlaceBlockEvent;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.util.hook.plugin.simplehook.EnchantsSquaredHook;
import me.vekster.lightanticheat.version.VerUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Set;

/**
 * AirPlace/LiquidPlace hack
 */
public class AirPlaceA extends InteractionCheck implements Listener {
    public AirPlaceA() {
        super(CheckName.AIRPLACE_A);
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

    @EventHandler(priority = EventPriority.LOW)
    public void beforeBlockPlace(LACPlayerPlaceBlockEvent event) {
        Material type = event.getBlockAgainst().getType();
        if (type != Material.AIR && type != Material.WATER && type != Material.LAVA)
            return;
        Player player = event.getPlayer();
        if (!isCheckAllowed(player, event.getLacPlayer()))
            return;

        Buffer buffer = getBuffer(player);
        if (System.currentTimeMillis() - buffer.getLong("lastBlockUpdate") < 5000)
            return;
        buffer.put("lastBlockUpdate", System.currentTimeMillis());

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    Block relativeBlock = event.getBlockAgainst().getRelative(x, y, z);
                    event.getLacPlayer().sendBlockDate(relativeBlock.getLocation(), relativeBlock);
                }
            }
        }
    }

    @EventHandler
    public void onBlockPlace(LACPlayerPlaceBlockEvent event) {
        Player player = event.getPlayer();
        LACPlayer lacPlayer = event.getLacPlayer();

        if (!isCheckAllowed(player, lacPlayer))
            return;

        Material replacedBlockType = event.getBlockReplacedState().getType();
        if (replacedBlockType != Material.AIR &&
                replacedBlockType != Material.WATER && replacedBlockType != Material.LAVA) {
            return;
        }

        Block block = event.getBlock();

        for (BlockFace blockFace : BLOCK_FACES) {
            Block relativeBlock = block.getRelative(blockFace);
            if (relativeBlock.getType() != Material.AIR &&
                    relativeBlock.getType() != Material.WATER && relativeBlock.getType() != Material.LAVA)
                return;
        }

        if (block.getType() == VerUtil.material.get("LILY_PAD") || block.getType().name().contains("COPPER"))
            return;

        Buffer buffer = getBuffer(player);
        if (System.currentTimeMillis() - buffer.getLong("lastBlockUpdate") < 200)
            return;

        if (lacPlayer.getPing() > 400) {
            buffer.put("flags", buffer.getInt("flags") + 1);
            if (buffer.getInt("flags") <= 1)
                return;
        }

        if (EnchantsSquaredHook.hasEnchantment(player, "Illuminated", "Harvesting"))
            return;

        callViolationEvent(player, lacPlayer, event.getEvent());
    }

}
