package me.vekster.lightanticheat.check.checks.movement.flight;

import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.buffer.Buffer;
import me.vekster.lightanticheat.check.checks.movement.MovementCheck;
import me.vekster.lightanticheat.event.playermove.LACAsyncPlayerMoveEvent;
import me.vekster.lightanticheat.event.playerplaceblock.LACAsyncPlayerPlaceBlockEvent;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.player.cache.PlayerCache;
import me.vekster.lightanticheat.player.cache.history.HistoryElement;
import me.vekster.lightanticheat.util.async.AsyncUtil;
import me.vekster.lightanticheat.util.detection.CheckUtil;
import me.vekster.lightanticheat.util.hook.plugin.simplehook.EnchantsSquaredHook;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import me.vekster.lightanticheat.version.VerUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lift limiter
 */
public class FlightB extends MovementCheck implements Listener {


    public FlightB() {
        super(CheckName.FLIGHT_B);
    }

    private static final Map<Integer, Double> HEIGHT_LIMITS = new ConcurrentHashMap<>();

    @Override
    public boolean isConditionAllowed(Player player, LACPlayer lacPlayer, PlayerCache cache, boolean isClimbing, boolean isInWater,
                                      boolean isFlying, boolean isInsideVehicle, boolean isGliding, boolean isRiptiding) {
        if (isFlying || isInsideVehicle || isClimbing || isGliding || isRiptiding || isInWater)
            return false;
        if (cache.flyingTicks >= -10 || cache.climbingTicks >= -2 ||
                cache.glidingTicks >= -3 || cache.riptidingTicks >= -5)
            return false;
        long time = System.currentTimeMillis();
        return time - cache.lastInsideVehicle > 150 && time - cache.lastInWater > 150 &&
                time - cache.lastKnockback > 750 && time - cache.lastKnockbackNotVanilla > 3000 &&
                time - cache.lastWasFished > 4000 && time - cache.lastTeleport > 700 &&
                time - cache.lastRespawn > 500 && time - cache.lastEntityVeryNearby > 700 &&
                time - cache.lastBlockExplosion > 5000 && time - cache.lastEntityExplosion > 3000 &&
                time - cache.lastSlimeBlockVertical > 4000 && time - cache.lastSlimeBlockHorizontal > 2500 &&
                time - cache.lastHoneyBlockVertical > 2500 && time - cache.lastHoneyBlockHorizontal > 1500 &&
                time - cache.lastPowderSnowWalk > 750 &&
                time - cache.lastWasHit > 350 && time - cache.lastWasDamaged > 150 &&
                time - cache.lastKbVelocity > 1000 && time - cache.lastAirKbVelocity > 2000 &&
                time - cache.lastStrongKbVelocity > 5000 && time - cache.lastStrongAirKbVelocity > 15 * 1000 &&
                time - cache.lastFlight > 750 &&
                time - cache.lastGliding > 2000 && time - cache.lastRiptiding > 3500 &&
                time - cache.lastWindCharge > 1000 && time - cache.lastWindChargeReceive > 500 &&
                time - cache.lastWindBurst > 1500 && time - cache.lastWindBurstNotVanilla > 4000;
    }

