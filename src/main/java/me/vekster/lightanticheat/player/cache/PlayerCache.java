package me.vekster.lightanticheat.player.cache;

import me.vekster.lightanticheat.event.playermove.blockcache.BlockCache;
import me.vekster.lightanticheat.player.cache.entity.CachedEntity;
import me.vekster.lightanticheat.player.cache.history.PlayerCacheHistory;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerCache {

    public PlayerCache(Player player) {
        history = new History(player);
        fromBlockCache = new BlockCache(player, player.getLocation());
    }

    public long lastWasDamaged;
    public long lastWasHit;
    public long lastWasFished;
    public Vector vectorOnWasFished;
    public long lastKbVelocity;
    public Vector vectorOnKbVelocity;
    public long lastAirKbVelocity;
    public Vector vectorOnAirKbVelocity;
    public long lastStrongKbVelocity;
    public Vector vectorOnStrongKbVelocity;
    public long lastStrongAirKbVelocity;
    public Vector vectorOnStrongAirKbVelocity;
    public long lastKnockback;
    public Vector vectorOnKnockback;
    public long lastKnockbackNotVanilla;
    public Vector vectorOnKnockbackNotVanilla;
    public long lastBlockPlace;
    public long lastBlockBreak;
    public long lastTeleport;
    public long lastWorldChange;
    public long lastGamemodeChange;
    public long lastRespawn;
    public long lastFireworkBoost;
    public long lastFireworkBoostNotVanilla;
    public long lastSlimeBlock;
    public Vector vectorOnSlimeBlock;
    public long lastSlimeBlockVertical;
    public Vector vectorOnSlimeBlockVertical;
    public long lastSlimeBlockHorizontal;
    public Vector vectorOnSlimeBlockHorizontal;
    public long lastHoneyBlock;
    public Vector vectorOnHoneyBlock;
    public long lastHoneyBlockVertical;
    public Vector vectorOnHoneyBlockVertical;
    public long lastHoneyBlockHorizontal;
    public Vector vectorOnHoneyBlockHorizontal;
    public long lastBlockExplosion;
    public Vector vectorOnBlockExplosion;
    public long lastEntityExplosion;
    public Vector vectorOnEntityExplosion;
    public long lastWindCharge;
    public long lastWindChargeReceive;
    public long lastWindBurst;
    public long lastWindBurstNotVanilla;
    public long lastGliding;
    public long lastRiptiding;
    public long lastFlight;
    public long lastPowderSnowWalk;
    public long lastHitTime;
    public long lastSwingTime;
    public long lastInsideVehicle;
    public long lastInWater;

    public int sneakingTicks;
    public int sprintingTicks;
    public int swimmingTicks;
    public int climbingTicks;
    public int glidingTicks;
    public int riptidingTicks;
    public int flyingTicks;
    public int blockingTicks;

    public Set<CachedEntity> entitiesVeryNearby = ConcurrentHashMap.newKeySet();
    public long lastEntityVeryNearby;
    public Set<CachedEntity> entitiesNearby = ConcurrentHashMap.newKeySet();
    public long lastEntityNearby;
    public Map<PotionEffectType, PotionEffect> potionEffects = new ConcurrentHashMap<>();

    public History history;

    public BlockCache fromBlockCache;

    public boolean alerts = true;

    public static class History {
        public History(Player player) {
            onEvent.location = new PlayerCacheHistory<>(player.getLocation());
            onEvent.onGround = new PlayerCacheHistory<>(new OnGround(true, false));
            onPacket.location = new PlayerCacheHistory<>(player.getLocation());
            onPacket.onGround = new PlayerCacheHistory<>(new OnGround(true, false));
        }

        public OnEvent onEvent = new OnEvent();
        public OnPacket onPacket = new OnPacket();

        public static class OnEvent {
            public PlayerCacheHistory<Location> location;
            public PlayerCacheHistory<OnGround> onGround;
        }

        public static class OnPacket {
            public PlayerCacheHistory<Location> location;
            public PlayerCacheHistory<OnGround> onGround;
        }
    }

    public static class OnGround {
        public OnGround(boolean towardsFalse, boolean towardsTrue) {
            this.towardsFalse = towardsFalse;
            this.towardsTrue = towardsTrue;
        }

        public boolean towardsFalse;
        public boolean towardsTrue;
    }

}
