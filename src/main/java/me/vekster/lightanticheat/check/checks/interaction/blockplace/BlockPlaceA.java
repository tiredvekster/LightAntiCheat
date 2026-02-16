package me.vekster.lightanticheat.check.checks.interaction.blockplace;

import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.buffer.Buffer;
import me.vekster.lightanticheat.check.checks.interaction.InteractionCheck;
import me.vekster.lightanticheat.event.playerplaceblock.LACAsyncPlayerPlaceBlockEvent;
import me.vekster.lightanticheat.event.playerplaceblock.LACPlayerPlaceBlockEvent;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.player.cache.history.HistoryElement;
import me.vekster.lightanticheat.player.cache.history.PlayerCacheHistory;
import me.vekster.lightanticheat.util.hook.plugin.simplehook.AureliumSkillsHook;
import me.vekster.lightanticheat.util.hook.plugin.simplehook.EnchantsSquaredHook;
import me.vekster.lightanticheat.util.hook.plugin.simplehook.McMMOHook;
import me.vekster.lightanticheat.util.hook.plugin.simplehook.VeinMinerHook;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Head rotation
 */
public class BlockPlaceA extends InteractionCheck implements Listener {
    public BlockPlaceA() {
        super(CheckName.BLOCKPLACE_A);
    }

    @EventHandler
    public void onAsyncBlockBreak(LACAsyncPlayerPlaceBlockEvent event) {
        Buffer buffer = getBuffer(event.getPlayer(), true);
        if (!flagSafe(event.getPlayer(), event.getLacPlayer(), event.getBlockWorld(), true)) {
            buffer.put("lastAsyncResult", false);
            return;
        }
        buffer.put("lastAsyncResult", true);
    }

    @EventHandler
    public void onBlockBreak(LACPlayerPlaceBlockEvent event) {
        Buffer buffer = getBuffer(event.getPlayer(), true);
        if (!buffer.getBoolean("lastAsyncResult"))
            return;
        Player player = event.getPlayer();
        LACPlayer lacPlayer = event.getLacPlayer();
        Block block = event.getBlock();
        Location eyeLocation = player.getEyeLocation();

        if (!flagSafe(player, lacPlayer, block.getWorld().getName(), false))
            return;

        boolean forceFlag = shouldForceFlagByAngle(block.getX(), block.getZ(), eyeLocation);
        boolean raytraceFlag = !forceFlag && executeRaytrace(player, block.getWorld().getName(), block.getX(), block.getZ(), block.getType(), buffer);
        if (!(forceFlag || raytraceFlag))
            return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - buffer.getLong("lastFlag") < 550)
            return;
        buffer.put("lastFlag", currentTime);

        buffer.put("flags", buffer.getInt("flags") + 1);
        if (buffer.getInt("flags") <= 1)
            return;

        Scheduler.runTaskLater(player, () -> {
            if (getYawChange(player.getEyeLocation(), lacPlayer) > 35.0)
                return;

            if (AureliumSkillsHook.isPrevented(player) ||
                    VeinMinerHook.isPrevented(player) ||
                    McMMOHook.isPrevented(block.getType()))
                return;

            if (EnchantsSquaredHook.hasEnchantment(player, "Illuminated", "Harvesting"))
                return;

            callViolationEvent(player, lacPlayer, null);
        }, 1);
    }

    private boolean flagSafe(Player player, LACPlayer lacPlayer, String blockWorld, boolean async) {
        if (!isCheckAllowed(player, lacPlayer, async))
            return false;
        return player.getWorld().getName().equals(blockWorld);
    }

    private boolean shouldForceFlagByAngle(int blockX, int blockZ, Location eyeLocation) {
        Vector vector = new Vector(blockX + 0.5D, 0.0D, blockZ + 0.5D)
                .subtract(eyeLocation.toVector().setY(0.0D));
        float angle = eyeLocation.getDirection().setY(0.0D).angle(vector) * 57.2958F;
        return angle > 110 && eyeLocation.getPitch() < 60 && eyeLocation.getPitch() > -40;
    }

    private boolean flagRaytrace(Player player, String blockWorld, int blockX, int blockZ, Material blockType) {
        if (!player.getWorld().getName().equals(blockWorld))
            return false;

        boolean flag = true;
        Location blockLocation = new Location(player.getWorld(), blockX, 0.0D, blockZ);
        Block targetBlock = player.getTargetBlockExact(10);
        if (targetBlock != null)
            if (distanceHorizontal(blockLocation, targetBlock.getLocation()) <= 3.0)
                flag = false;

        if (flag) {
            Set<Material> transparent = new HashSet<>();
            transparent.add(Material.AIR);
            transparent.add(Material.WATER);
            transparent.add(Material.LAVA);
            transparent.add(blockType);
            if (targetBlock != null)
                transparent.add(targetBlock.getType());

            List<Block> lineOfSight = player.getLineOfSight(transparent, 10);
            for (Block block1 : lineOfSight) {
                if (distanceHorizontal(blockLocation, block1.getLocation()) <= 2.5) {
                    flag = false;
                    break;
                }
            }
        }

        return flag;
    }

    private boolean executeRaytrace(Player player, String blockWorld, int blockX, int blockZ, Material blockType, Buffer buffer) {
        try {
            return flagRaytrace(player, blockWorld, blockX, blockZ, blockType);
        } catch (IllegalStateException exception) {
            resetBuffer(buffer);
            return false;
        }
    }

    private void resetBuffer(Buffer buffer) {
        buffer.put("lastAsyncResult", false);
        buffer.put("flags", 0);
        buffer.put("lastFlag", 0L);
    }

    private static double getYawChange(Location eyeLocation, LACPlayer lacPlayer) {
        float yaw = yaw(eyeLocation.getYaw());
        PlayerCacheHistory<Location> eventHistory = lacPlayer.cache.history.onEvent.location;
        PlayerCacheHistory<Location> packetHistory = lacPlayer.cache.history.onPacket.location;
        return Math.max(
                Math.min(Math.abs(yaw - yaw(eventHistory.get(HistoryElement.FROM).getYaw())),
                        Math.abs(yaw - yaw(packetHistory.get(HistoryElement.FROM).getYaw()))),
                Math.min(Math.abs(yaw - yaw(eventHistory.get(HistoryElement.FIRST).getYaw())),
                        Math.abs(yaw - yaw(packetHistory.get(HistoryElement.FIRST).getYaw())))
        );
    }

    private static float yaw(float yaw) {
        yaw = yaw % 360;
        return yaw >= 0 ? yaw : 360 - yaw;
    }

}