    @EventHandler
    public void onAsyncMovement(LACAsyncPlayerMoveEvent event) {
        LACPlayer lacPlayer = event.getLacPlayer();
        PlayerCache cache = lacPlayer.cache;
        Player player = event.getPlayer();
        Buffer buffer = getBuffer(player, true);

        boolean isInteractiveBlock = false;
        for (Block block : getInteractiveBlocks(player, event.getTo())) {
            if (!isActuallyPassable(block) && getAngle(player, block) <= 100) {
                isInteractiveBlock = true;
                break;
            }
        }
        if (isInteractiveBlock) {
            if (buffer.getInt("interactiveOffset") == 0) {
                buffer.put("interactiveOffset", 1);
                Block block = AsyncUtil.getBlock(event.getTo());
                if (block != null) {
                    buffer.put("interactiveBlock", block);
                } else {
                    buffer.put("flightTicks", 0);
                    updateStartLocation(event.getTo(), false, 0.5, buffer);
                    buffer.put("interactiveOffset", 0);
                    return;
                }
            } else if (buffer.getBlock("interactiveBlock") != null) {
                Block block = AsyncUtil.getBlock(event.getTo());
                if (block == null) {
                    buffer.put("flightTicks", 0);
                    updateStartLocation(event.getTo(), false, 0.5, buffer);
                    buffer.put("interactiveOffset", 0);
                    return;
                }
                Block previousBlock = buffer.getBlock("interactiveBlock");
                if (previousBlock.getX() != block.getX() || previousBlock.getZ() != block.getZ()) {
                    if (previousBlock.getY() < block.getY()) {
                        buffer.put("interactiveOffset", buffer.getInt("interactiveOffset") + 1);
                        buffer.put("interactiveBlock", block);
                    }
                }
            }
        }

        if (!isCheckAllowed(player, lacPlayer, true)) {
            buffer.put("flightTicks", 0);
            updateStartLocation(event.getTo(), false, 0.5, buffer);
            buffer.put("interactiveOffset", 0);
            return;
        }

        if (!isConditionAllowed(player, lacPlayer, event)) {
            buffer.put("flightTicks", 0);
            updateStartLocation(event.getTo(), false, 0.5, buffer);
            buffer.put("interactiveOffset", 0);
            return;
        }

        if (!event.isToWithinBlocksPassable() || !event.isFromWithinBlocksPassable()) {
            buffer.put("flightTicks", 0);
            updateStartLocation(event.getTo(), false, 0.5, buffer);
            buffer.put("interactiveOffset", 0);
            return;
        }

        long currentTime = System.currentTimeMillis();

        if (currentTime - cache.lastEntityNearby <= 1000) {
            buffer.put("flightTicks", 0);
            updateStartLocation(event.getTo(), false, 0.5, buffer);
            buffer.put("interactiveOffset", 0);
            return;
        }

        if (currentTime - buffer.getLong("effectTime") <= 2000) {
            buffer.put("flightTicks", 0);
            updateStartLocation(event.getTo(), false, 1.0, buffer);
            buffer.put("interactiveOffset", 0);
            return;
        }

        if (distanceAbsVertical(event.getFrom(), event.getTo()) < CheckUtil.LOWEST_BLOCK_HEIGHT &&
                cache.history.onEvent.onGround.get(HistoryElement.FROM).towardsFalse)
            buffer.put("lastVelocityTime", 0);
        long velocityBypass = 750L + 1500L * getEffectAmplifier(cache, VerUtil.potions.get("SLOW_FALLING"));
        if (currentTime - buffer.getLong("lastVelocityTime") < velocityBypass) {
            buffer.put("flightTicks", 0);
            updateStartLocation(event.getTo(), false, 1.0, buffer);
            buffer.put("interactiveOffset", 0);
            return;
        }

        if (cache.history.onEvent.onGround.get(HistoryElement.FROM).towardsTrue ||
                cache.history.onPacket.onGround.get(HistoryElement.FROM).towardsTrue) {
            updateStartLocation(event.getTo(), false, 0.0, buffer);
            buffer.put("flightTicks", 0);
            buffer.put("interactiveOffset", 0);
            return;
        }

        if (cache.history.onEvent.onGround.get(HistoryElement.FIRST).towardsTrue ||
                cache.history.onPacket.onGround.get(HistoryElement.FIRST).towardsTrue) {
            buffer.put("flightTicks", 0);
            buffer.put("interactiveOffset", 0);
            return;
        }

        if (event.getFrom().getBlockY() > event.getTo().getBlockY() ||
                event.getFrom().getY() > event.getTo().getY() && getBlockY(event.getTo().getY()) == 0) {
            if (!event.isToDownBlocksPassable()) {
                buffer.put("flightTicks", 0);
                updateStartLocation(event.getTo(), false, 0.0, buffer);
                buffer.put("interactiveOffset", 0);
                return;
            }
            for (Block block : event.getToDownBlocks()) {
                if (!isActuallyPassable(block.getRelative(BlockFace.DOWN))) {
                    buffer.put("flightTicks", 0);
                    updateStartLocation(event.getTo(), false, 0.0, buffer);
                    buffer.put("interactiveOffset", 0);
                    return;
                }
            }
        }

        if (getEffectAmplifier(cache, PotionEffectType.JUMP) == 0 &&
                currentTime - buffer.getLong("justEffectTime") < 100) {
            updateStartLocation(event.getTo(), false, 0.0, buffer);
            buffer.put("flightTicks", 0);
            buffer.put("interactiveOffset", 0);
        }

        buffer.put("flightTicks", buffer.getInt("flightTicks") + 1);
        if (buffer.getInt("flightTicks") <= 1 || buffer.getLocation("startLocation") == null)
            return;

        if (currentTime - buffer.getLong("lastScaffoldPlace") <= 400L) {
            buffer.put("flightTicks", 0);
            updateStartLocation(event.getTo(), false, 0.0, buffer);
            return;
        }

        double height = distanceVertical(buffer.getLocation("startLocation"), event.getTo());
        int jumpEffectAmplifier = getEffectAmplifier(cache, PotionEffectType.JUMP);
        if (jumpEffectAmplifier > 2)
            height -= (jumpEffectAmplifier - 2) * 0.2;
        height = height * 0.9 - 0.1 - buffer.getInt("interactiveOffset");
        double maxHeight = HEIGHT_LIMITS.getOrDefault(jumpEffectAmplifier, Double.MAX_VALUE);
        if (height <= maxHeight)
            return;

        Set<Player> players = getPlayersForEnchantsSquared(lacPlayer, player);
        updateDownBlocks(player, lacPlayer, event.getToDownBlocks());
        double finalHeight = height;
        Scheduler.runTask(true, () -> {
            if (currentTime - buffer.getLong("lastScaffoldPlace") <= 400L ||
                    lacPlayer.isGliding() || lacPlayer.isRiptiding()) {
                buffer.put("flightTicks", 0);
                updateStartLocation(event.getTo(), false, 0.0, buffer);
                return;
            }

            if (isLagGlidingPossible(player, buffer)) {
                buffer.put("lastGlidingLagPossibleTime", System.currentTimeMillis());
                return;
            }
            if (isPingGlidingPossible(player, cache))
                return;

            if (EnchantsSquaredHook.hasEnchantment(player, "Burden") &&
                    finalHeight * 0.9 - 0.5 <= maxHeight)
                return;
            if (isEnchantsSquaredImpact(players) && finalHeight * 0.7 - 1.8 <= maxHeight)
                return;

            if (System.currentTimeMillis() - buffer.getLong("lastGlidingLagPossibleTime") < 5 * 1000)
                callViolationEventIfRepeat(player, lacPlayer, event, buffer, 500);
            else
                callViolationEventIfRepeat(player, lacPlayer, event, buffer, 900);
        });
    }

