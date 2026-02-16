package me.vekster.lightanticheat.util.cooldown;

import me.vekster.lightanticheat.check.Check;
import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.player.cache.entity.CachedEntity;
import me.vekster.lightanticheat.player.cooldown.PlayerCooldown;
import me.vekster.lightanticheat.player.cooldown.element.CooldownElement;
import me.vekster.lightanticheat.player.cooldown.element.EntityDistance;
import me.vekster.lightanticheat.util.annotation.SecureAsync;
import me.vekster.lightanticheat.util.hook.server.folia.FoliaUtil;
import me.vekster.lightanticheat.util.hook.plugin.FloodgateHook;
import me.vekster.lightanticheat.util.player.entities.NearbyEntitiesUtil;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import me.vekster.lightanticheat.version.VerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CooldownUtil {

    @SecureAsync
    public static boolean isSkip(PlayerCooldown cooldown, Check check) {
        CheckName checkName = check.getCheckSetting().name;
        long currentTime = System.currentTimeMillis();
        CooldownElement<Boolean> cooldownElement = cooldown.CHECKS.getOrDefault(checkName, null);
        if (cooldownElement == null) {
            cooldownElement = new CooldownElement<>(currentTime % 3 == 0, currentTime);
            cooldown.CHECKS.put(checkName, cooldownElement);
            return cooldownElement.result;
        }
        if (currentTime - cooldownElement.time > 3000) {
            cooldownElement.result = currentTime % 3 == 0;
            cooldownElement.time = currentTime;
            return cooldownElement.result;
        }
        cooldownElement.result = !cooldownElement.result;
        return cooldownElement.result;
    }

    @SecureAsync
    public static boolean isSkip(long timeout, PlayerCooldown cooldown, Check check) {
        CheckName checkName = check.getCheckSetting().name;
        long currentTime = System.currentTimeMillis();
        long lastExecution = cooldown.CHECK_EXECUTIONS.getOrDefault(checkName, 0L);
        if (currentTime - lastExecution > timeout) {
            cooldown.CHECK_EXECUTIONS.put(checkName, currentTime);
            return false;
        }
        return true;
    }

    public static boolean hasPermission(PlayerCooldown cooldown, Player player, String permission) {
        CooldownElement<Boolean> cooldownElement = cooldown.PERMISSIONS.getOrDefault(permission, null);
        if (cooldownElement == null) {
            cooldownElement = new CooldownElement<>(player.hasPermission(permission), System.currentTimeMillis());
            cooldown.PERMISSIONS.put(permission, cooldownElement);
            return cooldownElement.result;
        }
        if (System.currentTimeMillis() - cooldownElement.time > 3000) {
            cooldownElement.result = player.hasPermission(permission);
            cooldownElement.time = System.currentTimeMillis();
            return cooldownElement.result;
        }
        return cooldownElement.result;
    }

    @SecureAsync
    public static boolean hasPermission(PlayerCooldown cooldown, Player player, String permission, boolean async) {
        if (!async)
            return hasPermission(cooldown, player, permission);

        if (!FoliaUtil.isFolia())
            return hasPermission(cooldown, player, permission);
        /*
          Check the permission from any thread (not for Folia)
         */
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        CooldownElement<Boolean> cooldownElement = cooldown.ASYNC_PERMISSIONS.getOrDefault(permission, null);
        if (cooldownElement == null) {
            Scheduler.runTask(true, () -> {
                CooldownElement<Boolean> cooldownElement1 = new CooldownElement<>(player.hasPermission(permission), System.currentTimeMillis());
                cooldown.ASYNC_PERMISSIONS.put(permission, cooldownElement1);
                result.complete(cooldownElement1.result);
            });
        } else if (System.currentTimeMillis() - cooldownElement.time > 5500) {
            Scheduler.runTask(true, () -> {
                cooldownElement.result = player.hasPermission(permission);
                cooldownElement.time = System.currentTimeMillis();
                result.complete(cooldownElement.result);
            });
        } else {
            result.complete(cooldownElement.result);
        }
        try {
            return result.get(300, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return false;
        }
    }

    @SecureAsync
    public static Set<Entity> getAllEntitiesAsync(PlayerCooldown cooldown, Player player) {
        long currentTime = System.currentTimeMillis();
        CooldownElement<Set<Entity>> cooldownElement = cooldown.ALL_ENTITIES;

        if (cooldownElement.result == null)
            cooldownElement.result = ConcurrentHashMap.newKeySet();

        if (currentTime - cooldownElement.time <= 444)
            return cooldownElement.result;

        if (!FoliaUtil.isFolia()) {
            if (Bukkit.isPrimaryThread()) {
                cooldownElement.result = NearbyEntitiesUtil.getAllEntitiesAsyncWithoutCache(player);
                cooldownElement.time = currentTime;
                return cooldownElement.result;
            }

            CompletableFuture<Set<Entity>> future = new CompletableFuture<>();
            Scheduler.runTask(true, () -> future.complete(NearbyEntitiesUtil.getAllEntitiesAsyncWithoutCache(player)));
            try {
                cooldownElement.result = future.get(250, TimeUnit.MILLISECONDS);
                cooldownElement.time = currentTime;
                return cooldownElement.result;
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                cooldownElement.time = currentTime;
                return cooldownElement.result;
            }
        }

        if (FoliaUtil.isOwnedByCurrentRegion(player)) {
            cooldownElement.result = NearbyEntitiesUtil.getAllEntitiesAsyncWithoutCache(player);
            cooldownElement.time = currentTime;
            return cooldownElement.result;
        }

        cooldownElement.time = currentTime;
        FoliaUtil.runTask(player, () -> {
            if (!player.isOnline())
                return;
            cooldownElement.result = NearbyEntitiesUtil.getAllEntitiesAsyncWithoutCache(player);
            cooldownElement.time = System.currentTimeMillis();
        });
        return cooldownElement.result;
    }

    private static Set<CachedEntity> selectNearbyEntitiesSafely(PlayerCooldown cooldown, Player player, Set<Entity> entities,
                                                                 EntityDistance type, Set<CachedEntity> fallback) {
        if (!FoliaUtil.isFolia()) {
            if (Bukkit.isPrimaryThread())
                return NearbyEntitiesUtil.selectNearbyEntities(player, entities, type);

            CompletableFuture<Set<CachedEntity>> future = new CompletableFuture<>();
            Scheduler.runTask(true, () -> future.complete(NearbyEntitiesUtil.selectNearbyEntities(player, entities, type)));
            try {
                return future.get(250, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                return fallback != null ? fallback : ConcurrentHashMap.newKeySet();
            }
        }

        if (FoliaUtil.isOwnedByCurrentRegion(player))
            return NearbyEntitiesUtil.selectNearbyEntities(player, entities, type);

        long now = System.currentTimeMillis();
        CooldownElement<Set<CachedEntity>> nearbyElement = cooldown.NEARBY_ENTITIES.get(type);
        long minRefreshDelay = type == EntityDistance.VERY_NEARBY ? 30 : 40;
        if (nearbyElement != null && now - nearbyElement.time < minRefreshDelay)
            return fallback != null ? fallback : ConcurrentHashMap.newKeySet();

        if (nearbyElement == null) {
            cooldown.NEARBY_ENTITIES.put(type, new CooldownElement<>(fallback != null ? fallback : ConcurrentHashMap.newKeySet(), now));
        } else {
            nearbyElement.time = now;
        }

        FoliaUtil.runTask(player, () -> {
            if (!player.isOnline())
                return;
            Set<Entity> freshEntities = NearbyEntitiesUtil.getAllEntitiesAsyncWithoutCache(player);
            cooldown.ALL_ENTITIES.result = freshEntities;
            cooldown.ALL_ENTITIES.time = System.currentTimeMillis();

            Set<CachedEntity> freshNearby = NearbyEntitiesUtil.selectNearbyEntities(player, freshEntities, type);
            long refreshTime = System.currentTimeMillis();
            cooldown.NEARBY_ENTITIES.put(type, new CooldownElement<>(freshNearby, refreshTime));
        });
        return fallback != null ? fallback : ConcurrentHashMap.newKeySet();
    }

    public static Set<CachedEntity> getNearbyEntitiesAsync(PlayerCooldown cooldown, Player player, EntityDistance type) {
        long currentTime = System.currentTimeMillis();
        CooldownElement<Set<CachedEntity>> cooldownElement = cooldown.NEARBY_ENTITIES.getOrDefault(type, null);
        if (cooldownElement == null) {
            Set<CachedEntity> entities = selectNearbyEntitiesSafely(cooldown, player, getAllEntitiesAsync(cooldown, player), type, null);
            CooldownElement<Set<CachedEntity>> newElement = new CooldownElement<>(entities, currentTime);
            CooldownElement<Set<CachedEntity>> existingElement = cooldown.NEARBY_ENTITIES.putIfAbsent(type, newElement);
            return existingElement != null ? existingElement.result : newElement.result;
        }
        if (type == EntityDistance.VERY_NEARBY && currentTime - cooldownElement.time > 30 ||
                type == EntityDistance.NEARBY && currentTime - cooldownElement.time > 40) {
            cooldownElement.result = selectNearbyEntitiesSafely(cooldown, player, getAllEntitiesAsync(cooldown, player), type, cooldownElement.result);
            cooldownElement.time = currentTime;
            return cooldownElement.result;
        }
        return cooldownElement.result;
    }

    @SecureAsync
    public static int getPing(PlayerCooldown cooldown, Player player, boolean async) {
        CooldownElement<Integer> cooldownElement = cooldown.PING;
        if (System.currentTimeMillis() - cooldownElement.time > 2000) {
            cooldownElement.result = VerPlayer.getPingWithoutCache(player, async);
            cooldownElement.time = System.currentTimeMillis();
            return cooldownElement.result;
        }
        return cooldownElement.result;
    }

    public static boolean isBedrockPlayer(PlayerCooldown cooldown, Player player) {
        CooldownElement<Boolean> cooldownElement = cooldown.BEDROCK;
        if (System.currentTimeMillis() - cooldownElement.time > 8000) {
            cooldownElement.result = FloodgateHook.isBedrockPlayerWithoutCache(player);
            cooldownElement.time = System.currentTimeMillis();
            return cooldownElement.result;
        }
        return cooldownElement.result;
    }

    @SecureAsync
    public static boolean isBedrockPlayer(PlayerCooldown cooldown, Player player, boolean async) {
        if (!async)
            return isBedrockPlayer(cooldown, player);

        if (!FoliaUtil.isFolia())
            return isBedrockPlayer(cooldown, player);
        /*
          Check if the player joined via Geyser from any thread (not for Folia)
         */
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        CooldownElement<Boolean> cooldownElement = cooldown.BEDROCK;
        if (System.currentTimeMillis() - cooldownElement.time > 8500) {
            Scheduler.runTask(true, () -> {
                cooldownElement.result = FloodgateHook.isBedrockPlayerWithoutCache(player);
                cooldownElement.time = System.currentTimeMillis();
                result.complete(cooldownElement.result);
            });
        } else {
            result.complete(cooldownElement.result);
        }

        try {
            return result.get(300, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return false;
        }
    }

}
