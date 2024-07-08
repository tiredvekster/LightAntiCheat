package me.vekster.lightanticheat.check.checks.packet.badpackets;

import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.buffer.Buffer;
import me.vekster.lightanticheat.check.checks.packet.PacketCheck;
import me.vekster.lightanticheat.event.playerattack.LACPlayerAttackEvent;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.player.cache.PlayerCache;
import me.vekster.lightanticheat.util.hook.server.folia.FoliaUtil;
import me.vekster.lightanticheat.version.identifier.LACVersion;
import me.vekster.lightanticheat.version.identifier.VerIdentifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * No swing(arm) animation
 */
public class BadPacketsD extends PacketCheck implements Listener {
    public BadPacketsD() {
        super(CheckName.BADPACKETS_D);
    }

    @EventHandler
    public void onHit(LACPlayerAttackEvent event) {
        if (FoliaUtil.isFolia()) return;
        if (VerIdentifier.getVersion().isOlderOrEqualsTo(LACVersion.V1_8)) return;

        Player player = event.getPlayer();
        LACPlayer lacPlayer = event.getLacPlayer();
        PlayerCache cache = lacPlayer.cache;

        if (!isCheckAllowed(player, lacPlayer))
            return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lacPlayer.joinTime < 8 * 1000)
            return;

        Buffer buffer = getBuffer(player);
        if (currentTime - cache.lastSwingTime <= currentTime - buffer.getLong("lastHit") + 3500)
            return;

        if (currentTime - buffer.getLong("lastFlag") < 500)
            return;
        buffer.put("lastFlag", currentTime);

        callViolationEventIfRepeat(player, lacPlayer, event.getEvent(), buffer, 3000);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void afterHit(LACPlayerAttackEvent event) {
        if (FoliaUtil.isFolia()) return;
        if (VerIdentifier.getVersion().isOlderOrEqualsTo(LACVersion.V1_8)) return;
        Player player = event.getPlayer();
        if (!isCheckAllowed(player, event.getLacPlayer()))
            return;
        getBuffer(player).put("lastHit", System.currentTimeMillis());
    }

}