    private void updateStartLocation(Location location, boolean force, double lift, Buffer buffer) {
        location.add(0, lift, 0);
        Location startLocation = buffer.getLocation("startLocation");
        if (force || startLocation == null) {
            buffer.put("startLocation", location);
            return;
        }
        if (location.getY() > startLocation.getY())
            buffer.put("startLocation", location);
    }

    private float getAngle(Player player, Block block) {
        Location blockLocation = block.getLocation();
        Location eyeLocation = player.getEyeLocation();
        Vector vector = blockLocation.toVector().setY(0.0D).subtract(eyeLocation.toVector().setY(0.0D));
        return eyeLocation.getDirection().setY(0.0D).normalize().angle(vector.normalize()) * 57.2958F;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void beforeMovement(LACAsyncPlayerMoveEvent event) {
        LACPlayer lacPlayer = event.getLacPlayer();
        Player player = event.getPlayer();

        if (!isCheckAllowed(player, lacPlayer, true))
            return;

        Buffer buffer = getBuffer(player, true);

        if (lacPlayer.cache.history.onEvent.onGround.get(HistoryElement.FROM).towardsTrue ||
                lacPlayer.cache.history.onPacket.onGround.get(HistoryElement.FROM).towardsTrue)
            updateStartLocation(event.getTo(), true, 0, buffer);


        if (getEffectAmplifier(lacPlayer.cache, VerUtil.potions.get("LEVITATION")) > 0 ||
                getEffectAmplifier(lacPlayer.cache, PotionEffectType.JUMP) > 32)
            buffer.put("effectTime", System.currentTimeMillis());

        if (getEffectAmplifier(lacPlayer.cache, PotionEffectType.JUMP) != 0)
            buffer.put("justEffectTime", System.currentTimeMillis());
    }

    @EventHandler
    public void scaffoldAsyncBlockPlace(LACAsyncPlayerPlaceBlockEvent event) {
        if (isActuallyPassable(event.getBlock()))
            return;
        Block placedBlock = event.getBlock();
        boolean within = false;
        for (Block block : getWithinBlocks(event.getPlayer())) {
            if (!equals(placedBlock, block) &&
                    !equals(placedBlock, block.getRelative(BlockFace.DOWN)))
                continue;
            within = true;
            break;
        }
        if (!within)
            return;
        Buffer buffer = getBuffer(event.getPlayer(), true);
        buffer.put("lastScaffoldPlace", System.currentTimeMillis());
    }

    private static boolean equals(Block block1, Block block2) {
        return block1.getX() == block2.getX() &&
                block1.getY() == block2.getY() &&
                block1.getZ() == block2.getZ();
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        LACPlayer lacPlayer = LACPlayer.getLacPlayer(player);
        if (lacPlayer != null) {
            if (!isCheckAllowed(player, lacPlayer, true))
                return;
            Buffer buffer = getBuffer(player, true);
            updateStartLocation(event.getPlayer().getLocation(), true, 0.5, buffer);
        } else {
            Scheduler.runTaskLater(() -> {
                if (!player.isOnline()) return;
                LACPlayer lacPlayer1 = LACPlayer.getLacPlayer(player);
                if (lacPlayer1 == null) return;
                if (!isCheckAllowed(player, lacPlayer1, true))
                    return;
                Buffer buffer = getBuffer(player, true);
                updateStartLocation(event.getPlayer().getLocation(), false, 0.5, buffer);
            }, 1);
        }
    }

    @EventHandler
    public void onVelocity(PlayerVelocityEvent event) {
        if (CheckUtil.isExternalNPC(event)) return;
        double yVelocity = event.getVelocity().getY();
        if (yVelocity < -0.0784000015258789 + 0.005 &&
                yVelocity > -0.0784000015258789 - 0.005)
            return;
        Buffer buffer = getBuffer(event.getPlayer(), true);
        buffer.put("lastVelocityTime", System.currentTimeMillis());
    }

    static {
        HEIGHT_LIMITS.put(0, 1.2491870787446828);
        HEIGHT_LIMITS.put(1, 1.8229914290304237);
        HEIGHT_LIMITS.put(2, 2.495504135427751);
        HEIGHT_LIMITS.put(3, 3.262979208689927);
        HEIGHT_LIMITS.put(4, 4.121780657759501);
        HEIGHT_LIMITS.put(5, 5.068380817939499);
        HEIGHT_LIMITS.put(6, 6.09935681691951);
        HEIGHT_LIMITS.put(7, 7.211386509209021);
        HEIGHT_LIMITS.put(8, 8.401247360588883);
        HEIGHT_LIMITS.put(9, 9.665813316670295);
        HEIGHT_LIMITS.put(10, 11.00205067446683);
        HEIGHT_LIMITS.put(11, 12.40702172138586);
        HEIGHT_LIMITS.put(12, 13.877873208335501);
        HEIGHT_LIMITS.put(13, 15.411839932915072);
        HEIGHT_LIMITS.put(14, 17.00623904858685);
        HEIGHT_LIMITS.put(15, 18.600076567816984);
        HEIGHT_LIMITS.put(16, 20.320436142409292);
        HEIGHT_LIMITS.put(17, 22.09475148051476);
        HEIGHT_LIMITS.put(18, 23.920602111211082);
        HEIGHT_LIMITS.put(19, 25.795652703139638);
        HEIGHT_LIMITS.put(20, 27.71764149877518);
        HEIGHT_LIMITS.put(21, 29.638742958312946);
        HEIGHT_LIMITS.put(22, 31.66704373015527);
        HEIGHT_LIMITS.put(23, 33.73672487314393);
        HEIGHT_LIMITS.put(24, 35.84575686484723);
        HEIGHT_LIMITS.put(25, 36.42330700836513);
        HEIGHT_LIMITS.put(26, 40.15228283711707);
        HEIGHT_LIMITS.put(27, 42.390953289857094);
        HEIGHT_LIMITS.put(28, 44.6078684568749);
        HEIGHT_LIMITS.put(29, 46.93469415176959);
        HEIGHT_LIMITS.put(30, 49.29112651218843);
        HEIGHT_LIMITS.put(31, 51.618257282853605);
        HEIGHT_LIMITS.put(32, 54.05607596071225);
    }

}
