package me.vekster.lightanticheat.version;

import me.vekster.lightanticheat.Main;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.util.annotation.SecureAsync;
import me.vekster.lightanticheat.util.cooldown.CooldownUtil;
import me.vekster.lightanticheat.util.logger.LogType;
import me.vekster.lightanticheat.util.logger.Logger;
import me.vekster.lightanticheat.util.reflection.ReflectionException;
import me.vekster.lightanticheat.util.reflection.ReflectionUtil;
import me.vekster.lightanticheat.version.identifier.LACVersion;
import me.vekster.lightanticheat.version.identifier.VerIdentifier;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VerPlayer {

    private static final Map<String, Boolean> CACHE = new HashMap<>();
    private static final Map<String, Boolean> ASYNC_CACHE = new ConcurrentHashMap<>();
    private static Class<?> craftPlayerClass;
    private final Player PLAYER;

    static {
        try {
            craftPlayerClass = ReflectionUtil.classForName("org.bukkit.craftbukkit.$version.entity.CraftPlayer");
        } catch (ReflectionException e) {
            Logger.logConsole(LogType.ERROR, "(" + Main.getInstance().getName() + ") CraftPlayer class is not found!");
        }
    }

    public VerPlayer(Player player) {
        this.PLAYER = player;
    }

    public static int getPingWithoutCache(Player player, boolean async) {
        String methodName = "getPing";
        Map<String, Boolean> cache = !async ? CACHE : ASYNC_CACHE;

        if (!cache.containsKey(methodName)) {
            try {
                cache.put(methodName, ReflectionUtil.runDeclaredMethod(player, methodName) != null);
            } catch (ReflectionException e) {
                cache.put(methodName, false);
            }
        }

        try {
            if (cache.get(methodName)) {
                Object value = ReflectionUtil.runDeclaredMethod(player, methodName);
                if (value instanceof Integer)
                    return (int) value;
                return 250;
            }

            if (craftPlayerClass == null) return 0;
            Object craftPlayer = craftPlayerClass.cast(player);
            Object entityPlayer = ReflectionUtil.runDeclaredMethod(craftPlayer, "getHandle");
            if (entityPlayer == null) return 0;
            Object result = ReflectionUtil.getDeclaredField(entityPlayer, "ping");
            if (result instanceof Integer)
                return (int) result;
        } catch (ReflectionException e) {
            return 250;
        }
        return 250;
    }

    public static int getPing(Player player) {
        return CooldownUtil.getPing(LACPlayer.getLacPlayer(player).cooldown, player, false);
    }

    @SecureAsync
    public static int getPing(Player player, boolean async) {
        return CooldownUtil.getPing(LACPlayer.getLacPlayer(player).cooldown, player, async);
    }

    public int getPing() {
        return CooldownUtil.getPing(LACPlayer.getLacPlayer(PLAYER).cooldown, PLAYER, false);
    }

    @SecureAsync
    public int getPing(boolean async) {
        return CooldownUtil.getPing(LACPlayer.getLacPlayer(PLAYER).cooldown, PLAYER, async);
    }

    @SecureAsync
    public static boolean isGliding(Player player) {
        return VerUtil.multiVersion.isGliding(player) ||
                VerUtil.multiVersion.isGlidingToggled(player);
    }

    @SecureAsync
    public boolean isGliding() {
        return VerUtil.multiVersion.isGliding(PLAYER) ||
                VerUtil.multiVersion.isGlidingToggled(PLAYER);
    }

    @SecureAsync
    public static boolean isRiptiding(Player player) {
        return VerUtil.multiVersion.isRiptiding(player);
    }

    @SecureAsync
    public boolean isRiptiding() {
        return VerUtil.multiVersion.isRiptiding(PLAYER);
    }

    @SecureAsync
    public static boolean isSwimming(Player player) {
        return VerUtil.multiVersion.isSwimming(player);
    }

    @SecureAsync
    public boolean isSwimming() {
        return VerUtil.multiVersion.isSwimming(PLAYER);
    }

    @SecureAsync
    public static boolean isClimbing(Player player) {
        return VerUtil.multiVersion.isClimbing(player);
    }

    @SecureAsync
    public boolean isClimbing() {
        return VerUtil.multiVersion.isClimbing(PLAYER);
    }

    @SecureAsync
    public static boolean isInWater(Player player) {
        return VerUtil.multiVersion.isInWater(player);
    }

    @SecureAsync
    public boolean isInWater() {
        return VerUtil.multiVersion.isInWater(PLAYER);
    }

    @SecureAsync
    public static ItemStack getItemInMainHand(Player player) {
        return VerUtil.multiVersion.getItemInMainHand(player);
    }

    @SecureAsync
    public ItemStack getItemInMainHand() {
        return VerUtil.multiVersion.getItemInMainHand(PLAYER);
    }

    @SecureAsync
    public static ItemStack getItemInOffHand(Player player) {
        return VerUtil.multiVersion.getItemInOffHand(player);
    }

    @SecureAsync
    public ItemStack getItemInOffHand() {
        return VerUtil.multiVersion.getItemInOffHand(PLAYER);
    }

    @SecureAsync
    @Nullable
    public static Block getTargetBlockExact(Player player, int distance) {
        return VerUtil.multiVersion.getTargetBlockExact(player, distance);
    }

    @SecureAsync
    @Nullable
    public Block getTargetBlockExact(int distance) {
        return VerUtil.multiVersion.getTargetBlockExact(PLAYER, distance);
    }

    @SecureAsync
    public static void sendBlockDate(Player player, Location location, Block block) {
        VerUtil.multiVersion.sendBlockData(player, location, block);
    }

    @SecureAsync
    public void sendBlockDate(Location location, Block block) {
        VerUtil.multiVersion.sendBlockData(PLAYER, location, block);
    }

    @SecureAsync
    public static boolean sendHoverMessage(Player player, List<String> lines, boolean hexColor) {
        return VerUtil.multiVersion.sendHoverMessage(player, lines, hexColor);
    }

    @SecureAsync
    public boolean sendHoverMessage(List<String> lines, boolean hexColor) {
        return VerUtil.multiVersion.sendHoverMessage(PLAYER, lines, hexColor);
    }

    @NotNull
    public ItemStack getArmorPiece(EquipmentSlot equipmentSlot) {
        return VerUtil.getArmorPiece(PLAYER.getInventory(), equipmentSlot);
    }

}
