package me.vekster.lightanticheat.util.scheduler.gamescheduler;

import me.vekster.lightanticheat.util.folia.FoliaUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class FoliaScheduler implements GameScheduler {

    @Override
    public void runTask(boolean ignoreOnFolia, Runnable task) {
        if (!ignoreOnFolia) FoliaUtil.runTask(task);
        else task.run();
    }

    @Override
    public void runTaskAsynchronously(boolean ignoreOnFolia, Runnable task) {
        if (!ignoreOnFolia) FoliaUtil.runTaskAsynchronously(task);
        else task.run();
    }

    @Override
    public void runTaskLater(Runnable task, long delayInTicks) {
        FoliaUtil.runTaskLater(task, Math.max(1, delayInTicks));
    }

    @Override
    public void runTaskLater(Entity entity, Runnable task, long delayInTicks) {
        FoliaUtil.runTaskLater(entity, task, Math.max(1, delayInTicks));
    }

    @Override
    public void runTaskLaterAsynchronously(Runnable task, long delayInTicks) {
        FoliaUtil.runTaskLaterAsynchronously(task, Math.max(1, delayInTicks));
    }

    @Override
    public void runTaskTimer(Runnable task, long delayInTicks, long periodInTicks) {
        FoliaUtil.runTaskTimer(task, Math.max(1, delayInTicks), periodInTicks);
    }

    @Override
    public void runTaskTimer(Entity entity, Runnable task, long delayInTicks, long periodInTicks) {
        FoliaUtil.runTaskTimer(entity, task, Math.max(1, delayInTicks), periodInTicks);
    }

    @Override
    public void runTaskTimerAsynchronously(Runnable task, long delayInTicks, long periodInTicks) {
        FoliaUtil.runTaskTimerAsynchronously(task, Math.max(1, delayInTicks), periodInTicks);
    }

    @Override
    public void entityThread(Player player, Runnable task) {
        if (!FoliaUtil.isStable(player))
            return;
        FoliaUtil.runTask(player, task);
    }

    @Override
    public void entityThread(Player player, boolean force, Runnable task) {
        if (!force)
            entityThread(player, task);
        FoliaUtil.runTask(player, task);
    }

}
