package me.vekster.lightanticheat.check.buffer;

import me.vekster.lightanticheat.check.Check;
import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.util.hook.server.folia.FoliaUtil;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Buffer {

    public Buffer(Check check, UUID uuid) {
        this.checkName = Check.getCheckSetting(check).name;
        this.uuid = uuid;
        this.playerBuffer = getPlayerBuffer();
    }

    public Buffer(Check check, UUID uuid, boolean async) {
        this.checkName = Check.getCheckSetting(check).name;
        this.uuid = uuid;
        this.playerBuffer = getPlayerBuffer();
        this.async = async;
    }

    public Buffer(Check check, Player player) {
        this.checkName = Check.getCheckSetting(check).name;
        this.uuid = player.getUniqueId();
        this.playerBuffer = getPlayerBuffer();
    }

    public Buffer(Check check, Player player, boolean async) {
        this.async = async;
        this.checkName = Check.getCheckSetting(check).name;
        this.uuid = player.getUniqueId();
        this.playerBuffer = getPlayerBuffer();
    }

    public static final Map<CheckName, Map<UUID, PlayerBuffer>> BUFFERS;
    public static final Map<CheckName, Map<UUID, PlayerBuffer>> ASYNC_BUFFERS = new ConcurrentHashMap<>();

    CheckName checkName;
    UUID uuid;
    PlayerBuffer playerBuffer;
    boolean async = false;

    private static List<CheckName> checkNamesForCleaner = new ArrayList<>();
    private static List<CheckName> checkNamesForAsyncCleaner = Collections.synchronizedList(new ArrayList<>());

    static {
        BUFFERS = !FoliaUtil.isFolia() ? new HashMap<>() : new ConcurrentHashMap<>();
    }

    private PlayerBuffer getPlayerBuffer() {
        Map<UUID, PlayerBuffer> checkBuffer = getCheckBuffer();
        if (checkBuffer.containsKey(uuid))
            return checkBuffer.get(uuid);
        PlayerBuffer playerBuffer = new PlayerBuffer(async);
        checkBuffer.put(uuid, playerBuffer);
        return playerBuffer;
    }

    private Map<UUID, PlayerBuffer> getCheckBuffer() {
        Map<CheckName, Map<UUID, PlayerBuffer>> buffers = !async ? BUFFERS : ASYNC_BUFFERS;
        if (buffers.containsKey(checkName))
            return buffers.get(checkName);
        Map<UUID, PlayerBuffer> checkBuffer = !async ? new HashMap<>() : new ConcurrentHashMap<>();
        buffers.put(checkName, checkBuffer);
        return checkBuffer;
    }

    public static void loadBufferCleaner(long cacheTimeMils) {
        Scheduler.runTaskTimer(() -> {
            if (checkNamesForCleaner.isEmpty())
                checkNamesForCleaner = new ArrayList<>(Arrays.asList(CheckName.values()));
            CheckName checkNameToClean = checkNamesForCleaner.get(0);
            checkNamesForCleaner.remove(checkNameToClean);
            long time = System.currentTimeMillis();

            Map<UUID, PlayerBuffer> checkBuffer = BUFFERS.getOrDefault(checkNameToClean, null);
            if (checkBuffer != null && !checkBuffer.isEmpty())
                checkBuffer.entrySet().removeIf(entry -> time - entry.getValue().updated > cacheTimeMils);
        }, 1, 1);

        Scheduler.runTaskTimerAsynchronously(() -> {
            if (checkNamesForAsyncCleaner.isEmpty())
                checkNamesForAsyncCleaner = Collections.synchronizedList(new ArrayList<>(Arrays.asList(CheckName.values())));
            CheckName checkNameToClean = checkNamesForAsyncCleaner.get(0);
            checkNamesForAsyncCleaner.remove(checkNameToClean);
            long time = System.currentTimeMillis();

            Map<UUID, PlayerBuffer> asyncCheckBuffer = ASYNC_BUFFERS.getOrDefault(checkNameToClean, null);
            if (asyncCheckBuffer != null && !asyncCheckBuffer.isEmpty())
                asyncCheckBuffer.entrySet().removeIf(entry -> time - entry.getValue().updated > cacheTimeMils);
        }, 1, 1);
    }

    public boolean isExists(String key) {
        return playerBuffer.containsKey(key);
    }

    public Integer getInt(String key) {
        Object object = playerBuffer.getOrDefault(key, new PlayerBuffer.PlayerVariable(0)).object;
        if (!(object instanceof Integer))
            return 0;
        return (int) object;
    }

    public Long getLong(String key) {
        Object object = playerBuffer.getOrDefault(key, new PlayerBuffer.PlayerVariable(0L)).object;
        if (!(object instanceof Long))
            return 0L;
        return (long) object;
    }

    public Float getFloat(String key) {
        Object object = playerBuffer.getOrDefault(key, new PlayerBuffer.PlayerVariable(0.0F)).object;
        if (!(object instanceof Float))
            return 0.0F;
        return (float) object;
    }

    public Double getDouble(String key) {
        Object object = playerBuffer.getOrDefault(key, new PlayerBuffer.PlayerVariable(0.0)).object;
        if (!(object instanceof Double))
            return 0.0;
        else return (double) object;
    }

    public Boolean getBoolean(String key) {
        Object object = playerBuffer.getOrDefault(key, new PlayerBuffer.PlayerVariable(null)).object;
        if (!(object instanceof Boolean))
            return false;
        else return (boolean) object;
    }

    public String getString(String key) {
        PlayerBuffer.PlayerVariable playerVariable = playerBuffer.getOrDefault(key, null);
        if (playerVariable == null || !(playerVariable.object instanceof String))
            return null;
        return (String) playerVariable.object;
    }

    public Location getLocation(String key) {
        PlayerBuffer.PlayerVariable playerVariable = playerBuffer.getOrDefault(key, null);
        if (playerVariable == null || !(playerVariable.object instanceof Location))
            return null;
        return (Location) playerVariable.object;
    }

    public Block getBlock(String key) {
        PlayerBuffer.PlayerVariable playerVariable = playerBuffer.getOrDefault(key, null);
        if (playerVariable == null || !(playerVariable.object instanceof Block))
            return null;
        return (Block) playerVariable.object;
    }

    public Material getMaterial(String key) {
        PlayerBuffer.PlayerVariable playerVariable = playerBuffer.getOrDefault(key, null);
        if (playerVariable == null || !(playerVariable.object instanceof Material))
            return null;
        return (Material) playerVariable.object;
    }

    public UUID getUUID(String key) {
        PlayerBuffer.PlayerVariable playerVariable = playerBuffer.getOrDefault(key, null);
        if (playerVariable == null || !(playerVariable.object instanceof UUID))
            return null;
        return (UUID) playerVariable.object;
    }

    public Entity getEntity(String key) {
        PlayerBuffer.PlayerVariable playerVariable = playerBuffer.getOrDefault(key, null);
        if (playerVariable == null || !(playerVariable.object instanceof Entity))
            return null;
        return (Entity) playerVariable.object;
    }

    public void put(String key, Object object) {
        playerBuffer.put(key, new PlayerBuffer.PlayerVariable(object));
    }

}
