package me.vekster.lightanticheat.check.checks.movement.elytra;

import me.vekster.lightanticheat.Main;
import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.buffer.Buffer;
import me.vekster.lightanticheat.check.checks.movement.MovementCheck;
import me.vekster.lightanticheat.event.playermove.LACAsyncPlayerMoveEvent;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.player.cache.PlayerCache;
import me.vekster.lightanticheat.player.cache.history.HistoryElement;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import me.vekster.lightanticheat.version.VerUtil;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fast takeoff without a firework
 */
public class ElytraC extends MovementCheck implements Listener {
    public ElytraC() {
        super(CheckName.ELYTRA_C);
    }

    private static final Map<Integer, Double> TICK_SPEEDS = new ConcurrentHashMap<>();
    private static final Map<Integer, Double> EVENT_SPEEDS = new ConcurrentHashMap<>();

    @Override
    public boolean isConditionAllowed(Player player, LACPlayer lacPlayer, PlayerCache cache, boolean isClimbing, boolean isInWater,
                                      boolean isFlying, boolean isInsideVehicle, boolean isGliding, boolean isRiptiding) {
        if (isFlying || isInsideVehicle || isClimbing || !isGliding || isRiptiding || isInWater)
            return false;
        if (cache.flyingTicks >= -5 || cache.climbingTicks >= -2 || cache.glidingTicks <= 3)
            return false;
        long time = System.currentTimeMillis();
        return time - cache.lastInsideVehicle > 150 && time - cache.lastInWater > 150 &&
                time - cache.lastKnockback > 750 && time - cache.lastKnockbackNotVanilla > 3000 &&
                time - cache.lastWasFished > 4000 && time - cache.lastTeleport > 500 &&
                time - cache.lastRespawn > 500 && time - cache.lastEntityVeryNearby > 700 &&
                time - cache.lastBlockExplosion > 8000 && time - cache.lastEntityExplosion > 3000 &&
                time - cache.lastSlimeBlockVertical > 6000 && time - cache.lastSlimeBlockHorizontal > 6000 &&
                time - cache.lastHoneyBlockVertical > 2500 && time - cache.lastHoneyBlockHorizontal > 2500 &&
                time - cache.lastFireworkBoost > 6000 && time - cache.lastFireworkBoostNotVanilla > 8000 &&
                time - cache.lastRiptiding > 15 * 1000 &&
                time - cache.lastWasHit > 350 && time - cache.lastWasDamaged > 150 &&
                time - cache.lastKbVelocity > 500 && time - cache.lastAirKbVelocity > 1000 &&
                time - cache.lastStrongKbVelocity > 2500 && time - cache.lastStrongAirKbVelocity > 5000 &&
                time - cache.lastFlight > 750;
    }

    @EventHandler
    public void onAsyncMovement(LACAsyncPlayerMoveEvent event) {
        LACPlayer lacPlayer = event.getLacPlayer();
        PlayerCache cache = lacPlayer.cache;
        Player player = event.getPlayer();
        Buffer buffer = getBuffer(player, true);

        if (!isCheckAllowed(player, lacPlayer, true)) {
            buffer.put("glidingEvents", 0);
            return;
        }

        if (!isConditionAllowed(player, lacPlayer, event)) {
            buffer.put("glidingEvents", 0);
            return;
        }

        if (!event.isToWithinBlocksPassable() || !event.isFromWithinBlocksPassable()) {
            buffer.put("glidingEvents", 0);
            return;
        }
        if (!event.isToDownBlocksPassable() || !event.isFromDownBlocksPassable()) {
            buffer.put("glidingEvents", 0);
            return;
        }

        if (cache.history.onEvent.onGround.get(HistoryElement.FROM).towardsTrue ||
                cache.history.onPacket.onGround.get(HistoryElement.FROM).towardsTrue)
            return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - buffer.getLong("effectTime") < 1000) {
            buffer.put("glidingEvents", 0);
            return;
        }

