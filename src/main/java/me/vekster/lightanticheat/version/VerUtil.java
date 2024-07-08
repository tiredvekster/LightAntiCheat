package me.vekster.lightanticheat.version;

import me.vekster.lightanticheat.Main;
import me.vekster.lightanticheat.event.LACEventCaller;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import me.vekster.lightanticheat.version.identifier.LACVersion;
import me.vekster.lightanticheat.version.identifier.VerIdentifier;
import me.vekster.multiversion.*;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class VerUtil {
    protected static MultiVersion multiVersion = null;
    public static VerEnumValues<Material> material = new VerEnumValues<>(new HashMap<>(), null);
    public static VerEnumValues<Enchantment> enchantment = new VerEnumValues<>(new HashMap<>(), null);
    public static VerEnumValues<EntityType> entityTypes = new VerEnumValues<>(new HashMap<>(), null);
    public static VerEnumValues<PotionEffectType> potions = new VerEnumValues<>(new HashMap<>(), null);
    private static final Map<EquipmentSlot, Integer> ARMOR_SLOTS = new HashMap<>();

    public static class VerEnumValues<T> {
        private final Map<String, T> VALUES = new HashMap<>();
        private final T defaultValue;

        public VerEnumValues(Map<String, T> values, T defaultValue) {
            this.VALUES.putAll(new HashMap<>(values));
            this.defaultValue = defaultValue;
        }

        public T getOrDefault(String string, T defaultValue) {
            return VALUES.getOrDefault(string.toLowerCase(), defaultValue);
        }

        public T get(String string) {
            return VALUES.getOrDefault(string.toLowerCase(), this.defaultValue);
        }
    }

    static {
        LACVersion lacVersion = VerIdentifier.getVersion();
        switch (lacVersion) {
            case V1_8:
                multiVersion = new V1_8(Main.getInstance()) {
                    @Override
                    public void onBlockPlace(BlockPlaceEvent event) {
                        LACEventCaller.callBlockPlaceEvents(event);
                    }

                    @Override
                    public void onBlockBreak(BlockBreakEvent event) {
                        LACEventCaller.callBlockBreakEvents(event);
                    }

                    @Override
                    public void onMovement(PlayerMoveEvent event) {
                        LACEventCaller.callMovementEvents(event);
                    }

                    @Override
                    public void onEntityDamage(EntityDamageByEntityEvent event) {
                        LACEventCaller.callEntityDamageEvent(event);
                    }
                };
                break;
            case V1_9:
                multiVersion = new V1_9(Main.getInstance()) {
                    @Override
                    public void onBlockPlace(BlockPlaceEvent event) {
                        LACEventCaller.callBlockPlaceEvents(event);
                    }

                    @Override
                    public void onBlockBreak(BlockBreakEvent event) {
                        LACEventCaller.callBlockBreakEvents(event);
                    }

                    @Override
                    public void onMovement(PlayerMoveEvent event) {
                        LACEventCaller.callMovementEvents(event);
                    }

                    @Override
                    public void onEntityDamage(EntityDamageByEntityEvent event) {
                        LACEventCaller.callEntityDamageEvent(event);
                    }
                };
                break;
            case V1_10:
                multiVersion = new V1_10(Main.getInstance()) {
                    @Override
                    public void onBlockPlace(BlockPlaceEvent event) {
                        LACEventCaller.callBlockPlaceEvents(event);
                    }

                    @Override
                    public void onBlockBreak(BlockBreakEvent event) {
                        LACEventCaller.callBlockBreakEvents(event);
                    }

                    @Override
                    public void onMovement(PlayerMoveEvent event) {
                        LACEventCaller.callMovementEvents(event);
                    }

                    @Override
                    public void onEntityDamage(EntityDamageByEntityEvent event) {
                        LACEventCaller.callEntityDamageEvent(event);
                    }
                };
                break;
            case V1_11:
                multiVersion = new V1_11(Main.getInstance()) {
                    @Override
                    public void onBlockPlace(BlockPlaceEvent event) {
                        LACEventCaller.callBlockPlaceEvents(event);
                    }

                    @Override
                    public void onBlockBreak(BlockBreakEvent event) {
                        LACEventCaller.callBlockBreakEvents(event);
                    }

                    @Override
                    public void onMovement(PlayerMoveEvent event) {
                        LACEventCaller.callMovementEvents(event);
                    }

                    @Override
                    public void onEntityDamage(EntityDamageByEntityEvent event) {
                        LACEventCaller.callEntityDamageEvent(event);
                    }
                };
                break;
            case V1_12:
                multiVersion = new V1_12(Main.getInstance()) {
                    @Override
                    public void onBlockPlace(BlockPlaceEvent event) {
                        LACEventCaller.callBlockPlaceEvents(event);
                    }

                    @Override
                    public void onBlockBreak(BlockBreakEvent event) {
                        LACEventCaller.callBlockBreakEvents(event);
                    }

                    @Override
                    public void onMovement(PlayerMoveEvent event) {
                        LACEventCaller.callMovementEvents(event);
                    }

                    @Override
                    public void onEntityDamage(EntityDamageByEntityEvent event) {
                        LACEventCaller.callEntityDamageEvent(event);
                    }
                };
                break;
            case V1_13:
                multiVersion = new V1_13(Main.getInstance()) {
                    @Override
                    public void onBlockPlace(BlockPlaceEvent event) {
                        LACEventCaller.callBlockPlaceEvents(event);
                    }

                    @Override
                    public void onBlockBreak(BlockBreakEvent event) {
                        LACEventCaller.callBlockBreakEvents(event);
                    }

                    @Override
                    public void onMovement(PlayerMoveEvent event) {
                        LACEventCaller.callMovementEvents(event);
                    }

                    @Override
                    public void onEntityDamage(EntityDamageByEntityEvent event) {
                        LACEventCaller.callEntityDamageEvent(event);
                    }
                };
                break;
            case V1_14:
                multiVersion = new V1_14(Main.getInstance()) {
                    @Override
                    public void onBlockPlace(BlockPlaceEvent event) {
                        LACEventCaller.callBlockPlaceEvents(event);
                    }

                    @Override
                    public void onBlockBreak(BlockBreakEvent event) {
                        LACEventCaller.callBlockBreakEvents(event);
                    }

                    @Override
                    public void onMovement(PlayerMoveEvent event) {
                        LACEventCaller.callMovementEvents(event);
                    }

                    @Override
                    public void onEntityDamage(EntityDamageByEntityEvent event) {
                        LACEventCaller.callEntityDamageEvent(event);
                    }
                };
                break;
            case V1_15:
                multiVersion = new V1_15(Main.getInstance()) {
                    @Override
                    public void onBlockPlace(BlockPlaceEvent event) {
                        LACEventCaller.callBlockPlaceEvents(event);
                    }

                    @Override
                    public void onBlockBreak(BlockBreakEvent event) {
                        LACEventCaller.callBlockBreakEvents(event);
                    }

                    @Override
                    public void onMovement(PlayerMoveEvent event) {
                        LACEventCaller.callMovementEvents(event);
                    }

                    @Override
                    public void onEntityDamage(EntityDamageByEntityEvent event) {
                        LACEventCaller.callEntityDamageEvent(event);
                    }
                };
                break;
            case V1_16:
                multiVersion = new V1_16(Main.getInstance()) {
                    @Override
                    public void onBlockPlace(BlockPlaceEvent event) {
                        LACEventCaller.callBlockPlaceEvents(event);
                    }

                    @Override
                    public void onBlockBreak(BlockBreakEvent event) {
                        LACEventCaller.callBlockBreakEvents(event);
                    }

                    @Override
                    public void onMovement(PlayerMoveEvent event) {
                        LACEventCaller.callMovementEvents(event);
                    }

                    @Override
                    public void onEntityDamage(EntityDamageByEntityEvent event) {
                        LACEventCaller.callEntityDamageEvent(event);
                    }
                };
                break;
            case V1_17:
                multiVersion = new V1_17(Main.getInstance()) {
                    @Override
                    public void onBlockPlace(BlockPlaceEvent event) {
                        LACEventCaller.callBlockPlaceEvents(event);
                    }

                    @Override
                    public void onBlockBreak(BlockBreakEvent event) {
                        LACEventCaller.callBlockBreakEvents(event);
                    }

                    @Override
                    public void onMovement(PlayerMoveEvent event) {
                        LACEventCaller.callMovementEvents(event);
                    }

                    @Override
                    public void onEntityDamage(EntityDamageByEntityEvent event) {
                        LACEventCaller.callEntityDamageEvent(event);
                    }
                };
                break;
            case V1_18:
                multiVersion = new V1_18(Main.getInstance()) {
                    @Override
                    public void onBlockPlace(BlockPlaceEvent event) {
                        LACEventCaller.callBlockPlaceEvents(event);
                    }

                    @Override
                    public void onBlockBreak(BlockBreakEvent event) {
                        LACEventCaller.callBlockBreakEvents(event);
                    }

                    @Override
                    public void onMovement(PlayerMoveEvent event) {
                        LACEventCaller.callMovementEvents(event);
                    }

                    @Override
                    public void onEntityDamage(EntityDamageByEntityEvent event) {
                        LACEventCaller.callEntityDamageEvent(event);
                    }
                };
                break;
            case V1_19:
                multiVersion = new V1_19(Main.getInstance()) {
                    @Override
                    public void onBlockPlace(BlockPlaceEvent event) {
                        LACEventCaller.callBlockPlaceEvents(event);
                    }

                    @Override
                    public void onBlockBreak(BlockBreakEvent event) {
                        LACEventCaller.callBlockBreakEvents(event);
                    }

                    @Override
                    public void onMovement(PlayerMoveEvent event) {
                        LACEventCaller.callMovementEvents(event);
                    }

                    @Override
                    public void onEntityDamage(EntityDamageByEntityEvent event) {
                        LACEventCaller.callEntityDamageEvent(event);
                    }
                };
                break;
            case V1_20:
                multiVersion = new V1_20(Main.getInstance()) {
                    @Override
                    public void onBlockPlace(BlockPlaceEvent event) {
                        LACEventCaller.callBlockPlaceEvents(event);
                    }

                    @Override
                    public void onBlockBreak(BlockBreakEvent event) {
                        LACEventCaller.callBlockBreakEvents(event);
                    }

                    @Override
                    public void onMovement(PlayerMoveEvent event) {
                        LACEventCaller.callMovementEvents(event);
                    }

                    @Override
                    public void onEntityDamage(EntityDamageByEntityEvent event) {
                        LACEventCaller.callEntityDamageEvent(event);
                    }
                };
                break;
        }
    }

    static {
        Map<String, Material> materials = new HashMap<>();
        for (Material material : Material.values()) {
            if (material == null) continue;
            materials.put(material.name().toLowerCase(), material);
        }
        VerUtil.material = new VerEnumValues<>(materials, Material.MAP);

        Map<String, Enchantment> enchantments = new HashMap<>();
        for (Enchantment enchantment : Enchantment.values()) {
            if (enchantment == null) continue;
            String key = multiVersion.getEnchantmentKey(enchantment);
            if (key == null) continue;
            enchantments.put(key.toLowerCase(), enchantment);
        }
        VerUtil.enchantment = new VerEnumValues<>(enchantments, Enchantment.LUCK);
        Scheduler.runTask(false, () -> {
            Map<String, Enchantment> newerEnchantments = new HashMap<>();
            for (Enchantment enchantment : Enchantment.values()) {
                if (enchantment == null) continue;
                String key = multiVersion.getEnchantmentKey(enchantment);
                if (key == null) continue;
                newerEnchantments.put(key.toLowerCase(), enchantment);
            }
            VerUtil.enchantment = new VerEnumValues<>(newerEnchantments, Enchantment.LUCK);
        });

        Map<String, EntityType> entityTypes = new HashMap<>();
        for (EntityType entityType : EntityType.values()) {
            if (entityType == null) continue;
            entityTypes.put(entityType.name().toLowerCase(), entityType);
        }
        VerUtil.entityTypes = new VerEnumValues<>(entityTypes, EntityType.UNKNOWN);

        Map<String, PotionEffectType> potions = new HashMap<>();
        for (PotionEffectType potionEffectType : PotionEffectType.values()) {
            if (potionEffectType == null) continue;
            potions.put(potionEffectType.getName().toLowerCase(), potionEffectType);
        }
        VerUtil.potions = new VerEnumValues<>(potions, PotionEffectType.NIGHT_VISION);
        Scheduler.runTask(false, () -> {
            Map<String, PotionEffectType> newerPotions = new HashMap<>();
            for (PotionEffectType potionEffectType : PotionEffectType.values()) {
                if (potionEffectType == null) continue;
                newerPotions.put(potionEffectType.getName().toLowerCase(), potionEffectType);
            }
            VerUtil.potions = new VerEnumValues<>(newerPotions, PotionEffectType.NIGHT_VISION);
        });
    }

    static {
        ARMOR_SLOTS.put(EquipmentSlot.FEET, 0);
        ARMOR_SLOTS.put(EquipmentSlot.LEGS, 1);
        ARMOR_SLOTS.put(EquipmentSlot.CHEST, 2);
        ARMOR_SLOTS.put(EquipmentSlot.HEAD, 3);
    }

    @NotNull
    public static ItemStack getArmorPiece(PlayerInventory inventory, EquipmentSlot slot) {
        if (!ARMOR_SLOTS.containsKey(slot))
            return new ItemStack(Material.AIR, 0);
        ItemStack armorPiece = inventory.getArmorContents()[ARMOR_SLOTS.get(slot)];
        return armorPiece != null ? armorPiece : new ItemStack(Material.AIR, 0);
    }

    public static int getEnchantmentLevel(ItemStack itemStack, Enchantment enchantment) {
        return multiVersion.getEnchantmentLevel(itemStack, enchantment);
    }

    public static Map<String, Double> getAttributes(ItemStack itemStack) {
        return multiVersion.getAttributes(itemStack);
    }

    public static Map<String, Double> getAttributes(Player player) {
        return multiVersion.getAttributes(player);
    }

    public static int getPotionLevel(LivingEntity entity, PotionEffectType effectType) {
        return multiVersion.getPotionLevel(entity, effectType);
    }

    public static double getWidth(Entity entity) {
        return multiVersion.getWidth(entity);
    }

    public static double getHeight(Entity entity) {
        return multiVersion.getHeight(entity);
    }

    public static ProjectileSource getSpectralArrowShooter(Entity spectralArrow) {
        return multiVersion.getSpectralArrowShooter(spectralArrow);
    }

    public static boolean isPassable(Block block) {
        return multiVersion.isPassable(block);
    }

    public static boolean isWatterLoggedSlab(Block block) {
        return multiVersion.isWatterLoggedSlab(block);
    }

    public static int getSnowLayers(Block block) {
        return multiVersion.getSnowLayers(block);
    }

}
