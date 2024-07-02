package me.vekster.lightanticheat.player;

import me.vekster.lightanticheat.player.cache.PlayerCache;
import me.vekster.lightanticheat.player.cooldown.PlayerCooldown;
import me.vekster.lightanticheat.player.violation.PlayerViolations;
import me.vekster.lightanticheat.util.hook.server.folia.FoliaUtil;
import me.vekster.lightanticheat.version.VerPlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LACPlayer extends VerPlayer {

    private LACPlayer(Player player) {
        super(player);
        this.uuid = player.getUniqueId();
        this.joinTime = System.currentTimeMillis();
        this.cache = new PlayerCache(player);
        this.cooldown = new PlayerCooldown();
        this.violations = new PlayerViolations();
        PLAYERS.put(uuid, this);
    }

    static {
        PLAYERS = !FoliaUtil.isFolia() ? new HashMap<>() : new ConcurrentHashMap<>();
    }

    protected static final Map<UUID, LACPlayer> PLAYERS;

    public UUID uuid;
    public long joinTime;
    public long leaveTime;

    public PlayerCache cache;
    public PlayerCooldown cooldown;
    public PlayerViolations violations;

    public static LACPlayer getLacPlayer(UUID uuid) {
        return PLAYERS.getOrDefault(uuid, null);
    }

    public static LACPlayer getLacPlayer(Player player) {
        return getLacPlayer(player.getUniqueId());
    }

    protected static void createLacPlayer(Player player) {
        new LACPlayer(player);
    }

    protected static void removeLacPlayer(UUID uuid) {
        PLAYERS.remove(uuid);
    }

}
