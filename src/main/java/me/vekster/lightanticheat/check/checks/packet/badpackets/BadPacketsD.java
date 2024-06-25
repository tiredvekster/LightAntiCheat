package me.vekster.lightanticheat.check.checks.packet.badpackets;

import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.buffer.Buffer;
import me.vekster.lightanticheat.check.checks.packet.PacketCheck;
import me.vekster.lightanticheat.event.playerattack.LACPlayerAttackEvent;
import me.vekster.lightanticheat.event.playerbreakblock.LACPlayerBreakBlockEvent;
import me.vekster.lightanticheat.event.playerplaceblock.LACPlayerPlaceBlockEvent;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.player.cache.PlayerCache;
import me.vekster.lightanticheat.util.folia.FoliaUtil;
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

        Player player = event.getPlayer();
        LACPlayer lacPlayer = event.getLacPlayer();
        PlayerCache cache = lacPlayer.cache;
        Buffer buffer = getBuffer(player);

        if (!isCheckAllowed(player, lacPlayer))
            return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lacPlayer.joinTime < 8 * 1000)
            return;

        long sinceLastHit = currentTime - buffer.getLong("lastHit");
        if (currentTime - cache.lastSwingTime <= 3500 + sinceLastHit)
            return;

        if (currentTime - buffer.getLong("lastFlag") < 500)
            return;
        buffer.put("lastFlag", currentTime);

        callViolationEventIfRepeat(player, lacPlayer, event.getEvent(), buffer, 3000);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void afterHit(LACPlayerAttackEvent event) {
        if (FoliaUtil.isFolia()) return;
        Player player = event.getPlayer();
        if (!isCheckAllowed(player, event.getLacPlayer()))
            return;
        getBuffer(player).put("lastHit", System.currentTimeMillis());
    }

    @EventHandler
    public void onBlockPlace(LACPlayerPlaceBlockEvent event) {
        if (FoliaUtil.isFolia()) return;
        Player player = event.getPlayer();
        LACPlayer lacPlayer = LACPlayer.getLacPlayer(player);
        PlayerCache cache = lacPlayer.cache;
        Buffer buffer = getBuffer(player);

        if (!isCheckAllowed(player, lacPlayer))
            return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lacPlayer.joinTime < 9 * 1000)
            return;

        long sinceLastPlace = currentTime - buffer.getLong("lastPlace");
        if (currentTime - cache.lastSwingTime <= 4000 + sinceLastPlace)
            return;

        if (currentTime - buffer.getLong("lastFlag") < 750)
            return;
        buffer.put("lastFlag", currentTime);

        callViolationEventIfRepeat(player, lacPlayer, event.getEvent(), buffer, 3500);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void afterBlockPlace(LACPlayerPlaceBlockEvent event) {
        if (FoliaUtil.isFolia()) return;
        Player player = event.getPlayer();
        if (!isCheckAllowed(player, LACPlayer.getLacPlayer(player)))
            return;
        getBuffer(player).put("lastPlace", System.currentTimeMillis());
    }

    @EventHandler
    public void onBlockBreak(LACPlayerBreakBlockEvent event) {
        if (FoliaUtil.isFolia()) return;
        if (VerIdentifier.getVersion().isOlderOrEqualsTo(LACVersion.V1_8)) return;
        Player player = event.getPlayer();
        LACPlayer lacPlayer = LACPlayer.getLacPlayer(player);
        PlayerCache cache = lacPlayer.cache;
        Buffer buffer = getBuffer(player);

        if (!isCheckAllowed(player, lacPlayer))
            return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lacPlayer.joinTime < 9 * 1000)
            return;

        long sinceLastBreak = currentTime - buffer.getLong("lastBreak");

        if (currentTime - cache.lastSwingTime <= 4500 + sinceLastBreak)
            return;

        if (currentTime - buffer.getLong("lastFlag") < 750)
            return;
        buffer.put("lastFlag", currentTime);

        if (System.currentTimeMillis() - buffer.getLong("lastBreakLvlFlag") < 5000) {
            buffer.put("lastBreakLvlFlag", System.currentTimeMillis());
            return;
        }

        callViolationEventIfRepeat(player, lacPlayer, event.getEvent(), buffer, 3000);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void afterBlockBreak(LACPlayerBreakBlockEvent event) {
        if (FoliaUtil.isFolia()) return;
        if (VerIdentifier.getVersion().isOlderOrEqualsTo(LACVersion.V1_8)) return;
        Player player = event.getPlayer();
        if (!isCheckAllowed(player, LACPlayer.getLacPlayer(player)))
            return;
        getBuffer(player).put("lastBreak", System.currentTimeMillis());
    }

}
