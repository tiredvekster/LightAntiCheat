package me.vekster.lightanticheat.check.checks.movement.nofall;

import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.buffer.Buffer;
import me.vekster.lightanticheat.check.checks.movement.MovementCheck;
import me.vekster.lightanticheat.event.playerbreakblock.LACAsyncPlayerBreakBlockEvent;
import me.vekster.lightanticheat.event.playermove.LACAsyncPlayerMoveEvent;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.player.cache.PlayerCache;
import me.vekster.lightanticheat.player.cache.history.HistoryElement;
import me.vekster.lightanticheat.util.hook.plugin.simplehook.EnchantsSquaredHook;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import me.vekster.lightanticheat.version.VerUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Spoof of the fall distance
 */
public class NoFallA extends MovementCheck implements Listener {
    public NoFallA() {
        super(CheckName.NOFALL_A);
    }

    @Override
    public boolean isConditionAllowed(Player player, LACPlayer lacPlayer, PlayerCache cache, boolean isClimbing, boolean isInWater,
                                      boolean isFlying, boolean isInsideVehicle, boolean isGliding, boolean isRiptiding) {
        if (isFlying || isInsideVehicle || isClimbing || isGliding || isRiptiding || isInWater)
            return false;
        if (cache.flyingTicks >= -5 || cache.climbingTicks >= -2 ||
                cache.glidingTicks >= -3 || cache.riptidingTicks >= -5)
            return false;
        long time = System.currentTimeMillis();
        return time - cache.lastInsideVehicle > 150 && time - cache.lastInWater > 150 &&
                time - cache.lastKnockback > 750 && time - cache.lastKnockbackNotVanilla > 3000 &&
                time - cache.lastWasFished > 4000 && time - cache.lastTeleport > 900 &&
                time - cache.lastRespawn > 500 && time - cache.lastEntityVeryNearby > 700 &&
                time - cache.lastBlockExplosion > 4000 && time - cache.lastEntityExplosion > 2000 &&
                time - cache.lastSlimeBlockVertical > 2500 && time - cache.lastSlimeBlockHorizontal > 2500 &&
                time - cache.lastHoneyBlockVertical > 2500 && time - cache.lastHoneyBlockHorizontal > 2500 &&
                time - cache.lastWasHit > 350 && time - cache.lastWasDamaged > 150 &&
                time - cache.lastFlight > 750;
    }

    private static final Map<Integer, Float> EVENTS_JUMP_0 = new ConcurrentHashMap<>();
    private static final Map<Integer, Float> EVENTS_JUMP_1 = new ConcurrentHashMap<>();
    private static final Map<Integer, Float> EVENTS_JUMP_2 = new ConcurrentHashMap<>();
    private static final Map<Integer, Float> EVENTS_JUMP_3 = new ConcurrentHashMap<>();
    private static final Map<Integer, Float> EVENTS_JUMP_4 = new ConcurrentHashMap<>();
    private static final Map<Integer, Float> EVENTS_JUMP_5 = new ConcurrentHashMap<>();
    private static final Map<Double, Float> DISTANCE_JUMP_0 = Collections.synchronizedMap(new LinkedHashMap<>());
    private static final Map<Double, Float> DISTANCE_JUMP_1 = Collections.synchronizedMap(new LinkedHashMap<>());
    private static final Map<Double, Float> DISTANCE_JUMP_2 = Collections.synchronizedMap(new LinkedHashMap<>());
    private static final Map<Double, Float> DISTANCE_JUMP_3 = Collections.synchronizedMap(new LinkedHashMap<>());
    private static final Map<Double, Float> DISTANCE_JUMP_4 = Collections.synchronizedMap(new LinkedHashMap<>());
    private static final Map<Double, Float> DISTANCE_JUMP_5 = Collections.synchronizedMap(new LinkedHashMap<>());

    @EventHandler
    public void onAsyncMovement(LACAsyncPlayerMoveEvent event) {
        LACPlayer lacPlayer = event.getLacPlayer();
        PlayerCache cache = lacPlayer.cache;
        Player player = event.getPlayer();
        Buffer buffer = getBuffer(player, true);

        if (System.currentTimeMillis() - buffer.getLong("lastScaffoldBreak") > 5000)
            buffer.put("scaffoldBreaks", 0);

        if (!isCheckAllowed(player, lacPlayer, true)) {
            buffer.put("fallEvents", 0);
            buffer.put("fallStartLocation", null);
            return;
        }

        if (!isConditionAllowed(player, lacPlayer, event)) {
            buffer.put("fallEvents", 0);
            buffer.put("fallStartLocation", null);
            return;
        }

        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) {
            buffer.put("fallEvents", 0);
            buffer.put("fallStartLocation", null);
            return;
        }

        if (!event.isToWithinBlocksPassable() || !event.isFromWithinBlocksPassable()) {
            buffer.put("fallEvents", 0);
            buffer.put("fallStartLocation", null);
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lacPlayer.joinTime < 5000) {
            buffer.put("fallEvents", 0);
            buffer.put("fallStartLocation", null);
            return;
        }

        if (currentTime - cache.lastEntityNearby <= 3000) {
            buffer.put("fallEvents", 0);
            buffer.put("fallStartLocation", null);
            return;
        }

        if (currentTime - buffer.getLong("effectTime") < 1000) {
            buffer.put("fallEvents", 0);
            buffer.put("fallStartLocation", null);
            return;
        }

        Set<Block> accurateDownBlocks = distanceAbsVertical(event.getFrom(), event.getTo()) > distanceHorizontal(event.getFrom(), event.getTo()) * 2.0 ?
                getDownBlocks(player, event.getTo(), Double.MIN_VALUE * 100) : event.getToDownBlocks();
        for (Block block : accurateDownBlocks) {
            if (!isActuallyPassable(block)) {
                buffer.put("fallEvents", 0);
                buffer.put("fallStartLocation", null);
                return;
            }
        }

        if (event.getFrom().getBlockY() > event.getTo().getBlockY() ||
                event.getFrom().getY() > event.getTo().getY() && getBlockY(event.getTo().getY()) == 0) {
            for (Block block : accurateDownBlocks) {
                if (!isActuallyPassable(block.getRelative(BlockFace.DOWN))) {
                    buffer.put("fallEvents", 0);
                    buffer.put("fallStartLocation", null);
                    return;
                }
            }
        }

        for (int i = 0; i < 3 && i < HistoryElement.values().length; i++)
            if (cache.history.onEvent.onGround.get(HistoryElement.values()[i]).towardsTrue ||
                    cache.history.onPacket.onGround.get(HistoryElement.values()[i]).towardsTrue) {
                buffer.put("fallEvents", 0);
                buffer.put("fallStartLocation", null);
                return;
            }

        Location newerLocation = event.getTo();
        for (int i = 0; i < 3 && i < HistoryElement.values().length; i++) {
            Location location = cache.history.onEvent.location.get(HistoryElement.values()[i]);
            double vSpeed = distanceVertical(location, newerLocation);
            newerLocation = location;
            if (vSpeed > 0) {
                buffer.put("fallEvents", 0);
                buffer.put("fallStartLocation", null);
                return;
            }
        }

        buffer.put("fallEvents", buffer.getInt("fallEvents") + 1);

        if (buffer.getLocation("fallStartLocation") == null) {
            buffer.put("fallStartLocation", event.getFrom());
            return;
        }

        if (buffer.getInt("fallEvents") <= 1)
            return;

        int fallEvents = buffer.getInt("fallEvents");
        double fallDistance = distanceVertical(buffer.getLocation("fallStartLocation"), event.getFrom());
        float playerFallDistance = player.getFallDistance();

