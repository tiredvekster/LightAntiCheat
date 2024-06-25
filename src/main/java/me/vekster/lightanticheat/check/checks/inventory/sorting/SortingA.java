package me.vekster.lightanticheat.check.checks.inventory.sorting;

import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.buffer.Buffer;
import me.vekster.lightanticheat.check.checks.inventory.InventoryCheck;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.util.async.AsyncUtil;
import me.vekster.lightanticheat.util.hook.FloodgateHook;
import me.vekster.lightanticheat.util.hook.simplehook.EnchantsSquaredHook;
import me.vekster.lightanticheat.util.reflection.ReflectionException;
import me.vekster.lightanticheat.util.reflection.ReflectionUtil;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import me.vekster.lightanticheat.version.VerUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

/**
 * Item swap interval
 */
public class SortingA extends InventoryCheck implements Listener {
    public SortingA() {
        super(CheckName.SORTING_A);
    }

    private static boolean outdated;
    private static final Set<InventoryAction> IGNORED_ACTIONS = new HashSet<>();

    static {
        IGNORED_ACTIONS.add(InventoryAction.CLONE_STACK);
        IGNORED_ACTIONS.add(InventoryAction.MOVE_TO_OTHER_INVENTORY);
        IGNORED_ACTIONS.add(InventoryAction.NOTHING);
        IGNORED_ACTIONS.add(InventoryAction.UNKNOWN);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player))
            return;
        Player player = (Player) event.getWhoClicked();
        if (isExternalNPC(player)) return;
        if (player.getGameMode() == GameMode.CREATIVE)
            return;
        LACPlayer lacPlayer = LACPlayer.getLacPlayer(player);

        Scheduler.entityThread(player, () -> {
            if (!isCheckAllowed(player, lacPlayer))
                return;

            if (FloodgateHook.isProbablyPocketEditionPlayer(player, true))
                return;

            if (IGNORED_ACTIONS.contains(event.getAction()))
                return;

            ItemStack cursor = event.getCursor();
            if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE &&
                    cursor != null && cursor.getType() == Material.AIR)
                return;

            Buffer buffer = getBuffer(player);
            InventoryType inventoryType = player.getOpenInventory().getType();
            if (inventoryType != InventoryType.PLAYER && inventoryType != InventoryType.CRAFTING &&
                    System.currentTimeMillis() - buffer.getLong("lastOpenChest") > 4 * 1000)
                return;

            long currentTime = System.currentTimeMillis();
            if (!buffer.isExists("startTime") || currentTime - buffer.getLong("startTime") > 22) {
                buffer.put("startTime", currentTime);
                buffer.put("clicks", 0);
            }

            if (buffer.isExists("lastSlot2") && buffer.getInt("lastSlot2") == event.getSlot()) {
                if (buffer.isExists("lastSlot1"))
                    buffer.put("lastSlot2", buffer.getInt("lastSlot1"));
                return;
            }
            if (buffer.isExists("lastSlot1"))
                buffer.put("lastSlot2", buffer.getInt("lastSlot1"));

            if (!buffer.isExists("lastSlot1") || buffer.getInt("lastSlot1") == event.getSlot()) {
                buffer.put("lastSlot1", event.getSlot());
                return;
            }
            buffer.put("lastSlot1", event.getSlot());

            buffer.put("clicks", buffer.getInt("clicks") + 1);
            if (buffer.getInt("clicks") < 8)
                return;

            if (currentTime - buffer.getLong("lastFlag") < 500)
                return;
            buffer.put("lastFlag", currentTime);

            if (inventoryType == InventoryType.CRAFTING) {
                Block block = lacPlayer.getTargetBlockExact(10);
                if (block != null && block.getType() == VerUtil.material.get("CRAFTING_TABLE"))
                    return;
            }

            if (EnchantsSquaredHook.hasEnchantment(player, "Telekinesis"))
                return;

            callViolationEvent(player, lacPlayer, event);
        });
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player))
            return;
        Player player = (Player) event.getPlayer();
        if (isExternalNPC(player))
            return;
        if (outdated)
            return;
        Scheduler.entityThread(player, () -> {
            Location location = null;
            try {
                Object object = ReflectionUtil.runDeclaredMethod(event.getInventory(), "getLocation");
                if (object instanceof Location) location = (Location) object;
            } catch (ReflectionException e) {
                outdated = true;
                return;
            }
            if (location == null)
                return;
            Block block = AsyncUtil.getBlock(location);
            Material material = block != null ? block.getType() : Material.AIR;
            if (material != Material.CHEST && material != Material.TRAPPED_CHEST)
                return;
            Buffer buffer = getBuffer(player);
            buffer.put("lastOpenChest", System.currentTimeMillis());
        });
    }

}
