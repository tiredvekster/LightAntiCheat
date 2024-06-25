package me.vekster.lightanticheat.util.logger.text;

import me.vekster.lightanticheat.check.CheckSetting;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.util.async.AsyncUtil;
import me.vekster.lightanticheat.util.config.ConfigManager;
import me.vekster.lightanticheat.util.config.placeholder.PlaceholderConvertor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ComponentUtil {

    public static List<String> generateLines(String text, CheckSetting checkSetting, Player violator, LACPlayer violatorLacPlayer) {
        List<String> lines = Collections.synchronizedList(new LinkedList<>());
        boolean punishment = checkSetting.punishmentVio == violatorLacPlayer.violations.getViolations(checkSetting.name);
        Location location = violator.getLocation();

        //message
        lines.add(PlaceholderConvertor.swapAll(text, checkSetting, violator, violatorLacPlayer));

        //hover message
        String hoverMessage = punishment ? ConfigManager.Config.Alerts.BroadcastPunishments.onHover :
                ConfigManager.Config.Alerts.BroadcastViolations.onHover;
        if (!hoverMessage.isEmpty()) {
            hoverMessage = PlaceholderConvertor.swapAll(
                    PlaceholderConvertor.swapCoordinates(hoverMessage, violator, "#0"),
                    checkSetting, violator, violatorLacPlayer);
        }
        lines.add(hoverMessage);

        //click message
        String clickMessage = punishment ? ConfigManager.Config.Alerts.BroadcastPunishments.onClick :
                ConfigManager.Config.Alerts.BroadcastViolations.onClick;
        if (!clickMessage.isEmpty()) {
            World world = AsyncUtil.getWorld(violator);
            if (world == null) world = violator.getWorld();
            clickMessage = PlaceholderConvertor.swapPlayer(clickMessage.replaceAll(
                            "%teleport-location%", world.getName() + " " +
                                    location.getX() + " " + location.getY() + " " + location.getZ() + " " +
                                    location.getYaw() + " " + location.getPitch()),
                    violator);
        }
        lines.add(clickMessage);

        return lines;
    }

}