        Map<Integer, Float> eventMap;
        Map<Double, Float> distanceMap;
        int jumpEffectAmplifier = getEffectAmplifier(lacPlayer.cache, PotionEffectType.JUMP);
        switch (jumpEffectAmplifier) {
            case 0:
                eventMap = EVENTS_JUMP_0;
                distanceMap = DISTANCE_JUMP_0;
                break;
            case 1:
                eventMap = EVENTS_JUMP_1;
                distanceMap = DISTANCE_JUMP_1;
                break;
            case 2:
                eventMap = EVENTS_JUMP_2;
                distanceMap = DISTANCE_JUMP_2;
                break;
            case 3:
                eventMap = EVENTS_JUMP_3;
                distanceMap = DISTANCE_JUMP_3;
                break;
            case 4:
                eventMap = EVENTS_JUMP_4;
                distanceMap = DISTANCE_JUMP_4;
                break;
            default:
                eventMap = EVENTS_JUMP_5;
                distanceMap = DISTANCE_JUMP_5;
                break;
        }

        float calculatedFallDistanceByEvents = eventMap.getOrDefault(fallEvents, -1F);
        if (calculatedFallDistanceByEvents == -1F)
            return;

        float calculatedFallDistanceByDistance = -1F;
        for (Double distance : distanceMap.keySet())
            if (Math.abs(distance) >= Math.abs(fallDistance))
                calculatedFallDistanceByDistance = calculatedFallDistanceByDistance == -1F ?
                        distanceMap.get(distance) : Math.min(calculatedFallDistanceByDistance, distanceMap.get(distance));
        if (calculatedFallDistanceByDistance == -1F)
            return;
        float calculatedFallDistance = Math.min(calculatedFallDistanceByEvents, calculatedFallDistanceByDistance);

        if (distanceHorizontal(event.getFrom(), event.getTo()) * 2.0 > distanceAbsVertical(event.getFrom(), event.getTo())) {
            playerFallDistance += 0.3;
            if (calculatedFallDistance - playerFallDistance * 1.1 + 0.15 == 0.5770024597644806)
                return;
        }
        if (distanceHorizontal(event.getFrom(), event.getTo()) > distanceAbsVertical(event.getFrom(), event.getTo()))
            playerFallDistance += 0.7;
        if (buffer.getInt("scaffoldBreaks") != 0)
            playerFallDistance += buffer.getInt("scaffoldBreaks") * 1.1;
        if (cache.sneakingTicks <= 25)
            playerFallDistance += 0.5;
        if (System.currentTimeMillis() - buffer.getLong("interactiveBlockTime") < 250)
            playerFallDistance += 0.75;
        playerFallDistance = (float) (playerFallDistance * 1.2 + 0.35);
        if (playerFallDistance > calculatedFallDistance)
            return;

