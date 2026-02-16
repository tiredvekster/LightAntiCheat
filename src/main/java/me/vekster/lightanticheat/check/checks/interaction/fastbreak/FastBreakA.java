package me.vekster.lightanticheat.check.checks.interaction.fastbreak;

import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.buffer.Buffer;
import me.vekster.lightanticheat.check.checks.interaction.InteractionCheck;
import me.vekster.lightanticheat.event.playerbreakblock.LACPlayerBreakBlockEvent;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.util.hook.plugin.simplehook.AureliumSkillsHook;
import me.vekster.lightanticheat.util.hook.plugin.simplehook.EnchantsSquaredHook;
import me.vekster.lightanticheat.util.hook.plugin.simplehook.McMMOHook;
import me.vekster.lightanticheat.util.hook.plugin.simplehook.VeinMinerHook;
import me.vekster.lightanticheat.version.VerPlayer;
import me.vekster.lightanticheat.version.VerUtil;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

/**
 * Mining with a pickaxe using Timer or FastBreak hack
 */
public class FastBreakA extends InteractionCheck implements Listener {
    private static final Map<Material, Double> BLOCK_HARDNESS = new HashMap<>();
    private static final Map<Material, Double> TOOL_SPEED = new HashMap<>();
    private static final Map<Material, Integer> TOOL_TIERS = new HashMap<>();
    private static final Map<Material, Integer> BLOCK_REQUIRED_TOOL_TIER = new HashMap<>();

    private static final long TICK_ROUNDING_TOLERANCE_MS = 75L;
    private static final long LATENCY_SPIKE_TOLERANCE_MS = 150L;
    private static final double BASE_BREAK_THRESHOLD_RATIO = 1.45;

    static {
        BLOCK_HARDNESS.put(Material.STONE, 1.5D);
        BLOCK_REQUIRED_TOOL_TIER.put(Material.STONE, 1);

        Material deepslate = VerUtil.material.get("DEEPSLATE");
        if (deepslate != null) {
            BLOCK_HARDNESS.put(deepslate, 3.0D);
            BLOCK_REQUIRED_TOOL_TIER.put(deepslate, 1);
        }

        registerPickaxeIfPresent("WOODEN_PICKAXE", 2.0D, 1);
        registerPickaxe(Material.STONE_PICKAXE, 4.0D, 2);
        registerPickaxe(Material.IRON_PICKAXE, 6.0D, 3);
        registerPickaxe(Material.DIAMOND_PICKAXE, 8.0D, 4);
        registerPickaxeIfPresent("NETHERITE_PICKAXE", 9.0D, 4);
        registerPickaxeIfPresent("GOLDEN_PICKAXE", 12.0D, 1);
        registerPickaxeIfPresent("GOLD_PICKAXE", 12.0D, 1);
    }

    public FastBreakA() {
        super(CheckName.FASTBREAK_A);
    }

