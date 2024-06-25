package me.vekster.lightanticheat.util.scheduler.gamescheduler;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public interface GameScheduler {

    void runTask(boolean ignoreOnFolia, Runnable task);

    void runTaskAsynchronously(boolean ignoreOnFolia, Runnable task);

    void runTaskLater(Runnable task, long delayInTicks);

    void runTaskLater(Entity entity, Runnable task, long delayInTicks);

    void runTaskLaterAsynchronously(Runnable task, long delayInTicks);

    void runTaskTimer(Runnable task, long delayInTicks, long periodInTicks);

    void runTaskTimer(Entity entity, Runnable task, long delayInTicks, long periodInTicks);

    void runTaskTimerAsynchronously(Runnable task, long delayInTicks, long periodInTicks);

    void entityThread(Player player, Runnable task);

    void entityThread(Player player, boolean force, Runnable task);

}
