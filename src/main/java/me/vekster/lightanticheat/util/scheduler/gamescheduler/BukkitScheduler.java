package me.vekster.lightanticheat.util.scheduler.gamescheduler;

import me.vekster.lightanticheat.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class BukkitScheduler implements GameScheduler {

    @Override
    public void runTask(boolean ignoreOnFolia, Runnable task) {
        Bukkit.getScheduler().runTask(Main.getInstance(), task);
    }

    @Override
    public void runTaskAsynchronously(boolean ignoreOnFolia, Runnable task) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), task);
    }

    @Override
    public void runTaskLater(Runnable task, long delayInTicks) {
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), task, delayInTicks);
    }

    @Override
    public void runTaskLater(Entity entity, Runnable task, long delayInTicks) {
        runTaskLater(task, delayInTicks);
    }

    @Override
    public void runTaskLaterAsynchronously(Runnable task, long delayInTicks) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), task, delayInTicks);
    }

    @Override
    public void runTaskTimer(Runnable task, long delayInTicks, long periodInTicks) {
        Bukkit.getScheduler().runTaskTimer(Main.getInstance(), task, delayInTicks, periodInTicks);
    }

    @Override
    public void runTaskTimer(Entity entity, Runnable task, long delayInTicks, long periodInTicks) {
        runTaskTimer(task, delayInTicks, periodInTicks);
    }

    @Override
    public void runTaskTimerAsynchronously(Runnable task, long delayInTicks, long periodInTicks) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getInstance(), task, delayInTicks, periodInTicks);
    }

    @Override
    public void entityThread(Player player, Runnable task) {
        task.run();
    }

    @Override
    public void entityThread(Player player, boolean force, Runnable task) {
        task.run();
    }

}
