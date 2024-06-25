package me.vekster.lightanticheat.listener.invalidping;

import me.vekster.lightanticheat.Main;
import me.vekster.lightanticheat.event.packetrecive.LACAsyncPacketReceiveEvent;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class InvalidPingListener implements Listener {

    public static void limitMaxPing() {
        Bukkit.getPluginManager().registerEvents(new InvalidPingListener(), Main.getInstance());
    }

    @EventHandler
    public void onAsyncPacketReceive(LACAsyncPacketReceiveEvent event) {
        if (event.getLacPlayer().getPing(true) <= 10000)
            return;
        Scheduler.runTask(true, () -> {
            if (!event.getPlayer().isOnline())
                return;
            event.getPlayer().kickPlayer("Internal Exception: java.net.SocketException: Connection Reset");
        });
    }

}
