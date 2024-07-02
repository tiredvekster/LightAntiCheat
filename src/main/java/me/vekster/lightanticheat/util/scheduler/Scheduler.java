package me.vekster.lightanticheat.util.scheduler;

import me.vekster.lightanticheat.util.hook.server.folia.FoliaUtil;
import me.vekster.lightanticheat.util.scheduler.gamescheduler.BukkitScheduler;
import me.vekster.lightanticheat.util.scheduler.gamescheduler.FoliaScheduler;
import me.vekster.lightanticheat.util.scheduler.gamescheduler.GameScheduler;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

public class Scheduler {

    private static final GameScheduler SCHEDULER;

    static {
        SCHEDULER = !FoliaUtil.isFolia() ? new BukkitScheduler() : new FoliaScheduler();
    }

    public static void runTask(boolean ignoreOnFolia, Runnable task) {
        SCHEDULER.runTask(ignoreOnFolia, task);
    }

    public static void runTaskAsynchronously(boolean ignoreOnFolia, Runnable task) {
        SCHEDULER.runTaskAsynchronously(ignoreOnFolia, task);
    }

    public static void runTaskLater(Runnable task, long delayInTicks) {
        SCHEDULER.runTaskLater(task, delayInTicks);
    }

    public static void runTaskLater(Entity entity, Runnable task, long delayInTicks) {
        SCHEDULER.runTaskLater(entity, task, delayInTicks);
    }

    public static void runTaskLaterAsynchronously(Runnable task, long delayInTicks) {
        SCHEDULER.runTaskLaterAsynchronously(task, delayInTicks);
    }

    public static void runTaskTimer(Runnable task, long delayInTicks, long periodInTicks) {
        SCHEDULER.runTaskTimer(task, delayInTicks, periodInTicks);
    }

    public static void runTaskTimer(Entity entity, Runnable task, long delayInTicks, long periodInTicks) {
        SCHEDULER.runTaskTimer(entity, task, delayInTicks, periodInTicks);
    }

    public static void runTaskTimerAsynchronously(Runnable task, long delayInTicks, long periodInTicks) {
        SCHEDULER.runTaskTimerAsynchronously(task, delayInTicks, periodInTicks);
    }

    public static void entityThread(Player player, Runnable task) {
        SCHEDULER.entityThread(player, task);
    }

    public static void entityThread(Player player, boolean force, Runnable task) {
        SCHEDULER.entityThread(player, force, task);
    }

    @SuppressWarnings("unchecked")
    public static <T> T entityThread(Player player, boolean force, T defaultValue, Supplier<Object> supplier) {
        CompletableFuture<Object> future = new CompletableFuture<>();
        entityThread(player, force, () -> {
            future.complete(supplier.get());
        });
        try {
            Object result = future.get(500, TimeUnit.MILLISECONDS);
            return result != null ? (T) result : defaultValue;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return defaultValue;
        }
    }

    public static void schedule(TimerTask task, long delayInMs) {
        new Timer().schedule(task, delayInMs);
    }

    public static void schedule(TimerTask task, Date date) {
        new Timer().schedule(task, date);
    }

    public static void scheduleAtFixedRate(TimerTask task, long delayInMs, long periodInMs) {
        new Timer().scheduleAtFixedRate(task, delayInMs, periodInMs);
    }

    public static void scheduleAtFixedRate(TimerTask task, Date date, long periodInMs) {
        new Timer().scheduleAtFixedRate(task, date, periodInMs);
    }

}
