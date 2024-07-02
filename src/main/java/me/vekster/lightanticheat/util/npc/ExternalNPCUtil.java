package me.vekster.lightanticheat.util.npc;

import me.vekster.lightanticheat.util.annotation.SecureAsync;
import me.vekster.lightanticheat.util.hook.server.folia.FoliaUtil;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ExternalNPCUtil {

    private static Set<UUID> uuids = ConcurrentHashMap.newKeySet();
    private static Set<Integer> ids = ConcurrentHashMap.newKeySet();

    public static void loadExternalNPCUtil() {
        if (!FoliaUtil.isFolia())
            Scheduler.runTaskTimerAsynchronously(ExternalNPCUtil::updateUuids, 1, 1);
        else
            Scheduler.runTaskTimer(ExternalNPCUtil::updateUuids, 1, 1);
    }

    private static void updateUuids() {
        Set<UUID> uuids = ConcurrentHashMap.newKeySet();
        Set<Integer> ids = ConcurrentHashMap.newKeySet();
        for (Player player : Bukkit.getOnlinePlayers()) {
            uuids.add(player.getUniqueId());
            ids.add(player.getEntityId());
        }
        ExternalNPCUtil.uuids = uuids;
        ExternalNPCUtil.ids = ids;
    }

    @SecureAsync
    public static boolean isExternalNPC(Player player, boolean async) {
        if (player == null || player.getUniqueId() == null)
            return true;
        if (!async)
            return Bukkit.getPlayer(player.getUniqueId()) == null;
        return !uuids.contains(player.getUniqueId()) || !ids.contains(player.getEntityId());
    }

    @SecureAsync
    public static boolean isExternalNPC(Entity entity, boolean async) {
        if (entity == null || entity.getUniqueId() == null)
            return true;
        if (entity instanceof Player)
            return isExternalNPC((Player) entity, async);
        return false;
    }

}