        if (event.getFrom().getBlockY() > event.getTo().getBlockY() ||
                event.getFrom().getY() > event.getTo().getY() && event.getTo().getY() % 1.0 == 0) {
            for (Block block : event.getToDownBlocks()) {
                Block downBlock = block.getRelative(BlockFace.DOWN);
                if (!isActuallyPassable(downBlock)) {
                    buffer.put("glidingEvents", 0);
                    return;
                }
            }
        }

        Set<Block> interactiveBlocks = new HashSet<>();
        getInteractiveBlocks(player, event.getTo()).forEach(block -> {
            interactiveBlocks.add(block);
            interactiveBlocks.add(block.getRelative(BlockFace.UP));
        });
        for (Block block : interactiveBlocks)
            if (!isActuallyPassable(block)) {
                buffer.put("glidingEvents", 0);
                return;
            }

        buffer.put("glidingEvents", buffer.getInt("glidingEvents") + 1);
        if (buffer.getInt("glidingEvents") <= 1)
            return;

        double maxTickSpeed = TICK_SPEEDS.getOrDefault(cache.glidingTicks, Double.MAX_VALUE);
        if (maxTickSpeed == Double.MAX_VALUE) return;
        double maxEventSpeed = EVENT_SPEEDS.getOrDefault(buffer.getInt("glidingEvents"), Double.MAX_VALUE);
        if (maxEventSpeed == Double.MAX_VALUE) return;

        double horizontalSpeed = distanceHorizontal(event.getFrom(), event.getTo());
        double averageHorizontalSpeed = distanceHorizontal(cache.history.onEvent.location.get(HistoryElement.FIRST), event.getTo()) / 2.0;

        if (Math.min(horizontalSpeed, averageHorizontalSpeed) < Math.max(maxTickSpeed, maxEventSpeed) * 1.6 + 0.35)
            return;

