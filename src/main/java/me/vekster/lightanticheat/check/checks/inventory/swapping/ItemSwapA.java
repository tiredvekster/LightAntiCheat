package me.vekster.lightanticheat.check.checks.inventory.swapping;

import me.vekster.lightanticheat.Main;
import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.buffer.Buffer;
import me.vekster.lightanticheat.check.checks.inventory.InventoryCheck;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.player.cache.PlayerCache;
import me.vekster.lightanticheat.player.cache.history.HistoryElement;
import me.vekster.lightanticheat.util.hook.FloodgateHook;
import me.vekster.lightanticheat.util.hook.simplehook.EnchantsSquaredHook;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * Item swap while walking
 */
public class ItemSwapA extends InventoryCheck implements Listener {
    public ItemSwapA() {
        super(CheckName.ITEMSWAP_A);
    }

    private static final Set<InventoryAction> IGNORED_ACTIONS = new HashSet<>();

    static {
        IGNORED_ACTIONS.add(InventoryAction.NOTHING);
        IGNORED_ACTIONS.add(InventoryAction.UNKNOWN);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player))
            return;
        Player player = (Player) event.getWhoClicked();
        if (isExternalNPC(player)) return;
        LACPlayer lacPlayer = LACPlayer.getLacPlayer(player);
        PlayerCache cache = lacPlayer.cache;

        Scheduler.entityThread(player, () -> {
            if (!isCheckAllowed(player, lacPlayer))
                return;

            if (FloodgateHook.isBedrockPlayer(player, true))
                return;

            if (IGNORED_ACTIONS.contains(event.getAction()))
                return;

            int lagCompensationTicks = (int) Math.ceil(lacPlayer.getPing(true) / 50.0);
            if ((!player.isSprinting() || cache.sprintingTicks < 2 + lagCompensationTicks) &&
                    (!player.isSneaking() || cache.sneakingTicks < 2 + lagCompensationTicks) &&
                    (!lacPlayer.isSwimming() || cache.swimmingTicks < 2 + lagCompensationTicks))
                return;

            PlayerCache.History history = lacPlayer.cache.history;
            if (distance(history.onEvent.location.get(HistoryElement.FROM),
                    history.onEvent.location.get(HistoryElement.FIRST)) < Float.MIN_VALUE * 5)
                return;

            Buffer buffer = getBuffer(player);
            long currentTime = System.currentTimeMillis();
            if (currentTime - buffer.getLong("lastFlag") < 450)
                return;
            buffer.put("lastFlag", currentTime);

            buffer.put("flags", buffer.getInt("flags") + 1);
            if (buffer.getInt("flags") <= 1)
                return;
            if (lacPlayer.getPing() > 250 && buffer.getInt("flags") <= 2)
                return;

            if (EnchantsSquaredHook.hasEnchantment(player, "Telekinesis"))
                return;

            callViolationEventIfRepeat(player, lacPlayer, event, buffer, Main.getBufferDurationMils() - 1000L);
        });
    }

}