    @EventHandler
    public void onBlockBreak(LACPlayerBreakBlockEvent event) {
        Player player = event.getPlayer();
        LACPlayer lacPlayer = event.getLacPlayer();

        if (!isCheckAllowed(player, lacPlayer))
            return;
        if (player.getGameMode() != GameMode.SURVIVAL)
            return;
        Block block = event.getBlock();
        if (AureliumSkillsHook.isPrevented(player) ||
                VeinMinerHook.isPrevented(player) ||
                McMMOHook.isPrevented(player, block))
            return;

        Buffer buffer = getBuffer(player);
        ItemStack tool = lacPlayer.getItemInMainHand();
        if (tool == null) return;
        int efficiencyLevel = tool.getEnchantmentLevel(VerUtil.enchantment.get("EFFICIENCY"));
        if (efficiencyLevel > 5)
            return;

        if (!BLOCK_HARDNESS.containsKey(block.getType())) {
            if (buffer.getInt("flags") > 0)
                buffer.put("flags", buffer.getInt("flags") - 1);
            return;
        }
        if (!TOOL_SPEED.containsKey(tool.getType())) {
            if (buffer.getInt("flags") > 0)
                buffer.put("flags", buffer.getInt("flags") - 1);
            return;
        }

        if (!buffer.isExists("lastInteraction")) {
            if (buffer.getInt("flags") > 0)
                buffer.put("flags", buffer.getInt("flags") - 1);
            return;
        }

        if (!buffer.isExists("blockType") || buffer.getMaterial("blockType") != block.getType()) {
            buffer.put("blockType", block.getType());
            if (buffer.getInt("flags") > 0)
                buffer.put("flags", buffer.getInt("flags") - 1);
            return;
        }

        if (!buffer.isExists("tool") || buffer.getMaterial("tool") != tool.getType()) {
            buffer.put("tool", tool.getType());
            if (buffer.getInt("flags") > 0)
                buffer.put("flags", buffer.getInt("flags") - 1);
            return;
        }

        if (!buffer.isExists("efficiencyLevel") || buffer.getInt("efficiencyLevel") != efficiencyLevel) {
            buffer.put("efficiencyLevel", efficiencyLevel);
            if (buffer.getInt("flags") > 0)
                buffer.put("flags", buffer.getInt("flags") - 1);
            return;
        }

        int environmentSignature = getEnvironmentSignature(player);
        if (!buffer.isExists("environmentSignature") || buffer.getInt("environmentSignature") != environmentSignature) {
            buffer.put("environmentSignature", environmentSignature);
            buffer.put("flags", 0);
            return;
        }

        long interval = System.currentTimeMillis() - buffer.getLong("lastInteraction");

        long maxDuration = getExpectedMineDuration(player, block, tool, efficiencyLevel);
        long tolerance = getIntervalTolerance(player);
        boolean flag = interval + tolerance < maxDuration / BASE_BREAK_THRESHOLD_RATIO;

        if (flag) {
            if (buffer.getInt("flags") < 6)
                buffer.put("flags", buffer.getInt("flags") + 1);
        } else {
            if (buffer.getInt("flags") > 0)
                buffer.put("flags", buffer.getInt("flags") - 1);
            if (buffer.getInt("flags") > 0)
                buffer.put("flags", buffer.getInt("flags") - 1);
        }

        if (buffer.getInt("flags") < 6)
            return;
        if (buffer.getInt("flags") > 0)
            buffer.put("flags", buffer.getInt("flags") - 1);
        if (buffer.getInt("flags") > 0)
            buffer.put("flags", buffer.getInt("flags") - 1);

        if (EnchantsSquaredHook.hasEnchantment(player, "Excavation", "Deforestation", "Harvesting"))
            return;

        Map<String, Double> attributes = getPlayerAttributes(player);
        if (getItemStackAttributes(player, "PLAYER_BLOCK_BREAK_SPEED",
                "PLAYER_MINING_EFFICIENCY", "PLAYER_SUBMERGED_MINING_SPEED") != 0 ||
                attributes.getOrDefault("PLAYER_BLOCK_BREAK_SPEED", 0.0) > 0.01 ||
                attributes.getOrDefault("PLAYER_MINING_EFFICIENCY", 0.0) > 0.01 ||
                attributes.getOrDefault("PLAYER_SUBMERGED_MINING_SPEED", 0.0) > 0.01)
            buffer.put("attribute", System.currentTimeMillis());
        if (System.currentTimeMillis() - buffer.getLong("attribute") < 3500)
            return;

        callViolationEvent(player, lacPlayer, event.getEvent());
    }

    private static void registerPickaxe(Material tool, double speed, int tier) {
        TOOL_SPEED.put(tool, speed);
        TOOL_TIERS.put(tool, tier);
    }

    private static void registerPickaxeIfPresent(String materialKey, double speed, int tier) {
        Material material = VerUtil.material.get(materialKey);
        if (material != null)
            registerPickaxe(material, speed, tier);
    }

    private long getExpectedMineDuration(Player player, Block block, ItemStack tool, int efficiencyLevel) {
        double hardness = BLOCK_HARDNESS.getOrDefault(block.getType(), 1.5D);
        double speedMultiplier = getToolSpeedMultiplier(player, tool, efficiencyLevel);
        double divisor = canHarvest(block, tool) ? 30.0D : 100.0D;
        double damagePerTick = speedMultiplier / hardness / divisor;

        int ticksToBreak = (int) Math.ceil(1.0D / Math.max(damagePerTick, 1.0E-4D));
        return ticksToBreak * 50L;
    }