        Set<Player> players = getPlayersForEnchantsSquared(lacPlayer, player);
        updateDownBlocks(player, lacPlayer, event.getToDownBlocks());
        float finalPlayerFallDistance = playerFallDistance;
        Scheduler.runTask(true, () -> {
            if (EnchantsSquaredHook.hasEnchantment(player, "Burden") &&
                    finalPlayerFallDistance * 1.2 + 1.0 > calculatedFallDistance)
                return;

            if (isEnchantsSquaredImpact(players) && finalPlayerFallDistance * 1.5 + 1.2 > calculatedFallDistance)
                return;

            callViolationEventIfRepeat(player, lacPlayer, event, buffer, 1500);
        });
    }

    @EventHandler(priority = EventPriority.LOW)
    public void beforeMovement(LACAsyncPlayerMoveEvent event) {
        LACPlayer lacPlayer = event.getLacPlayer();
        Player player = event.getPlayer();

        if (!isCheckAllowed(player, lacPlayer, true))
            return;

        if (getEffectAmplifier(lacPlayer.cache, VerUtil.potions.get("LEVITATION")) > 0 ||
                getEffectAmplifier(lacPlayer.cache, VerUtil.potions.get("SLOW_FALLING")) > 0 ||
                getEffectAmplifier(lacPlayer.cache, PotionEffectType.JUMP) > 5) {
            Buffer buffer = getBuffer(player, true);
            buffer.put("effectTime", System.currentTimeMillis());
        }

        for (Block block : getInteractiveBlocks(player, event.getTo())) {
            if (isActuallyPassable(block) && isActuallyPassable(block.getRelative(BlockFace.DOWN)))
                continue;
            Buffer buffer = getBuffer(player, true);
            buffer.put("interactiveBlockTime", System.currentTimeMillis());
        }
    }

    @EventHandler
    public void scaffoldBlockBreak(LACAsyncPlayerBreakBlockEvent event) {
        if (isActuallyPassable(event.getBlock()))
            return;
        Block placedBlock = event.getBlock();
        boolean within = false;
        for (Block block : getWithinBlocks(event.getPlayer())) {
            if (!equals(placedBlock, block) &&
                    !equals(placedBlock, block.getRelative(BlockFace.DOWN)) &&
                    !equals(placedBlock, block.getRelative(BlockFace.UP)))
                continue;
            within = true;
            break;
        }
        if (!within)
            return;
        Buffer buffer = getBuffer(event.getPlayer(), true);
        buffer.put("lastScaffoldBreak", System.currentTimeMillis());
        buffer.put("scaffoldBreaks", buffer.getInt("scaffoldBreaks") + 1);
    }

    private static boolean equals(Block block1, Block block2) {
        return block1.getX() == block2.getX() &&
                block1.getY() == block2.getY() &&
                block1.getZ() == block2.getZ();
    }

    static {
        EVENTS_JUMP_0.put(2, 0.7570025F);
        EVENTS_JUMP_0.put(3, 1.1309066F);
        EVENTS_JUMP_0.put(4, 1.5757326F);
        EVENTS_JUMP_0.put(5, 2.0900621F);
        EVENTS_JUMP_0.put(6, 2.672505F);
        EVENTS_JUMP_0.put(7, 3.321699F);
        EVENTS_JUMP_0.put(8, 4.0363092F);
        EVENTS_JUMP_0.put(9, 4.815027F);
        EVENTS_JUMP_0.put(10, 5.656571F);
        EVENTS_JUMP_0.put(11, 6.559684F);
        EVENTS_JUMP_0.put(12, 7.523134F);
        EVENTS_JUMP_0.put(13, 8.545715F);
        EVENTS_JUMP_0.put(14, 9.6262455F);
        EVENTS_JUMP_0.put(15, 10.763565F);
        EVENTS_JUMP_0.put(16, 11.956538F);
        EVENTS_JUMP_0.put(17, 13.204052F);
        EVENTS_JUMP_0.put(18, 14.505015F);
        EVENTS_JUMP_0.put(19, 15.858359F);
        EVENTS_JUMP_0.put(20, 17.263037F);
        EVENTS_JUMP_0.put(21, 18.718021F);
        EVENTS_JUMP_0.put(22, 20.222305F);
        EVENTS_JUMP_0.put(23, 21.774904F);
        EVENTS_JUMP_0.put(24, 23.374851F);
        EVENTS_JUMP_0.put(25, 25.021198F);
        EVENTS_JUMP_0.put(26, 26.713018F);
        EVENTS_JUMP_0.put(27, 28.449402F);
        EVENTS_JUMP_0.put(28, 30.229458F);
        EVENTS_JUMP_0.put(29, 32.052315F);
        EVENTS_JUMP_0.put(30, 33.917114F);
        EVENTS_JUMP_0.put(31, 35.823017F);
        EVENTS_JUMP_0.put(32, 37.769203F);
        EVENTS_JUMP_0.put(33, 39.754864F);
        EVENTS_JUMP_0.put(34, 41.779213F);
        EVENTS_JUMP_0.put(35, 43.841473F);
        EVENTS_JUMP_0.put(36, 45.940887F);
        EVENTS_JUMP_0.put(37, 48.076714F);
        EVENTS_JUMP_0.put(38, 50.248226F);
        EVENTS_JUMP_0.put(39, 52.45471F);
        EVENTS_JUMP_0.put(40, 54.695457F);
        EVENTS_JUMP_0.put(41, 56.969795F);
        DISTANCE_JUMP_0.put(-0.22768848754498094, 0.7570025F);
        DISTANCE_JUMP_0.put(-0.23052736891296632, 0.7684762F);
        DISTANCE_JUMP_0.put(-0.5292232112077642, 1.1309066F);
        DISTANCE_JUMP_0.put(-0.5348441963705, 1.1451067F);
        DISTANCE_JUMP_0.put(-0.9031272476744903, 1.5757326F);
        DISTANCE_JUMP_0.put(-0.9114746946091543, 1.5926046F);
        DISTANCE_JUMP_0.put(-1.347953212069413, 2.0900621F);
        DISTANCE_JUMP_0.put(-1.3589725915925754, 2.1095526F);
        DISTANCE_JUMP_0.put(-1.8622826671866903, 2.672505F);
        DISTANCE_JUMP_0.put(-1.8759205406975497, 2.6945615F);
        DISTANCE_JUMP_0.put(-2.4447255445375617, 3.321699F);
        DISTANCE_JUMP_0.put(-2.4609295422063013, 3.3462703F);
        DISTANCE_JUMP_0.put(-3.0939195769765178, 4.0363092F);
        DISTANCE_JUMP_0.put(-3.1126383763689205, 4.063345F);
        DISTANCE_JUMP_0.put(-3.8085297426749634, 4.815027F);
        DISTANCE_JUMP_0.put(-3.8297130478045176, 4.844478F);
        DISTANCE_JUMP_0.put(-4.58724772021543, 5.656571F);
        DISTANCE_JUMP_0.put(-4.6108462410144, 5.688389F);
        DISTANCE_JUMP_0.put(-5.428791354583822, 6.559684F);
        DISTANCE_JUMP_0.put(-6.331904133841903, 7.523134F);
        DISTANCE_JUMP_0.put(-7.295354676266214, 8.545715F);
        DISTANCE_JUMP_0.put(-8.31793622774427, 9.6262455F);
        DISTANCE_JUMP_0.put(-9.398466169222843, 10.763565F);
        DISTANCE_JUMP_0.put(-10.53578553400719, 11.956538F);
        DISTANCE_JUMP_0.put(-11.728758534714373, 13.204052F);
        DISTANCE_JUMP_0.put(-12.97627209968745, 14.505015F);
        DISTANCE_JUMP_0.put(-14.277235418681371, 15.858359F);
        DISTANCE_JUMP_0.put(-15.63057949763521, 17.263037F);
        DISTANCE_JUMP_0.put(-17.035256722348834, 18.718021F);
        DISTANCE_JUMP_0.put(-18.49024043088616, 20.222305F);
        DISTANCE_JUMP_0.put(-19.994524494530225, 21.774904F);
        DISTANCE_JUMP_0.put(-21.54712290711923, 23.374851F);
        DISTANCE_JUMP_0.put(-23.147069382595802, 25.021198F);
        DISTANCE_JUMP_0.put(-24.793416960605285, 26.713018F);
        DISTANCE_JUMP_0.put(-26.485237619982044, 28.449402F);
        DISTANCE_JUMP_0.put(-28.221621899966067, 30.229458F);
        DISTANCE_JUMP_0.put(-30.001678528995186, 32.052315F);
        DISTANCE_JUMP_0.put(-31.82453406092148, 33.917114F);
        DISTANCE_JUMP_0.put(-33.68933251850335, 35.823017F);
        DISTANCE_JUMP_0.put(-35.59523504402766, 37.769203F);
        DISTANCE_JUMP_0.put(-37.54141955691958, 39.754864F);
        DISTANCE_JUMP_0.put(-39.52708041820006, 41.779213F);
        DISTANCE_JUMP_0.put(-41.55142810165428, 43.841473F);
        DISTANCE_JUMP_0.put(-43.61368887157667, 45.940887F);
        DISTANCE_JUMP_0.put(-45.713104466960985, 48.076714F);
        DISTANCE_JUMP_0.put(-47.84893179200667, 50.248226F);
        DISTANCE_JUMP_0.put(-50.02044261281499, 52.45471F);
        DISTANCE_JUMP_0.put(-52.22692326015131, 54.695457F);
        DISTANCE_JUMP_0.put(-54.46767433815205, 56.969795F);
    }

    static {
        EVENTS_JUMP_1.put(2, 0.7184915F);
        EVENTS_JUMP_1.put(3, 1.0832443F);
        EVENTS_JUMP_1.put(4, 1.5191021F);
        EVENTS_JUMP_1.put(5, 2.0246427F);
        EVENTS_JUMP_1.put(6, 2.5984726F);
        EVENTS_JUMP_1.put(7, 3.2392259F);
        EVENTS_JUMP_1.put(8, 3.945564F);
        EVENTS_JUMP_1.put(9, 4.7161756F);
        EVENTS_JUMP_1.put(10, 5.5497746F);
        EVENTS_JUMP_1.put(11, 6.4451017F);
        EVENTS_JUMP_1.put(12, 7.4009223F);
        EVENTS_JUMP_1.put(13, 8.416027F);
        EVENTS_JUMP_1.put(14, 9.489229F);
        EVENTS_JUMP_1.put(15, 10.619368F);
        EVENTS_JUMP_1.put(16, 11.805304F);
        EVENTS_JUMP_1.put(17, 13.04592F);
        EVENTS_JUMP_1.put(18, 14.340125F);
        EVENTS_JUMP_1.put(19, 15.686846F);
        EVENTS_JUMP_1.put(20, 17.085032F);
        EVENTS_JUMP_1.put(21, 18.533653F);
        EVENTS_JUMP_1.put(22, 20.031704F);
        EVENTS_JUMP_1.put(23, 21.578194F);
        EVENTS_JUMP_1.put(24, 23.172153F);
        EVENTS_JUMP_1.put(25, 24.812634F);
        EVENTS_JUMP_1.put(26, 26.498705F);
        EVENTS_JUMP_1.put(27, 28.229454F);
        EVENTS_JUMP_1.put(28, 30.003988F);
        EVENTS_JUMP_1.put(29, 31.821432F);
        EVENTS_JUMP_1.put(30, 33.680927F);
        EVENTS_JUMP_1.put(31, 35.58163F);
        EVENTS_JUMP_1.put(32, 37.52272F);
        EVENTS_JUMP_1.put(33, 39.503387F);
        EVENTS_JUMP_1.put(34, 41.522842F);
        EVENTS_JUMP_1.put(35, 43.580307F);
        EVENTS_JUMP_1.put(36, 45.675026F);
        EVENTS_JUMP_1.put(37, 47.806248F);
        EVENTS_JUMP_1.put(38, 49.973248F);
        EVENTS_JUMP_1.put(39, 52.175304F);
        EVENTS_JUMP_1.put(40, 54.41172F);
        EVENTS_JUMP_1.put(41, 56.68181F);
        DISTANCE_JUMP_1.put(-0.21815993781497411, 0.7184915F);
        DISTANCE_JUMP_1.put(-0.5103566825605981, 1.0832443F);
        DISTANCE_JUMP_1.put(-0.8751094995104012, 1.5191021F);
        DISTANCE_JUMP_1.put(-1.3109672686041876, 2.0246427F);
        DISTANCE_JUMP_1.put(-1.8165078921553146, 2.5984726F);
        DISTANCE_JUMP_1.put(-2.3903377144037137, 3.2392259F);
        DISTANCE_JUMP_1.put(-3.031090952677957, 3.945564F);
        DISTANCE_JUMP_1.put(-3.737429139933994, 4.7161756F);
        DISTANCE_JUMP_1.put(-4.508040578443129, 5.5497746F);
        DISTANCE_JUMP_1.put(-5.341639804406199, 6.4451017F);
        DISTANCE_JUMP_1.put(-6.236967063275529, 7.4009223F);
        DISTANCE_JUMP_1.put(-7.192787795570368, 8.416027F);
        DISTANCE_JUMP_1.put(-8.207892132976028, 9.489229F);
        DISTANCE_JUMP_1.put(-9.281094404521028, 10.619368F);
        DISTANCE_JUMP_1.put(-10.411232652630716, 11.805304F);
        DISTANCE_JUMP_1.put(-11.597168158859759, 13.04592F);
        DISTANCE_JUMP_1.put(-12.837784979110026, 14.340125F);
        DISTANCE_JUMP_1.put(-14.131989488144058, 15.686846F);
        DISTANCE_JUMP_1.put(-15.478709933208279, 17.085032F);
        DISTANCE_JUMP_1.put(-16.876895996583755, 18.533653F);
        DISTANCE_JUMP_1.put(-18.325518366885873, 20.031704F);
        DISTANCE_JUMP_1.put(-19.823568318938115, 21.578194F);
        DISTANCE_JUMP_1.put(-21.370057302048224, 23.172153F);
        DISTANCE_JUMP_1.put(-22.96401653651894, 24.812634F);
        DISTANCE_JUMP_1.put(-22.971079727060726, 25.083332F);
        DISTANCE_JUMP_1.put(-24.60449661822848, 26.498705F);
        DISTANCE_JUMP_1.put(-24.61917543321084, 26.776865F);
        DISTANCE_JUMP_1.put(-26.29056713111939, 28.229454F);
        DISTANCE_JUMP_1.put(-26.312709258198765, 28.514929F);
        DISTANCE_JUMP_1.put(-28.021316267437598, 30.003988F);
        DISTANCE_JUMP_1.put(-28.050772440514407, 30.29663F);
        DISTANCE_JUMP_1.put(-29.79585045556675, 31.821432F);
        DISTANCE_JUMP_1.put(-29.832474393860537, 32.121098F);
        DISTANCE_JUMP_1.put(-31.613293995305753, 33.680927F);
        DISTANCE_JUMP_1.put(-31.656942343648893, 33.987476F);
        DISTANCE_JUMP_1.put(-33.47278870044083, 35.58163F);
        DISTANCE_JUMP_1.put(-33.52332097076632, 35.894928F);
        DISTANCE_JUMP_1.put(-35.37349354846613, 37.52272F);
        DISTANCE_JUMP_1.put(-35.430772062465635, 37.84263F);
        DISTANCE_JUMP_1.put(-37.31458433730987, 39.503387F);
        DISTANCE_JUMP_1.put(-37.378474170238576, 39.829777F);
        DISTANCE_JUMP_1.put(-39.29525334892598, 41.522842F);
        DISTANCE_JUMP_1.put(-39.365622274531404, 41.855583F);
        DISTANCE_JUMP_1.put(-41.314709019613915, 43.580307F);
        DISTANCE_JUMP_1.put(-41.3914274561661, 43.919273F);
        DISTANCE_JUMP_1.put(-43.372175616932026, 45.675026F);
        DISTANCE_JUMP_1.put(-43.45511657433315, 46.02009F);
        DISTANCE_JUMP_1.put(-45.46689292307272, 47.806248F);
        DISTANCE_JUMP_1.put(-45.555931951024476, 48.157288F);
        DISTANCE_JUMP_1.put(-47.59811592457004, 49.973248F);
        DISTANCE_JUMP_1.put(-47.693131061777734, 50.330143F);
        DISTANCE_JUMP_1.put(-49.765114508213145, 52.175304F);
        DISTANCE_JUMP_1.put(-49.86598623260565, 52.53794F);
        DISTANCE_JUMP_1.put(-51.967173163041494, 54.41172F);
        DISTANCE_JUMP_1.put(-52.07378434298681, 54.779984F);
        DISTANCE_JUMP_1.put(-54.20359068830008, 56.68181F);
    }

    static {
        EVENTS_JUMP_2.put(2, 0.68749005F);
        EVENTS_JUMP_2.put(3, 1.0448761F);
        EVENTS_JUMP_2.put(4, 1.4735144F);
        EVENTS_JUMP_2.put(5, 1.9719801F);
        EVENTS_JUMP_2.put(6, 2.5388763F);
        EVENTS_JUMP_2.put(7, 3.1728346F);
        EVENTS_JUMP_2.put(8, 3.8725138F);
        EVENTS_JUMP_2.put(9, 4.6365995F);
        EVENTS_JUMP_2.put(10, 5.4638033F);
        EVENTS_JUMP_2.put(11, 6.3528633F);
        EVENTS_JUMP_2.put(12, 7.302542F);
        EVENTS_JUMP_2.put(13, 8.311627F);
        EVENTS_JUMP_2.put(14, 9.378931F);
        EVENTS_JUMP_2.put(15, 10.503288F);
        EVENTS_JUMP_2.put(16, 11.683558F);
        EVENTS_JUMP_2.put(17, 12.918623F);
        EVENTS_JUMP_2.put(18, 14.207387F);
        EVENTS_JUMP_2.put(19, 15.548776F);
        EVENTS_JUMP_2.put(20, 16.941736F);
        EVENTS_JUMP_2.put(21, 18.385239F);
        EVENTS_JUMP_2.put(22, 19.87827F);
        EVENTS_JUMP_2.put(23, 21.41984F);
        EVENTS_JUMP_2.put(24, 23.00898F);
        EVENTS_JUMP_2.put(25, 24.644737F);
        EVENTS_JUMP_2.put(26, 26.32618F);
        EVENTS_JUMP_2.put(27, 28.052393F);
        EVENTS_JUMP_2.put(28, 29.822481F);
        EVENTS_JUMP_2.put(29, 31.635569F);
        EVENTS_JUMP_2.put(30, 33.490795F);
        EVENTS_JUMP_2.put(31, 35.387318F);
        EVENTS_JUMP_2.put(32, 37.324306F);
        EVENTS_JUMP_2.put(33, 39.300957F);
        EVENTS_JUMP_2.put(34, 41.316475F);
        EVENTS_JUMP_2.put(35, 43.370083F);
        EVENTS_JUMP_2.put(36, 45.461018F);
        EVENTS_JUMP_2.put(37, 47.588535F);
        EVENTS_JUMP_2.put(38, 49.7519F);
        EVENTS_JUMP_2.put(39, 51.950397F);
        EVENTS_JUMP_2.put(40, 54.183327F);
        EVENTS_JUMP_2.put(41, 56.449997F);
        EVENTS_JUMP_2.put(42, 58.749733F);
        DISTANCE_JUMP_2.put(-0.21048942867385279, 0.68749005F);
        DISTANCE_JUMP_2.put(-0.28467964564102033, 1.0448761F);
        DISTANCE_JUMP_2.put(-0.30431682745754074, 1.1451067F);
        DISTANCE_JUMP_2.put(-0.4951690743148731, 1.0448761F);
        DISTANCE_JUMP_2.put(-0.6420657053249386, 1.4735144F);
        DISTANCE_JUMP_2.put(-0.8525551339987913, 1.4735144F);
        DISTANCE_JUMP_2.put(-1.2811934808315044, 1.9719801F);
        DISTANCE_JUMP_2.put(-1.3410763443270497, 2.6945615F);
        DISTANCE_JUMP_2.put(-1.3687835055982163, 4.844478F);
        DISTANCE_JUMP_2.put(-1.7796590704290765, 2.5388763F);
        DISTANCE_JUMP_2.put(-2.346555359268052, 3.1728346F);
        DISTANCE_JUMP_2.put(-2.980513734668804, 3.8725138F);
        DISTANCE_JUMP_2.put(-3.6801929561792264, 4.6365995F);
        DISTANCE_JUMP_2.put(-4.444278608130631, 5.4638033F);
        DISTANCE_JUMP_2.put(-5.271482563142669, 6.3528633F);
        DISTANCE_JUMP_2.put(-6.160542456358016, 7.302542F);
        DISTANCE_JUMP_2.put(-7.110221170192403, 8.311627F);
        DISTANCE_JUMP_2.put(-8.119306329389659, 9.378931F);
        DISTANCE_JUMP_2.put(-8.120194764257079, 9.667067F);
        DISTANCE_JUMP_2.put(-9.186609806175625, 10.503288F);
        DISTANCE_JUMP_2.put(-9.202907869620446, 10.806525F);
        DISTANCE_JUMP_2.put(-10.31096723530895, 11.683558F);
        DISTANCE_JUMP_2.put(-10.342366735053545, 12.001595F);
        DISTANCE_JUMP_2.put(-11.491237538830902, 12.918623F);
        DISTANCE_JUMP_2.put(-11.537436446437312, 13.251163F);
        DISTANCE_JUMP_2.put(-12.726302460320156, 14.207387F);
        DISTANCE_JUMP_2.put(-12.787004787913432, 14.554139F);
        DISTANCE_JUMP_2.put(-14.015066108462506, 15.548776F);
        DISTANCE_JUMP_2.put(-14.089981787919527, 15.909456F);
        DISTANCE_JUMP_2.put(-15.3564545097491, 16.941736F);
        DISTANCE_JUMP_2.put(-15.445299274303693, 17.316067F);
        DISTANCE_JUMP_2.put(-16.7494151701208, 18.385239F);
        DISTANCE_JUMP_2.put(-16.851910438336688, 18.772945F);
        DISTANCE_JUMP_2.put(-18.192916645379555, 19.87827F);
        DISTANCE_JUMP_2.put(-18.308789407443882, 20.279087F);
        DISTANCE_JUMP_2.put(-19.685948120191625, 21.41984F);
        DISTANCE_JUMP_2.put(-19.814930826482566, 21.833506F);
        DISTANCE_JUMP_2.put(-21.22751899551065, 23.00898F);
        DISTANCE_JUMP_2.put(-21.36934944739373, 23.435236F);
        DISTANCE_JUMP_2.put(-22.8166584842523, 24.644737F);
        DISTANCE_JUMP_2.put(-24.452415215055424, 26.32618F);
        DISTANCE_JUMP_2.put(-26.133856843967948, 28.052393F);
        DISTANCE_JUMP_2.put(-27.860069673899062, 29.822481F);
        DISTANCE_JUMP_2.put(-29.630158281682327, 31.635569F);
        DISTANCE_JUMP_2.put(-31.44324515259757, 33.490795F);
        DISTANCE_JUMP_2.put(-33.29847032220226, 35.387318F);
        DISTANCE_JUMP_2.put(-35.19499102532636, 37.324306F);
        DISTANCE_JUMP_2.put(-37.13198135208711, 39.300957F);
        DISTANCE_JUMP_2.put(-39.108631910783686, 41.316475F);
        DISTANCE_JUMP_2.put(-41.12414949753382, 43.370083F);
        DISTANCE_JUMP_2.put(-43.17775677251778, 45.461018F);
        DISTANCE_JUMP_2.put(-45.2686919426974, 47.588535F);
        DISTANCE_JUMP_2.put(-47.39620845088072, 49.7519F);
        DISTANCE_JUMP_2.put(-49.55957467100542, 51.950397F);
        DISTANCE_JUMP_2.put(-49.56166940514811, 52.53794F);
        DISTANCE_JUMP_2.put(-51.75807360951643, 54.183327F);
        DISTANCE_JUMP_2.put(-51.76946751552927, 54.779984F);
        DISTANCE_JUMP_2.put(-53.991002612716144, 56.449997F);
        DISTANCE_JUMP_2.put(-56.25767307996749, 58.749733F);
    }

    static {
        EVENTS_JUMP_3.put(2, 0.6637132F);
        EVENTS_JUMP_3.put(3, 1.0154493F);
        EVENTS_JUMP_3.put(4, 1.4385507F);
        EVENTS_JUMP_3.put(5, 1.9315901F);
        EVENTS_JUMP_3.put(6, 2.4931686F);
        EVENTS_JUMP_3.put(7, 3.1219156F);
        EVENTS_JUMP_3.put(8, 3.8164878F);
        EVENTS_JUMP_3.put(9, 4.575568F);
        EVENTS_JUMP_3.put(10, 5.397867F);
        EVENTS_JUMP_3.put(11, 6.28212F);
        EVENTS_JUMP_3.put(12, 7.2270885F);
        EVENTS_JUMP_3.put(13, 8.231557F);
        EVENTS_JUMP_3.put(14, 9.294336F);
        EVENTS_JUMP_3.put(15, 10.41426F);
        EVENTS_JUMP_3.put(16, 11.590185F);
        EVENTS_JUMP_3.put(17, 12.8209915F);
        EVENTS_JUMP_3.put(18, 14.105582F);
        EVENTS_JUMP_3.put(19, 15.442882F);
        EVENTS_JUMP_3.put(20, 16.831835F);
        EVENTS_JUMP_3.put(21, 18.271408F);
        EVENTS_JUMP_3.put(22, 19.76059F);
        EVENTS_JUMP_3.put(23, 21.29839F);
        EVENTS_JUMP_3.put(24, 22.883833F);
        EVENTS_JUMP_3.put(25, 24.515966F);
        EVENTS_JUMP_3.put(26, 26.193857F);
        EVENTS_JUMP_3.put(27, 27.916592F);
        EVENTS_JUMP_3.put(28, 29.683271F);
        EVENTS_JUMP_3.put(29, 31.493017F);
        EVENTS_JUMP_3.put(30, 33.344967F);
        EVENTS_JUMP_3.put(31, 35.238277F);
        EVENTS_JUMP_3.put(32, 37.172123F);
        EVENTS_JUMP_3.put(33, 39.14569F);
        EVENTS_JUMP_3.put(34, 41.158188F);
        EVENTS_JUMP_3.put(35, 43.208836F);
        EVENTS_JUMP_3.put(36, 45.29687F);
        EVENTS_JUMP_3.put(37, 47.421547F);
        EVENTS_JUMP_3.put(38, 49.582127F);
        EVENTS_JUMP_3.put(39, 51.777897F);
        EVENTS_JUMP_3.put(40, 54.008152F);
        EVENTS_JUMP_3.put(41, 56.2722F);
        EVENTS_JUMP_3.put(42, 58.56937F);
        EVENTS_JUMP_3.put(43, 60.898994F);
        DISTANCE_JUMP_3.put(-0.20460647433223755, 0.6637132F);
        DISTANCE_JUMP_3.put(-0.2789143502740359, 1.0154493F);
        DISTANCE_JUMP_3.put(-0.30431682745754074, 1.1451067F);
        DISTANCE_JUMP_3.put(-0.48352082460627344, 1.0154493F);
        DISTANCE_JUMP_3.put(-0.630650420388335, 1.4385507F);
        DISTANCE_JUMP_3.put(-0.8352568947205725, 1.4385507F);
        DISTANCE_JUMP_3.put(-1.2583582516673033, 1.9315901F);
        DISTANCE_JUMP_3.put(-1.3410763443270497, 2.6945615F);
        DISTANCE_JUMP_3.put(-1.7513975910709974, 2.4931686F);
        DISTANCE_JUMP_3.put(-2.3129761546164787, 3.1219156F);
        DISTANCE_JUMP_3.put(-2.9417231591281876, 3.8164878F);
        DISTANCE_JUMP_3.put(-3.636295237067941, 4.575568F);
        DISTANCE_JUMP_3.put(-4.395375888222688, 5.397867F);
        DISTANCE_JUMP_3.put(-5.217674942358528, 6.28212F);
        DISTANCE_JUMP_3.put(-5.224229417871939, 6.593821F);
        DISTANCE_JUMP_3.put(-6.101928032621643, 7.2270885F);
        DISTANCE_JUMP_3.put(-6.129661770349216, 7.5595446F);
        DISTANCE_JUMP_3.put(-7.046896079471168, 8.231557F);
        DISTANCE_JUMP_3.put(-7.095385494572582, 8.584353F);
        DISTANCE_JUMP_3.put(-8.051364784933412, 9.294336F);
        DISTANCE_JUMP_3.put(-8.120194764257079, 9.667067F);
        DISTANCE_JUMP_3.put(-9.114144136971007, 10.41426F);
        DISTANCE_JUMP_3.put(-9.202907869620446, 10.806525F);
        DISTANCE_JUMP_3.put(-10.234067923764641, 11.590185F);
        DISTANCE_JUMP_3.put(-10.342366735053545, 12.001595F);
        DISTANCE_JUMP_3.put(-11.409993257709132, 12.8209915F);
        DISTANCE_JUMP_3.put(-11.537436446437312, 13.251163F);
        DISTANCE_JUMP_3.put(-12.64080010892961, 14.105582F);
        DISTANCE_JUMP_3.put(-12.787004787913432, 14.554139F);
        DISTANCE_JUMP_3.put(-13.925390848127336, 15.442882F);
        DISTANCE_JUMP_3.put(-14.089981787919527, 15.909456F);
        DISTANCE_JUMP_3.put(-15.262689798568601, 16.831835F);
        DISTANCE_JUMP_3.put(-15.445299274303693, 17.316067F);
        DISTANCE_JUMP_3.put(-16.651642797033873, 18.271408F);
        DISTANCE_JUMP_3.put(-16.851910438336688, 18.772945F);
        DISTANCE_JUMP_3.put(-18.091216763547905, 19.76059F);
        DISTANCE_JUMP_3.put(-18.308789407443882, 20.279087F);
        DISTANCE_JUMP_3.put(-19.580399279715223, 21.29839F);
        DISTANCE_JUMP_3.put(-19.814930826482566, 21.833506F);
        DISTANCE_JUMP_3.put(-21.118198175488985, 22.883833F);
        DISTANCE_JUMP_3.put(-21.36934944739373, 23.435236F);
        DISTANCE_JUMP_3.put(-22.703641124204324, 24.515966F);
        DISTANCE_JUMP_3.put(-22.971079727060726, 25.083332F);
        DISTANCE_JUMP_3.put(-24.33577524571116, 26.193857F);
        DISTANCE_JUMP_3.put(-24.61917543321084, 26.776865F);
        DISTANCE_JUMP_3.put(-26.013666717444238, 27.916592F);
        DISTANCE_JUMP_3.put(-26.312709258198765, 28.514929F);
        DISTANCE_JUMP_3.put(-27.736400393271765, 29.683271F);
        DISTANCE_JUMP_3.put(-28.050772440514407, 30.29663F);
        DISTANCE_JUMP_3.put(-29.50307942996716, 31.493017F);
        DISTANCE_JUMP_3.put(-29.832474393860537, 32.121098F);
        DISTANCE_JUMP_3.put(-31.312824921151247, 33.344967F);
        DISTANCE_JUMP_3.put(-31.352625516191353, 33.987476F);
        DISTANCE_JUMP_3.put(-33.16477553855569, 35.238277F);
        DISTANCE_JUMP_3.put(-33.21900414330878, 35.894928F);
        DISTANCE_JUMP_3.put(-35.05808718046107, 37.172123F);
        DISTANCE_JUMP_3.put(-35.126455235008095, 37.84263F);
        DISTANCE_JUMP_3.put(-36.99193262716629, 39.14569F);
        DISTANCE_JUMP_3.put(-37.074157342781035, 39.829777F);
        DISTANCE_JUMP_3.put(-38.96550120334845, 41.158188F);
        DISTANCE_JUMP_3.put(-39.06130544707386, 41.855583F);
        DISTANCE_JUMP_3.put(-40.97799844717569, 43.208836F);
        DISTANCE_JUMP_3.put(-41.08711062870856, 43.919273F);
        DISTANCE_JUMP_3.put(-43.0286457860376, 45.29687F);
        DISTANCE_JUMP_3.put(-43.150799746875606, 46.02009F);
        DISTANCE_JUMP_3.put(-45.11668021876115, 47.421547F);
        DISTANCE_JUMP_3.put(-45.251615123566935, 48.157288F);
        DISTANCE_JUMP_3.put(-47.2413540041822, 49.582127F);
        DISTANCE_JUMP_3.put(-47.38881423432019, 50.330143F);
        DISTANCE_JUMP_3.put(-49.40193435594564, 51.777897F);
        DISTANCE_JUMP_3.put(-49.56166940514811, 52.53794F);
        DISTANCE_JUMP_3.put(-51.59770314340949, 54.008152F);
        DISTANCE_JUMP_3.put(-51.76946751552927, 54.779984F);
        DISTANCE_JUMP_3.put(-53.8279565985309, 56.2722F);
        DISTANCE_JUMP_3.put(-56.09200502861448, 58.56937F);
        DISTANCE_JUMP_3.put(-58.38917253480557, 60.898994F);
    }

    static {
        EVENTS_JUMP_4.put(2, 0.6468847F);
        EVENTS_JUMP_4.put(3, 0.9946219F);
        EVENTS_JUMP_4.put(4, 1.4138043F);
        EVENTS_JUMP_4.put(5, 1.9030031F);
        EVENTS_JUMP_4.put(6, 2.4608178F);
        EVENTS_JUMP_4.put(7, 3.0858765F);
        EVENTS_JUMP_4.put(8, 3.7768338F);
        EVENTS_JUMP_4.put(9, 4.532372F);
        EVENTS_JUMP_4.put(10, 5.3511996F);
        EVENTS_JUMP_4.put(11, 6.2320504F);
        EVENTS_JUMP_4.put(12, 7.1736846F);
        EVENTS_JUMP_4.put(13, 8.174886F);
        EVENTS_JUMP_4.put(14, 9.234463F);
        EVENTS_JUMP_4.put(15, 10.351249F);
        EVENTS_JUMP_4.put(16, 11.524099F);
        EVENTS_JUMP_4.put(17, 12.751892F);
        EVENTS_JUMP_4.put(18, 14.033529F);
        EVENTS_JUMP_4.put(19, 15.367934F);
        EVENTS_JUMP_4.put(20, 16.754051F);
        EVENTS_JUMP_4.put(21, 18.190845F);
        EVENTS_JUMP_4.put(22, 19.677303F);
        EVENTS_JUMP_4.put(23, 21.212433F);
        EVENTS_JUMP_4.put(24, 22.79526F);
        EVENTS_JUMP_4.put(25, 24.42483F);
        EVENTS_JUMP_4.put(26, 26.100208F);
        EVENTS_JUMP_4.put(27, 27.82048F);
        EVENTS_JUMP_4.put(28, 29.584745F);
        EVENTS_JUMP_4.put(29, 31.392126F);
        EVENTS_JUMP_4.put(30, 33.24176F);
        EVENTS_JUMP_4.put(31, 35.1328F);
        EVENTS_JUMP_4.put(32, 37.064423F);
        EVENTS_JUMP_4.put(33, 39.03581F);
        EVENTS_JUMP_4.put(34, 41.04617F);
        EVENTS_JUMP_4.put(35, 43.094723F);
        EVENTS_JUMP_4.put(36, 45.180702F);
        EVENTS_JUMP_4.put(37, 47.303364F);
        EVENTS_JUMP_4.put(38, 49.46197F);
        EVENTS_JUMP_4.put(39, 51.655807F);
        EVENTS_JUMP_4.put(40, 53.884167F);
        EVENTS_JUMP_4.put(41, 58.441708F);
        DISTANCE_JUMP_4.put(-0.2004426876369365, 0.6468847F);
        DISTANCE_JUMP_4.put(-0.47527652687014665, 0.9946219F);
        DISTANCE_JUMP_4.put(-0.823013696086619, 1.4138043F);
        DISTANCE_JUMP_4.put(-1.2421961300771898, 1.9030031F);
        DISTANCE_JUMP_4.put(-1.731394924909111, 2.4608178F);
        DISTANCE_JUMP_4.put(-2.289209754700991, 3.0858765F);
        DISTANCE_JUMP_4.put(-2.914268300062389, 3.7768338F);
        DISTANCE_JUMP_4.put(-3.6052256879644773, 4.532372F);
        DISTANCE_JUMP_4.put(-4.360763942813378, 5.3511996F);
        DISTANCE_JUMP_4.put(-4.380318872101441, 5.688389F);
        DISTANCE_JUMP_4.put(-5.179591448501924, 6.2320504F);
        DISTANCE_JUMP_4.put(-5.224229417871939, 6.593821F);
        DISTANCE_JUMP_4.put(-5.2299514289150295, 18.190845F);
        DISTANCE_JUMP_4.put(-6.060442421220472, 7.1736846F);
        DISTANCE_JUMP_4.put(-6.129661770349216, 7.5595446F);
        DISTANCE_JUMP_4.put(-7.002076392811432, 8.174886F);
        DISTANCE_JUMP_4.put(-7.095385494572582, 8.584353F);
        DISTANCE_JUMP_4.put(-8.003277704456693, 9.234463F);
        DISTANCE_JUMP_4.put(-8.120194764257079, 9.667067F);
        DISTANCE_JUMP_4.put(-9.062855010491319, 10.351249F);
        DISTANCE_JUMP_4.put(-9.202907869620446, 10.806525F);
        DISTANCE_JUMP_4.put(-10.179640792140972, 11.524099F);
        DISTANCE_JUMP_4.put(-10.342366735053545, 12.001595F);
        DISTANCE_JUMP_4.put(-11.352490880984504, 12.751892F);
        DISTANCE_JUMP_4.put(-11.537436446437312, 13.251163F);
        DISTANCE_JUMP_4.put(-12.580283991947383, 14.033529F);
        DISTANCE_JUMP_4.put(-12.787004787913432, 14.554139F);
        DISTANCE_JUMP_4.put(-13.86192126563519, 15.367934F);
        DISTANCE_JUMP_4.put(-14.089981787919527, 15.909456F);
        DISTANCE_JUMP_4.put(-14.576108705105725, 27.82048F);
        DISTANCE_JUMP_4.put(-15.196325819820402, 16.754051F);
        DISTANCE_JUMP_4.put(-15.445299274303693, 17.316067F);
        DISTANCE_JUMP_4.put(-16.582442309899534, 18.190845F);
        DISTANCE_JUMP_4.put(-16.851910438336688, 18.772945F);
        DISTANCE_JUMP_4.put(-18.01923649814104, 19.677303F);
        DISTANCE_JUMP_4.put(-18.308789407443882, 20.279087F);
        DISTANCE_JUMP_4.put(-19.50569483154827, 21.212433F);
        DISTANCE_JUMP_4.put(-19.814930826482566, 21.833506F);
        DISTANCE_JUMP_4.put(-21.040824028165176, 22.79526F);
        DISTANCE_JUMP_4.put(-21.36934944739373, 23.435236F);
        DISTANCE_JUMP_4.put(-22.623650671655895, 24.42483F);
        DISTANCE_JUMP_4.put(-22.971079727060726, 26.776865F);
        DISTANCE_JUMP_4.put(-24.253220813992698, 26.100208F);
        DISTANCE_JUMP_4.put(-24.61917543321084, 28.514929F);
        DISTANCE_JUMP_4.put(-25.92859958609023, 27.82048F);
        DISTANCE_JUMP_4.put(-26.312709258198765, 28.514929F);
        DISTANCE_JUMP_4.put(-27.648870816227003, 29.584745F);
        DISTANCE_JUMP_4.put(-28.050772440514407, 30.29663F);
        DISTANCE_JUMP_4.put(-29.413136656098487, 31.392126F);
        DISTANCE_JUMP_4.put(-29.832474393860537, 32.121098F);
        DISTANCE_JUMP_4.put(-31.220517214349115, 33.24176F);
        DISTANCE_JUMP_4.put(-31.656942343648893, 33.987476F);
        DISTANCE_JUMP_4.put(-33.07015019743366, 35.1328F);
        DISTANCE_JUMP_4.put(-33.52332097076632, 35.894928F);
        DISTANCE_JUMP_4.put(-34.961190557661354, 37.064423F);
        DISTANCE_JUMP_4.put(-35.430772062465635, 37.84263F);
        DISTANCE_JUMP_4.put(-36.892810148279096, 39.03581F);
        DISTANCE_JUMP_4.put(-37.378474170238576, 39.829777F);
        DISTANCE_JUMP_4.put(-38.86419738545308, 41.04617F);
        DISTANCE_JUMP_4.put(-39.365622274531404, 41.855583F);
        DISTANCE_JUMP_4.put(-40.87455691701069, 43.094723F);
        DISTANCE_JUMP_4.put(-41.3914274561661, 43.919273F);
        DISTANCE_JUMP_4.put(-42.9231092978076, 45.180702F);
        DISTANCE_JUMP_4.put(-43.45511657433315, 46.02009F);
        DISTANCE_JUMP_4.put(-45.00909067158749, 47.303364F);
        DISTANCE_JUMP_4.put(-45.555931951024476, 48.157288F);
        DISTANCE_JUMP_4.put(-47.13175245920459, 49.46197F);
        DISTANCE_JUMP_4.put(-47.693131061777734, 50.330143F);
        DISTANCE_JUMP_4.put(-49.290361053081796, 51.655807F);
        DISTANCE_JUMP_4.put(-49.86598623260565, 52.53794F);
        DISTANCE_JUMP_4.put(-51.48419751777952, 53.884167F);
        DISTANCE_JUMP_4.put(-52.07378434298681, 54.779984F);
        DISTANCE_JUMP_4.put(-53.712557296553285, 58.441708F);
    }

    static {
        EVENTS_JUMP_5.put(2, 0.6367358F);
        EVENTS_JUMP_5.put(3, 0.9820613F);
        EVENTS_JUMP_5.put(4, 1.3988804F);
        EVENTS_JUMP_5.put(5, 1.885763F);
        EVENTS_JUMP_5.put(6, 2.441308F);
        EVENTS_JUMP_5.put(7, 3.0641422F);
        EVENTS_JUMP_5.put(8, 3.7529197F);
        EVENTS_JUMP_5.put(9, 4.5063214F);
        EVENTS_JUMP_5.put(10, 5.3230553F);
        EVENTS_JUMP_5.put(11, 6.2018547F);
        EVENTS_JUMP_5.put(12, 7.141478F);
        EVENTS_JUMP_5.put(13, 8.140709F);
        EVENTS_JUMP_5.put(14, 9.198355F);
        EVENTS_JUMP_5.put(15, 10.313248F);
        EVENTS_JUMP_5.put(16, 11.484243F);
        EVENTS_JUMP_5.put(17, 12.710219F);
        EVENTS_JUMP_5.put(18, 13.990075F);
        EVENTS_JUMP_5.put(19, 15.322734F);
        EVENTS_JUMP_5.put(20, 16.70714F);
        EVENTS_JUMP_5.put(21, 18.142258F);
        EVENTS_JUMP_5.put(22, 19.627073F);
        EVENTS_JUMP_5.put(23, 21.160593F);
        EVENTS_JUMP_5.put(24, 22.741842F);
        EVENTS_JUMP_5.put(25, 24.369865F);
        EVENTS_JUMP_5.put(26, 26.043728F);
        EVENTS_JUMP_5.put(27, 27.762514F);
        EVENTS_JUMP_5.put(28, 29.525324F);
        EVENTS_JUMP_5.put(29, 31.331278F);
        EVENTS_JUMP_5.put(30, 33.179512F);
        EVENTS_JUMP_5.put(31, 35.069183F);
        EVENTS_JUMP_5.put(32, 36.999462F);
        EVENTS_JUMP_5.put(33, 38.969532F);
        EVENTS_JUMP_5.put(34, 40.978603F);
        EVENTS_JUMP_5.put(35, 43.025894F);
        EVENTS_JUMP_5.put(36, 45.110638F);
        EVENTS_JUMP_5.put(37, 47.232086F);
        EVENTS_JUMP_5.put(38, 49.389507F);
        EVENTS_JUMP_5.put(39, 51.58218F);
        EVENTS_JUMP_5.put(40, 53.8094F);
        EVENTS_JUMP_5.put(41, 56.070473F);
        EVENTS_JUMP_5.put(42, 58.364723F);
        EVENTS_JUMP_5.put(43, 60.69149F);
        EVENTS_JUMP_5.put(44, 63.05012F);
        DISTANCE_JUMP_5.put(-0.1979316083521212, 0.6367358F);
        DISTANCE_JUMP_5.put(-0.2723729814861997, 0.9820613F);
        DISTANCE_JUMP_5.put(-0.4703045898383209, 0.9820613F);
        DISTANCE_JUMP_5.put(-0.8156301184157826, 1.3988804F);
        DISTANCE_JUMP_5.put(-1.2324491445341295, 1.885763F);
        DISTANCE_JUMP_5.put(-1.7193317996061808, 2.441308F);
        DISTANCE_JUMP_5.put(-2.2748768123892233, 3.0641422F);
        DISTANCE_JUMP_5.put(-2.897710937038667, 3.7529197F);
        DISTANCE_JUMP_5.put(-3.5864883926006144, 4.5063214F);
        DISTANCE_JUMP_5.put(-3.5991856788915584, 4.844478F);
        DISTANCE_JUMP_5.put(-4.339890313714591, 5.3230553F);
        DISTANCE_JUMP_5.put(-4.380318872101441, 5.688389F);
        DISTANCE_JUMP_5.put(-5.156624212302177, 6.2018547F);
        DISTANCE_JUMP_5.put(-6.035423450021852, 7.141478F);
        DISTANCE_JUMP_5.put(-6.975046721274779, 8.140709F);
        DISTANCE_JUMP_5.put(-7.974277546550411, 9.198355F);
        DISTANCE_JUMP_5.put(-9.031923775905227, 10.313248F);
        DISTANCE_JUMP_5.put(-10.146817102371827, 11.484243F);
        DISTANCE_JUMP_5.put(-11.317812585099873, 12.710219F);
        DISTANCE_JUMP_5.put(-12.543788182034206, 13.990075F);
        DISTANCE_JUMP_5.put(-13.82364429193936, 15.322734F);
        DISTANCE_JUMP_5.put(-15.156303305583606, 16.70714F);
        DISTANCE_JUMP_5.put(-16.5407091658993, 18.142258F);
        DISTANCE_JUMP_5.put(-16.547593610879147, 18.772945F);
        DISTANCE_JUMP_5.put(-17.97582693694001, 19.627073F);
        DISTANCE_JUMP_5.put(-18.00447257998634, 20.279087F);
        DISTANCE_JUMP_5.put(-19.460642381458484, 21.160593F);
        DISTANCE_JUMP_5.put(-19.510613999025026, 21.833506F);
        DISTANCE_JUMP_5.put(-20.99416154693307, 22.741842F);
        DISTANCE_JUMP_5.put(-21.06503261993619, 23.435236F);
        DISTANCE_JUMP_5.put(-22.5754103598736, 24.369865F);
        DISTANCE_JUMP_5.put(-22.666762899603185, 25.083332F);
        DISTANCE_JUMP_5.put(-24.20343422824112, 26.043728F);
        DISTANCE_JUMP_5.put(-24.3148586057533, 26.776865F);
        DISTANCE_JUMP_5.put(-25.877297651819262, 27.762514F);
        DISTANCE_JUMP_5.put(-26.008392430741225, 28.514929F);
        DISTANCE_JUMP_5.put(-27.596083840378128, 29.525324F);
        DISTANCE_JUMP_5.put(-27.746455613056867, 30.29663F);
        DISTANCE_JUMP_5.put(-29.358894339474944, 31.331278F);
        DISTANCE_JUMP_5.put(-29.528157566402996, 32.121098F);
        DISTANCE_JUMP_5.put(-31.164848663738653, 33.179512F);
        DISTANCE_JUMP_5.put(-31.352625516191353, 33.987476F);
        DISTANCE_JUMP_5.put(-33.0130839374888, 35.069183F);
        DISTANCE_JUMP_5.put(-33.21900414330878, 35.894928F);
        DISTANCE_JUMP_5.put(-34.90275454254213, 36.999462F);
        DISTANCE_JUMP_5.put(-35.126455235008095, 37.84263F);
        DISTANCE_JUMP_5.put(-36.83303177306287, 38.969532F);
        DISTANCE_JUMP_5.put(-37.074157342781035, 39.829777F);
        DISTANCE_JUMP_5.put(-38.80310349731619, 40.978603F);
        DISTANCE_JUMP_5.put(-39.06130544707386, 41.855583F);
        DISTANCE_JUMP_5.put(-40.81217382618645, 43.025894F);
        DISTANCE_JUMP_5.put(-41.08711062870856, 43.919273F);
        DISTANCE_JUMP_5.put(-42.859462788325175, 45.110638F);
        DISTANCE_JUMP_5.put(-43.150799746875606, 46.02009F);
        DISTANCE_JUMP_5.put(-44.94420601179593, 47.232086F);
        DISTANCE_JUMP_5.put(-45.251615123566935, 48.157288F);
        DISTANCE_JUMP_5.put(-47.065654412086474, 49.389507F);
        DISTANCE_JUMP_5.put(-47.38881423432019, 50.330143F);
        DISTANCE_JUMP_5.put(-49.22307388636051, 51.58218F);
        DISTANCE_JUMP_5.put(-49.56166940514811, 52.53794F);
        DISTANCE_JUMP_5.put(-51.41574501382445, 53.8094F);
        DISTANCE_JUMP_5.put(-51.76946751552927, 54.779984F);
        DISTANCE_JUMP_5.put(-53.64296276208687, 56.070473F);
        DISTANCE_JUMP_5.put(-55.90403619939073, 58.364723F);
        DISTANCE_JUMP_5.put(-58.19828821260094, 60.69149F);
        DISTANCE_JUMP_5.put(-60.52505523083222, 63.05012F);
    }

}
