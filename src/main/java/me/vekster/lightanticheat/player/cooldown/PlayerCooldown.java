package me.vekster.lightanticheat.player.cooldown;

import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.player.cache.entity.CachedEntity;
import me.vekster.lightanticheat.player.cooldown.element.CooldownElement;
import me.vekster.lightanticheat.player.cooldown.element.EntityDistance;
import org.bukkit.entity.Entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerCooldown {

    public final Map<CheckName, CooldownElement<Boolean>> CHECKS = new ConcurrentHashMap<>();
    public final Map<CheckName, Long> CHECK_EXECUTIONS = new ConcurrentHashMap<>();
    public final Map<String, CooldownElement<Boolean>> PERMISSIONS = new HashMap<>();
    public final Map<String, CooldownElement<Boolean>> ASYNC_PERMISSIONS = new ConcurrentHashMap<>();
    public final CooldownElement<Set<Entity>> ALL_ENTITIES = new CooldownElement<>(null, 0L);
    public final Map<EntityDistance, CooldownElement<Set<CachedEntity>>> NEARBY_ENTITIES = new ConcurrentHashMap<>();
    public final CooldownElement<Integer> PING = new CooldownElement<>(0, 0L);
    public final CooldownElement<Boolean> BEDROCK = new CooldownElement<>(false, 0L);

}
