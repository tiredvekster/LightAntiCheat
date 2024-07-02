package me.vekster.lightanticheat.util.hook.server.folia;

import com.tcoded.folialib.FoliaLib;
import me.vekster.lightanticheat.Main;
import me.vekster.lightanticheat.util.async.AsyncUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FoliaUtil {

    private static boolean folia;
    private static FoliaLib foliaLib;
    private static Map<UUID, List<FLocation>> players = new ConcurrentHashMap<>();

    public static void loadFoliaUtil() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;
        } catch (ClassNotFoundException e) {
            folia = false;
        }

        if (isFolia()) {
            foliaLib = new FoliaLib(Main.getInstance());

            runTaskTimer(() -> {
                Map<UUID, List<FLocation>> players = new ConcurrentHashMap<>();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    List<FLocation> locations = Collections.synchronizedList(new ArrayList<>(FoliaUtil.players
                            .getOrDefault(player.getUniqueId(), Collections.emptyList())));
                    if (locations.isEmpty()) {
                        FLocation[] fLocationArray = new FLocation[30];
                        Arrays.fill(fLocationArray, new FLocation(player));
                        locations = Collections.synchronizedList(new ArrayList<>(Arrays.asList(fLocationArray)));
                    }
                    locations.add(new FLocation(player));
                    locations.remove(0);
                    players.put(player.getUniqueId(), locations);
                }
                FoliaUtil.players = players;
            }, 1, 1);
        }
    }

    public static boolean isFolia() {
        return folia;
    }

    public static boolean isStable(Player player) {
        if (!isFolia())
            return true;
        List<FLocation> fLocations = players.getOrDefault(player.getUniqueId(), null);
        if (fLocations == null)
            return false;
        World world = AsyncUtil.getWorld(player);
        if (world == null) world = player.getWorld();
        String worldName = world.getName();
        Location location = player.getLocation();
        double x = location.getX();
        double z = location.getZ();
        FLocation prevFLoc = null;
        for (int i = fLocations.size() - 1; i >= 0; i--) {
            FLocation fLocation = fLocations.get(i);
            if (!fLocation.world.equals(worldName))
                return false;
            if (prevFLoc == null) {
                if (Math.sqrt(Math.pow(x - fLocation.x, 2) + Math.pow(z - fLocation.z, 2)) >= 32)
                    return false;
            } else {
                if (Math.sqrt(Math.pow(prevFLoc.x - fLocation.x, 2) + Math.pow(prevFLoc.z - fLocation.z, 2)) >= 32)
                    return false;
            }
            prevFLoc = fLocation;
        }
        return true;
    }

    public static void runTask(Runnable runnable) {
        foliaLib.getImpl().runNextTick(wrappedTask -> runnable.run());
    }

    public static void runTask(Entity entity, Runnable runnable) {
        foliaLib.getImpl().runAtEntity(entity, wrappedTask -> runnable.run());
    }

    public static void runTaskAsynchronously(Runnable runnable) {
        foliaLib.getImpl().runAsync(wrappedTask -> runnable.run());
    }


    public static void runTaskLater(Runnable runnable, long delayInTicks) {
        foliaLib.getImpl().runLater(runnable, delayInTicks);
    }

    public static void runTaskLater(Entity entity, Runnable runnable, long delayInTicks) {
        foliaLib.getImpl().runAtEntityLater(entity, runnable, delayInTicks);
    }

    public static void runTaskLaterAsynchronously(Runnable runnable, long delayInTicks) {
        foliaLib.getImpl().runLaterAsync(runnable, delayInTicks);
    }

    public static void runTaskTimer(Runnable task, long delayInTicks, long periodInTicks) {
        foliaLib.getImpl().runTimer(task, delayInTicks, periodInTicks);
    }

    public static void runTaskTimer(Entity entity, Runnable task, long delayInTicks, long periodInTicks) {
        foliaLib.getImpl().runAtEntityTimer(entity, task, delayInTicks, periodInTicks);
    }

    public static void runTaskTimerAsynchronously(Runnable task, long delayInTicks, long periodInTicks) {
        foliaLib.getImpl().runTimerAsync(task, delayInTicks, periodInTicks);
    }

    public static void teleportPlayer(Player player, Location location) {
        if (!isFolia()) player.teleport(location);
        else foliaLib.getImpl().teleportAsync(player, location);
    }

    public static class FLocation {
        public FLocation(Player player) {
            Location location = player.getLocation();
            World world1 = AsyncUtil.getWorld(player);
            if (world1 == null) world1 = player.getWorld();
            this.world = world1.getName();
            this.x = location.getX();
            this.z = location.getZ();
        }

        public String world;
        public double x;
        public double z;
    }

}