    private double getToolSpeedMultiplier(Player player, ItemStack tool, int efficiencyLevel) {
        double speedMultiplier = TOOL_SPEED.getOrDefault(tool.getType(), 1.0D);

        if (speedMultiplier > 1.0D && efficiencyLevel > 0)
            speedMultiplier += efficiencyLevel * efficiencyLevel + 1.0D;

        int hasteLevel = getEffectAmplifier(player, PotionEffectType.HASTE);
        if (hasteLevel > 0)
            speedMultiplier *= 1.0D + hasteLevel * 0.2D;

        PotionEffectType fatigueType = VerUtil.potions.getOrDefault("MINING_FATIGUE", PotionEffectType.MINING_FATIGUE);
        if (fatigueType != null) {
            int fatigueLevel = getEffectAmplifier(player, fatigueType);
            if (fatigueLevel > 0)
                speedMultiplier *= getMiningFatigueMultiplier(fatigueLevel);
        }

        if (isUnderwater(player) && !hasAquaAffinity(player))
            speedMultiplier /= 5.0D;

        return Math.max(speedMultiplier, 0.0001D);
    }

    private boolean canHarvest(Block block, ItemStack tool) {
        int requiredTier = BLOCK_REQUIRED_TOOL_TIER.getOrDefault(block.getType(), Integer.MAX_VALUE);
        int toolTier = TOOL_TIERS.getOrDefault(tool.getType(), 0);
        return toolTier >= requiredTier;
    }

    private boolean hasAquaAffinity(Player player) {
        if (VerUtil.enchantment.get("AQUA_AFFINITY") == null)
            return false;

        ItemStack helmet = player.getInventory().getHelmet();
        return helmet != null && helmet.getEnchantmentLevel(VerUtil.enchantment.get("AQUA_AFFINITY")) > 0;
    }

    private boolean isUnderwater(Player player) {
        Material eyeBlockType = player.getEyeLocation().getBlock().getType();
        if (eyeBlockType == Material.WATER)
            return true;

        Material bubbleColumn = VerUtil.material.get("BUBBLE_COLUMN");
        return bubbleColumn != null && eyeBlockType == bubbleColumn;
    }

    private double getMiningFatigueMultiplier(int fatigueLevel) {
        switch (fatigueLevel) {
            case 1:
                return 0.3D;
            case 2:
                return 0.09D;
            case 3:
                return 0.0027D;
            default:
                return 0.00081D;
        }
    }

    private long getIntervalTolerance(Player player) {
        int ping = Math.max(0, VerPlayer.getPing(player));
        long pingTolerance = Math.min(200L, Math.round(ping * 0.35D));
        return TICK_ROUNDING_TOLERANCE_MS + LATENCY_SPIKE_TOLERANCE_MS + pingTolerance;
    }

    private int getEnvironmentSignature(Player player) {
        int hasteLevel = getEffectAmplifier(player, PotionEffectType.HASTE);
        PotionEffectType fatigueType = VerUtil.potions.getOrDefault("MINING_FATIGUE", PotionEffectType.MINING_FATIGUE);
        int fatigueLevel = fatigueType == null ? 0 : getEffectAmplifier(player, fatigueType);
        int underwater = isUnderwater(player) ? 1 : 0;
        int aquaAffinity = hasAquaAffinity(player) ? 1 : 0;

        return hasteLevel * 1000 + fatigueLevel * 100 + underwater * 10 + aquaAffinity;
    }

    @EventHandler
    public void onInteraction(PlayerInteractEvent event) {
        if (isExternalNPC(event)) return;
        if (event.getAction() != Action.LEFT_CLICK_BLOCK)
            return;

        Buffer buffer = getBuffer(event.getPlayer());
        buffer.put("lastInteraction", System.currentTimeMillis());
    }


}
