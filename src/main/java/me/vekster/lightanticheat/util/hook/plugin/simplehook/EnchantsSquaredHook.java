package me.vekster.lightanticheat.util.hook.plugin.simplehook;

import me.vekster.lightanticheat.util.hook.plugin.HookUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class EnchantsSquaredHook extends HookUtil {

    public static boolean isPluginInstalled() {
        return isPlugin("EnchantsSquared") || isPlugin("EnchantsPlus");
    }

    public static boolean hasEnchantment(Player player, String... enchantments) {
        if (!isPlugin("EnchantsSquared") && !isPlugin("EnchantsPlus"))
            return false;
        for (ItemStack itemStack : player.getInventory().getContents()) {
            if (itemStack == null || itemStack.getAmount() == 0)
                continue;
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta == null) continue;
            List<String> itemLore = itemMeta.getLore();
            if (itemLore == null) continue;
            for (String line : itemLore)
                for (String enchantment : enchantments)
                    if (line.contains(enchantment))
                        return true;
        }
        return false;
    }

}