        Scheduler.runTask(true, () -> {
            callViolationEventIfRepeat(player, lacPlayer, event, buffer, Main.getBufferDurationMils() - 1000L);
        });
    }

    @EventHandler(priority = EventPriority.LOW)
    public void beforeMovement(LACAsyncPlayerMoveEvent event) {
        LACPlayer lacPlayer = event.getLacPlayer();
        Player player = event.getPlayer();

        if (!isCheckAllowed(player, lacPlayer, true))
            return;

        if (getEffectAmplifier(lacPlayer.cache, VerUtil.potions.get("LEVITATION")) > 0 ||
                getEffectAmplifier(lacPlayer.cache, VerUtil.potions.get("SLOW_FALLING")) > 0) {
            Buffer buffer = getBuffer(player, true);
            long currentTime = System.currentTimeMillis();
            buffer.put("effectTime", currentTime);
        }
    }

    static {
        TICK_SPEEDS.put(4, 0.44734879260319627);
        TICK_SPEEDS.put(5, 0.44734879260319627);
        TICK_SPEEDS.put(6, 0.44734879260319627);
        TICK_SPEEDS.put(7, 0.44734879260319627);
        TICK_SPEEDS.put(8, 0.44734879260319627);
        TICK_SPEEDS.put(9, 0.44734879260319627);
        TICK_SPEEDS.put(10, 0.44734879260319627);
        TICK_SPEEDS.put(11, 0.44734879260319627);
        TICK_SPEEDS.put(12, 0.44734879260319627);
        TICK_SPEEDS.put(13, 0.4678633848470381);
        TICK_SPEEDS.put(14, 0.4678633848470381);
        TICK_SPEEDS.put(15, 0.5096458131240956);
        TICK_SPEEDS.put(16, 0.5096458131240956);
        TICK_SPEEDS.put(17, 0.5096458131240956);
        TICK_SPEEDS.put(18, 0.5096458131240956);
        TICK_SPEEDS.put(19, 0.5096458131240956);
        TICK_SPEEDS.put(20, 0.5096458131240956);
        TICK_SPEEDS.put(21, 0.6363293271295004);
        TICK_SPEEDS.put(22, 0.6363293271295004);
        TICK_SPEEDS.put(23, 0.6363293271295004);
        TICK_SPEEDS.put(24, 0.6363293271295004);
        TICK_SPEEDS.put(25, 0.6363293271295004);
        TICK_SPEEDS.put(26, 0.6363293271295004);
        TICK_SPEEDS.put(27, 0.6363293271295004);
        TICK_SPEEDS.put(28, 0.7761990432739139);
        TICK_SPEEDS.put(29, 0.7761990432739139);
        TICK_SPEEDS.put(30, 0.7761990432739139);
        TICK_SPEEDS.put(31, 0.7761990432739139);
        TICK_SPEEDS.put(32, 0.7812136208841997);
        TICK_SPEEDS.put(33, 0.8276965891878098);
        TICK_SPEEDS.put(34, 0.8736567587535112);
        TICK_SPEEDS.put(35, 0.9183020882937782);
        TICK_SPEEDS.put(36, 0.9999682347314106);
        TICK_SPEEDS.put(37, 1.0135098587188913);
        TICK_SPEEDS.put(38, 1.0364071500995329);
        TICK_SPEEDS.put(39, 1.0696900826352207);
        TICK_SPEEDS.put(40, 1.100002594538592);
    }

    static {
        EVENT_SPEEDS.put(2, 0.27182645707616016);
        EVENT_SPEEDS.put(3, 0.27182645707616016);
        EVENT_SPEEDS.put(4, 0.27182645707616016);
        EVENT_SPEEDS.put(5, 0.27182645707616016);
        EVENT_SPEEDS.put(6, 0.27182645707616016);
        EVENT_SPEEDS.put(7, 0.27182645707616016);
        EVENT_SPEEDS.put(8, 0.27182645707616016);
        EVENT_SPEEDS.put(9, 0.27182645707616016);
        EVENT_SPEEDS.put(10, 0.27182645707616016);
        EVENT_SPEEDS.put(11, 0.27182645707616016);
        EVENT_SPEEDS.put(12, 0.28429297909853596);
        EVENT_SPEEDS.put(13, 0.29731067574460923);
        EVENT_SPEEDS.put(14, 0.3110668199157708);
        EVENT_SPEEDS.put(15, 0.3262354959638223);
        EVENT_SPEEDS.put(16, 0.3449602243391874);
        EVENT_SPEEDS.put(17, 0.3662675896438863);
        EVENT_SPEEDS.put(18, 0.3904261112530776);
        EVENT_SPEEDS.put(19, 0.416232566957176);
        EVENT_SPEEDS.put(20, 0.4431727632559902);
        EVENT_SPEEDS.put(21, 0.4716248088280485);
        EVENT_SPEEDS.put(22, 0.5016334162704484);
        EVENT_SPEEDS.put(23, 0.5329268013209907);
        EVENT_SPEEDS.put(24, 0.565231662539986);
        EVENT_SPEEDS.put(25, 0.600932554447271);
        EVENT_SPEEDS.put(26, 0.6414542902207322);
        EVENT_SPEEDS.put(27, 0.6868105839578575);
        EVENT_SPEEDS.put(28, 0.734397683958967);
        EVENT_SPEEDS.put(29, 0.7812136208841997);
        EVENT_SPEEDS.put(30, 0.8276965891878098);
        EVENT_SPEEDS.put(31, 0.8736567587535112);
        EVENT_SPEEDS.put(32, 0.9183020882937782);
        EVENT_SPEEDS.put(33, 0.9604544698562417);
        EVENT_SPEEDS.put(34, 0.9999682347314106);
        EVENT_SPEEDS.put(35, 1.0364071500995329);
        EVENT_SPEEDS.put(36, 1.0696900826352207);
        EVENT_SPEEDS.put(37, 1.100002594538592);
        EVENT_SPEEDS.put(38, 1.1278065520749856);
    }

}
