package me.vekster.lightanticheat.check.checks.movement.flight;

import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.buffer.Buffer;
import me.vekster.lightanticheat.check.checks.movement.MovementCheck;
import me.vekster.lightanticheat.event.playermove.LACAsyncPlayerMoveEvent;
import me.vekster.lightanticheat.event.playerplaceblock.LACAsyncPlayerPlaceBlockEvent;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.player.cache.PlayerCache;
import me.vekster.lightanticheat.player.cache.history.HistoryElement;
import me.vekster.lightanticheat.player.cache.history.PlayerCacheHistory;
import me.vekster.lightanticheat.util.hook.plugin.FloodgateHook;
import me.vekster.lightanticheat.util.hook.plugin.simplehook.EnchantsSquaredHook;
import me.vekster.lightanticheat.util.hook.plugin.simplehook.ValhallaMMOHook;
import me.vekster.lightanticheat.util.precise.AccuracyUtil;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import me.vekster.lightanticheat.version.VerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Acceleration of free fall
 */
public class FlightA extends MovementCheck implements Listener {

    public FlightA() {
        super(CheckName.FLIGHT_A);
    }

    private static final Map<Integer, Double> JUMP_0 = new ConcurrentHashMap<>();
    private static final Map<Integer, Double> JUMP_1 = new ConcurrentHashMap<>();
    private static final Map<Integer, Double> JUMP_2 = new ConcurrentHashMap<>();
    private static final Map<Integer, Double> JUMP_3 = new ConcurrentHashMap<>();
    private static final Map<Integer, Double> JUMP_4 = new ConcurrentHashMap<>();
    private static final Map<Integer, Double> JUMP_5 = new ConcurrentHashMap<>();
    private static final Map<Integer, Double> JUMP_6 = new ConcurrentHashMap<>();
    private static final Map<Integer, Double> SLOW_FALLING_JUMP_0 = new ConcurrentHashMap<>();
    private static final Map<Integer, Double> SLOW_FALLING_JUMP_1 = new ConcurrentHashMap<>();
    private static final Map<Integer, Double> SLOW_FALLING_JUMP_2 = new ConcurrentHashMap<>();
    private static final Map<Integer, Double> SLOW_FALLING_JUMP_3 = new ConcurrentHashMap<>();
    private static final Map<Integer, Double> SLOW_FALLING_JUMP_4 = new ConcurrentHashMap<>();
    private static final Map<Integer, Double> SLOW_FALLING_JUMP_5 = new ConcurrentHashMap<>();
    private static final Map<Integer, Double> SLOW_FALLING_JUMP_6 = new ConcurrentHashMap<>();

    @Override
    public boolean isConditionAllowed(Player player, LACPlayer lacPlayer, PlayerCache cache, boolean isClimbing, boolean isInWater,
                                      boolean isFlying, boolean isInsideVehicle, boolean isGliding, boolean isRiptiding) {
        if (isFlying || isInsideVehicle || isClimbing || isGliding || isRiptiding || isInWater)
            return false;
        if (cache.flyingTicks >= -25 || cache.climbingTicks >= -2 ||
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

        if (!isCheckAllowed(player, lacPlayer, true)) {
            buffer.put("flightTicks", 0);
            return;
        }

        if (!isConditionAllowed(player, lacPlayer, event)) {
            buffer.put("flightTicks", 0);
            return;
        }

        if (!event.isToWithinBlocksPassable() || !event.isFromWithinBlocksPassable()) {
            buffer.put("flightTicks", 0);
            return;
        }

        long currentTime = System.currentTimeMillis();

        if (currentTime - cache.lastEntityNearby <= 1000) {
            buffer.put("flightTicks", 0);
            return;
        }

        if (currentTime - buffer.getLong("effectTime") <= 2000) {
            buffer.put("flightTicks", 0);
            return;
        }

        for (int i = 0; i < 3 && i < HistoryElement.values().length; i++)
            if (cache.history.onEvent.onGround.get(HistoryElement.values()[i]).towardsTrue ||
                    cache.history.onPacket.onGround.get(HistoryElement.values()[i]).towardsTrue) {
                buffer.put("flightTicks", 0);
                return;
            }

        if (event.getFrom().getBlockY() > event.getTo().getBlockY() ||
                event.getFrom().getY() > event.getTo().getY() && getBlockY(event.getTo().getY()) == 0) {
            if (!event.isToDownBlocksPassable()) {
                buffer.put("flightTicks", 0);
                return;
            }
            for (Block block : event.getToDownBlocks()) {
                if (!isActuallyPassable(block.getRelative(BlockFace.DOWN))) {
                    buffer.put("flightTicks", 0);
                    return;
                }
            }
        }

        if (currentTime - buffer.getLong("lastScaffoldPlace") <= 400L) {
            buffer.put("flightTicks", 0);
            return;
        }

        buffer.put("flightTicks", buffer.getInt("flightTicks") + 1);
        int fallingTicks = buffer.getInt("flightTicks");

        int slowFallingEffectAmplifier = getEffectAmplifier(lacPlayer.cache, PotionEffectType.JUMP);
        if (getItemStackAttributes(player, "GENERIC_GRAVITY") != 0)
            slowFallingEffectAmplifier += 1;
        int jumpEffectAmplifier = getEffectAmplifier(lacPlayer.cache, PotionEffectType.JUMP);

        double attributeAmount = Math.max(
                getItemStackAttributes(player, "GENERIC_JUMP_STRENGTH"),
                getPlayerAttributes(player).getOrDefault("GENERIC_JUMP_STRENGTH", 0.42) - 0.42
        );
        if (attributeAmount != 0)
            buffer.put("attribute", System.currentTimeMillis());
        else if (System.currentTimeMillis() - buffer.getLong("attribute") < 4000)
            return;
        if (attributeAmount != 0) {
            if (attributeAmount <= 0.5) {
                if (attributeAmount <= 0.25 && jumpEffectAmplifier == 0)
                    jumpEffectAmplifier = 6;
                else
                    fallingTicks -= 90;
            } else if (attributeAmount <= 1.0) {
                fallingTicks -= 150;
            } else {
                fallingTicks -= 300;
            }
        }

        PlayerCacheHistory<Location> eventHistory = cache.history.onEvent.location;
        PlayerCacheHistory<Location> packetHistory = cache.history.onPacket.location;
        double verticalSpeed = Math.min(
                Math.min(
                        distanceVertical(event.getFrom(), event.getTo()),
                        decreaseVertical(distanceVertical(eventHistory.get(HistoryElement.FIRST), event.getTo()) / 2.0, 1.2)
                ),
                Math.min(
                        decreaseVertical(distanceVertical(packetHistory.get(HistoryElement.FIRST), packetHistory.get(HistoryElement.FROM)), 1.25),
                        decreaseVertical(distanceVertical(packetHistory.get(HistoryElement.SECOND), packetHistory.get(HistoryElement.FROM)) / 2.0, 1.25, 1.2)
                )
        );


        Map<Integer, Double> map;
        switch (jumpEffectAmplifier) {
            case 0:
                map = slowFallingEffectAmplifier == 0 ? JUMP_0 : SLOW_FALLING_JUMP_0;
                break;
            case 1:
                map = slowFallingEffectAmplifier == 0 ? JUMP_1 : SLOW_FALLING_JUMP_1;
                break;
            case 2:
                map = slowFallingEffectAmplifier == 0 ? JUMP_2 : SLOW_FALLING_JUMP_2;
                break;
            case 3:
                map = slowFallingEffectAmplifier == 0 ? JUMP_3 : SLOW_FALLING_JUMP_3;
                break;
            case 4:
                map = slowFallingEffectAmplifier == 0 ? JUMP_4 : SLOW_FALLING_JUMP_4;
                break;
            case 5:
                map = slowFallingEffectAmplifier == 0 ? JUMP_5 : SLOW_FALLING_JUMP_5;
                break;
            default:
                map = slowFallingEffectAmplifier == 0 ? JUMP_6 : SLOW_FALLING_JUMP_6;
                break;
        }

        double calculatedVerticalSpeed;
        if (map.containsKey(fallingTicks)) calculatedVerticalSpeed = map.get(fallingTicks);
        else calculatedVerticalSpeed = slowFallingEffectAmplifier == 0 ? -0.5 : -0.2;

        if (calculatedVerticalSpeed > 0) verticalSpeed -= 0.05;
        verticalSpeed = decreaseVertical(verticalSpeed, 0.925);
        verticalSpeed -= 0.095;

        if (FloodgateHook.isBedrockPlayer(player, true)) {
            verticalSpeed -= 0.11;
        }
        if (FloodgateHook.isProbablyPocketEditionPlayer(player, true)) {
            verticalSpeed = decreaseVertical(verticalSpeed, 0.85);
            verticalSpeed -= 0.05;
        }

        if (fallingTicks <= 12) {
            if (distanceHorizontal(event.getFrom(), event.getTo()) > distanceAbsVertical(event.getFrom(), event.getTo()))
                verticalSpeed -= 0.15;
            else if (distanceHorizontal(event.getFrom(), event.getTo()) * 1.5 > distanceAbsVertical(event.getFrom(), event.getTo()))
                verticalSpeed -= 0.1;
        }

        if (System.currentTimeMillis() - buffer.getLong("fallingTime") < 4000) {
            verticalSpeed = decreaseVertical(verticalSpeed, 0.9);
            verticalSpeed -= 0.4;
        }

        if (verticalSpeed <= calculatedVerticalSpeed)
            return;

        Set<Player> players = getPlayersForEnchantsSquared(lacPlayer, player);
        updateDownBlocks(player, lacPlayer, event.getToDownBlocks());
        Scheduler.runTask(true, () -> {
            if (currentTime - buffer.getLong("lastScaffoldPlace") <= 400L ||
                    lacPlayer.isGliding() || lacPlayer.isRiptiding()) {
                buffer.put("flightTicks", 0);
                return;
            }

            if (isLagGlidingPossible(player, buffer)) {
                buffer.put("lastGlidingLagPossibleTime", System.currentTimeMillis());
                return;
            }
            if (isPingGlidingPossible(player, cache))
                return;

            if (EnchantsSquaredHook.hasEnchantment(player, "Burden"))
                return;
            if (isEnchantsSquaredImpact(players))
                return;

            if (AccuracyUtil.isViolationCancel(getCheckSetting(), buffer))
                return;

            if (ValhallaMMOHook.isPluginInstalled()) {
                if (System.currentTimeMillis() - buffer.getLong("firstLevelFlagTime") > 8000) {
                    buffer.put("firstLevelFlagTime", System.currentTimeMillis());
                    buffer.put("firstLevelFlags", 0);
                }

                buffer.put("firstLevelFlags", buffer.getInt("firstLevelFlags") + 1);
                if (buffer.getInt("firstLevelFlags") <= 9)
                    return;
            }

            if (System.currentTimeMillis() - buffer.getLong("lastGlidingLagPossibleTime") < 5 * 1000)
                callViolationEventIfRepeat(player, lacPlayer, event, buffer, 500);
            else
                callViolationEventIfRepeat(player, lacPlayer, event, buffer, 900);
        });
    }

    @EventHandler(priority = EventPriority.LOW)
    public void beforeMovement(LACAsyncPlayerMoveEvent event) {
        LACPlayer lacPlayer = event.getLacPlayer();
        Player player = event.getPlayer();

        if (!isCheckAllowed(player, lacPlayer, true))
            return;

        if (getEffectAmplifier(lacPlayer.cache, VerUtil.potions.get("LEVITATION")) > 0 ||
                getEffectAmplifier(lacPlayer.cache, VerUtil.potions.get("SLOW_FALLING")) > 1 ||
                getEffectAmplifier(lacPlayer.cache, PotionEffectType.JUMP) > 6) {
            Buffer buffer = getBuffer(player, true);
            buffer.put("effectTime", System.currentTimeMillis());
        }

        Location first = null;
        Location previous = null;
        for (int i = 0; i < 6 && i < HistoryElement.values().length; i++) {
            Location location = lacPlayer.cache.history.onEvent.location.get(HistoryElement.values()[i]);
            if (previous == null) {
                first = location;
                previous = location;
                continue;
            }
            if (distanceVertical(location, previous) >= -0.05)
                break;
            if (i == 5) {
                if (distanceVertical(previous, first) >= -0.5)
                    break;
                Buffer buffer = getBuffer(player, true);
                buffer.put("fallingTime", System.currentTimeMillis());
            }
            previous = location;
        }
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

    private static double decreaseVertical(double value, double multiplier) {
        return value >= 0 ? value * multiplier : value / multiplier;
    }

    private static double decreaseVertical(double value, double firstMultiplier, double secondMultiplier) {
        return decreaseVertical(decreaseVertical(value, firstMultiplier), secondMultiplier);
    }

    static {
        JUMP_0.put(1, 0.1647732818260721);
        JUMP_0.put(2, 0.08307781780646906);
        JUMP_0.put(3, 0.003016261509046103);
        JUMP_0.put(4, -0.07544406518948676);
        JUMP_0.put(5, -0.15233518685055003);
        JUMP_0.put(6, -0.22768848754498094);
        JUMP_0.put(7, -0.3015347236627832);
        JUMP_0.put(8, -0.3739040364667261);
        JUMP_0.put(9, -0.44482596439492283);
        JUMP_0.put(10, -0.5143294551172772);
        JUMP_0.put(11, -0.5824428773508714);
        JUMP_0.put(12, -0.649194032438956);
        JUMP_0.put(13, -0.7146101656984456);
        JUMP_0.put(14, -0.7787179775404667);
        JUMP_0.put(15, -0.8415436343683922);
        JUMP_0.put(16, -0.9031127792580804);
        JUMP_0.put(17, -0.9634505424243116);
        JUMP_0.put(18, -1.022581551478055);
        JUMP_0.put(19, -1.0805299414785736);
        JUMP_0.put(20, -1.1373193647843465);
        JUMP_0.put(21, -1.192973000707184);
        JUMP_0.put(22, -1.247513564973076);
        JUMP_0.put(23, -1.3009633189939223);
        JUMP_0.put(24, -1.353344078953839);
        JUMP_0.put(25, -1.4046772247136232);
    }

    static {
        JUMP_1.put(1, 0.25889248171158386);
        JUMP_1.put(2, 0.1753146354894497);
        JUMP_1.put(3, 0.09340834459764835);
        JUMP_1.put(4, 0.013140177961432187);
        JUMP_1.put(5, -0.06552262687304733);
        JUMP_1.put(6, -0.14261217711120366);
        JUMP_1.put(7, -0.21815993781497411);
        JUMP_1.put(8, -0.29219674474562396);
        JUMP_1.put(9, -0.3647528169498031);
        JUMP_1.put(10, -0.4358577690937864);
        JUMP_1.put(11, -0.505540623551127);
        JUMP_1.put(12, -0.5738298222483991);
        JUMP_1.put(13, -0.6407532382742431);
        JUMP_1.put(14, -0.706338187256037);
        JUMP_1.put(15, -0.7706114385091354);
        JUMP_1.put(16, -0.83359922596307);
        JUMP_1.put(17, -0.8953272588693295);
        JUMP_1.put(18, -0.9558207322948391);
        JUMP_1.put(19, -1.0151043374056599);
        JUMP_1.put(20, -1.073202271545);
        JUMP_1.put(21, -1.130138248109688);
        JUMP_1.put(22, -1.1859355062290433);
        JUMP_1.put(23, -1.240616820250267);
        JUMP_1.put(24, -1.2942045090340315);
        JUMP_1.put(25, -1.3467204450642214);
        JUMP_1.put(26, -1.3981860633754764);
        JUMP_1.put(27, -1.4486223703021182);
    }

    static {
        JUMP_2.put(1, 0.3530117096467933);
        JUMP_2.put(2, 0.2675514806611403);
        JUMP_2.put(3, 0.18380045462518524);
        JUMP_2.put(4, 0.10172444751252385);
        JUMP_2.put(5, 0.02128995897662378);
        JUMP_2.put(6, -0.05753584132270362);
        JUMP_2.put(7, -0.13478512711954238);
        JUMP_2.put(8, -0.21048942867385279);
        JUMP_2.put(9, -0.28467964564102033);
        JUMP_2.put(10, -0.35738605968391823);
        JUMP_2.put(11, -0.42863834683271307);
        JUMP_2.put(12, -0.49846558959757203);
        JUMP_2.put(13, -0.5668962888389757);
        JUMP_2.put(14, -0.6339583754007521);
        JUMP_2.put(15, -0.6996792215104222);
        JUMP_2.put(16, -0.7640856519514045);
        JUMP_2.put(17, -0.8272039550120383);
        JUMP_2.put(18, -0.8890598932153466);
        JUMP_2.put(19, -0.9496787138343876);
        JUMP_2.put(20, -1.0090851591972552);
        JUMP_2.put(21, -1.0673034767859662);
        JUMP_2.put(22, -1.124357429133326);
        JUMP_2.put(23, -1.1802703035219508);
        JUMP_2.put(24, -1.2350649214892542);
        JUMP_2.put(25, -1.28876364814235);
        JUMP_2.put(26, -1.3413884012865935);
        JUMP_2.put(27, -1.392960660371699);
        JUMP_2.put(28, -1.4435014752587563);
        JUMP_2.put(29, -1.4930314748120708);
    }

    static {
        JUMP_3.put(1, 0.44713093758201694);
        JUMP_3.put(2, 0.35978832583283804);
        JUMP_3.put(3, 0.2741925646527221);
        JUMP_3.put(4, 0.1903087170636013);
        JUMP_3.put(5, 0.10810254482629489);
        JUMP_3.put(6, 0.027540494465782217);
        JUMP_3.put(7, -0.05141031642411065);
        JUMP_3.put(8, -0.1287821126020816);
        JUMP_3.put(9, -0.20460647433223755);
        JUMP_3.put(10, -0.2789143502740359);
        JUMP_3.put(11, -0.3517360701142991);
        JUMP_3.put(12, -0.4231013569467308);
        JUMP_3.put(13, -0.49303933940369404);
        JUMP_3.put(14, -0.5615785635454813);
        JUMP_3.put(15, -0.628747004511709);
        JUMP_3.put(16, -0.6945720779397533);
        JUMP_3.put(17, -0.759080651154747);
        JUMP_3.put(18, -0.8222990541358399);
        JUMP_3.put(19, -0.8842530902631154);
        JUMP_3.put(20, -0.9449680468495245);
        JUMP_3.put(21, -1.0044687054622443);
        JUMP_3.put(22, -1.0627793520375945);
        JUMP_3.put(23, -1.1199237867936347);
        JUMP_3.put(24, -1.175925333944491);
        JUMP_3.put(25, -1.2308068512204784);
        JUMP_3.put(26, -1.284590739197725);
        JUMP_3.put(27, -1.3372989504412658);
        JUMP_3.put(28, -1.388952998465271);
        JUMP_3.put(29, -1.4395739665140326);
        JUMP_3.put(30, -1.4891825161673182);
    }

    static {
        JUMP_4.put(1, 0.5412501094178168);
        JUMP_4.put(2, 0.4520251160271158);
        JUMP_4.put(3, 0.36458462080238974);
        JUMP_4.put(4, 0.27889293381436175);
        JUMP_4.put(5, 0.1949150789316576);
        JUMP_4.put(6, 0.11261677954485094);
        JUMP_4.put(7, 0.031964444576075834);
        JUMP_4.put(8, -0.04707484523166272);
        JUMP_4.put(9, -0.12453335075078087);
        JUMP_4.put(10, -0.2004426876369365);
        JUMP_4.put(11, -0.27483383923321014);
        JUMP_4.put(12, -0.34773716921647235);
        JUMP_4.put(13, -0.41918243399057076);
        JUMP_4.put(14, -0.4891987948319212);
        JUMP_4.put(15, -0.5578148297918801);
        JUMP_4.put(16, -0.6250585453613979);
        JUMP_4.put(17, -0.6909573879020883);
        JUMP_4.put(18, -0.7555382548489007);
        JUMP_4.put(19, -0.8188275056885459);
        JUMP_4.put(20, -0.8808509727185481);
        JUMP_4.put(21, -0.9416339715909601);
        JUMP_4.put(22, -1.0012013116452607);
        JUMP_4.put(23, -1.059577306034626);
        JUMP_4.put(24, -1.1167857816496536);
        JUMP_4.put(25, -1.172850088843532);
        JUMP_4.put(26, -1.2277931109628781);
        JUMP_4.put(27, -1.2816372736878066);
        JUMP_4.put(28, -1.3344045541852125);
        JUMP_4.put(29, -1.3861164900791323);
        JUMP_4.put(30, -1.4367941882415067);
        JUMP_4.put(31, -1.4864583334072279);
        JUMP_4.put(32, -1.5351291966169072);
    }

    static {
        JUMP_5.put(1, 0.6353692812536167);
        JUMP_5.put(2, 0.5442619062213794);
        JUMP_5.put(3, 0.45497667695204314);
        JUMP_5.put(4, 0.3674771505651222);
        JUMP_5.put(5, 0.2817276130370061);
        JUMP_5.put(6, 0.19769306462391967);
        JUMP_5.put(7, 0.1153392055762481);
        JUMP_5.put(8, 0.03463242213877038);
        JUMP_5.put(9, -0.044460227169324185);
        JUMP_5.put(10, -0.12197102499982293);
        JUMP_5.put(11, -0.1979316083521212);
        JUMP_5.put(12, -0.2723729814861997);
        JUMP_5.put(13, -0.3453255285774617);
        JUMP_5.put(14, -0.41681902611834687);
        JUMP_5.put(15, -0.4868826550720513);
        JUMP_5.put(16, -0.5555450127830426);
        JUMP_5.put(17, -0.6228341246494438);
        JUMP_5.put(18, -0.6887774555619472);
        JUMP_5.put(19, -0.7534019211139764);
        JUMP_5.put(20, -0.8167338985875858);
        JUMP_5.put(21, -0.8787992377196758);
        JUMP_5.put(22, -0.9396232712529269);
        JUMP_5.put(23, -0.9992308252756317);
        JUMP_5.put(24, -1.0576462293548161);
        JUMP_5.put(25, -1.1148933264665999);
        JUMP_5.put(26, -1.1709954827280455);
        JUMP_5.put(27, -1.2259755969343331);
        JUMP_5.put(28, -1.279856109905154);
        JUMP_5.put(29, -1.3326590136442462);
        JUMP_5.put(30, -1.3844058603156952);
        JUMP_5.put(31, -1.4351177710407086);
        JUMP_5.put(32, -1.4848154445184747);
        JUMP_5.put(33, -1.533519165474587);
        JUMP_5.put(34, -1.5812488129405295);
    }

    static {
        JUMP_6.put(1, 0.7294885091888403);
        JUMP_6.put(2, 0.6364987513930771);
        JUMP_6.put(3, 0.54536878697958);
        JUMP_6.put(4, 0.45606142011619966);
        JUMP_6.put(5, 0.3685401988866772);
        JUMP_6.put(6, 0.2827694004124055);
        JUMP_6.put(7, 0.19871401627167984);
        JUMP_6.put(8, 0.11633973821054155);
        JUMP_6.put(9, 0.03561294413945859);
        JUMP_6.put(10, -0.04349931558995479);
        JUMP_6.put(11, -0.12102933163370722);
        JUMP_6.put(12, -0.19700874883537267);
        JUMP_6.put(13, -0.27146857914218003);
        JUMP_6.put(14, -0.3444392142630761);
        JUMP_6.put(15, -0.4159504380733523);
        JUMP_6.put(16, -0.4860314387713771);
        JUMP_6.put(17, -0.5547108207921525);
        JUMP_6.put(18, -0.6220166164824548);
        JUMP_6.put(19, -0.6879762975427042);
        JUMP_6.put(20, -0.752616786239841);
        JUMP_6.put(21, -0.815964466395954);
        JUMP_6.put(22, -0.8780451941571954);
        JUMP_6.put(23, -0.9388843085473155);
        JUMP_6.put(24, -0.9985066418100388);
        JUMP_6.put(25, -1.0569365295447284);
        JUMP_6.put(26, -1.114197820639177);
        JUMP_6.put(27, -1.1703138870038998);
        JUMP_6.put(28, -1.2253076331116688);
        JUMP_6.put(29, -1.279201505346208);
        JUMP_6.put(30, -1.332017501163989);
        JUMP_6.put(31, -1.3837771780728048);
        JUMP_6.put(32, -1.4345016624306908);
        JUMP_6.put(33, -1.4842116580689009);
        JUMP_6.put(34, -1.5329274547424916);
        JUMP_6.put(35, -1.5806689364117972);
    }

    static {
        SLOW_FALLING_JUMP_0.put(1, 0.1647732818260721);
        SLOW_FALLING_JUMP_0.put(2, 0.08307781780646906);
        SLOW_FALLING_JUMP_0.put(3, 0.003016261509046103);
        SLOW_FALLING_JUMP_0.put(4, -0.07544406518948676);
        SLOW_FALLING_JUMP_0.put(5, -0.08373518551540826);
        SLOW_FALLING_JUMP_0.put(6, -0.09186048359296706);
        SLOW_FALLING_JUMP_0.put(7, -0.09982327586394035);
        SLOW_FALLING_JUMP_0.put(8, -0.10762681244136729);
        SLOW_FALLING_JUMP_0.put(9, -0.11527427843610383);
        SLOW_FALLING_JUMP_0.put(11, -0.13011342188401898);
        SLOW_FALLING_JUMP_0.put(13, -0.14436493580615206);
        SLOW_FALLING_JUMP_0.put(15, -0.1580520903097522);
        SLOW_FALLING_JUMP_0.put(17, -0.17119723400668363);
        SLOW_FALLING_JUMP_0.put(19, -0.18382183050464107);
        SLOW_FALLING_JUMP_0.put(21, -0.1959464934532349);
        SLOW_FALLING_JUMP_0.put(23, -0.20759102020232945);
        SLOW_FALLING_JUMP_0.put(25, -0.21877442412747428);
        SLOW_FALLING_JUMP_0.put(27, -0.2295149656752784);
        SLOW_FALLING_JUMP_0.put(29, -0.23983018217930407);
        SLOW_FALLING_JUMP_0.put(31, -0.24973691649539376);
        SLOW_FALLING_JUMP_0.put(33, -0.2592513445029283);
        SLOW_FALLING_JUMP_0.put(35, -0.2683890015170505);
        SLOW_FALLING_JUMP_0.put(37, -0.27716480765501217);
        SLOW_FALLING_JUMP_0.put(39, -0.2855930921979848);
        SLOW_FALLING_JUMP_0.put(41, -0.2936876169881515);
        SLOW_FALLING_JUMP_0.put(43, -0.3014615988992233);
        SLOW_FALLING_JUMP_0.put(45, -0.30892773141724206);
        SLOW_FALLING_JUMP_0.put(47, -0.3160982053666572);
        SLOW_FALLING_JUMP_0.put(49, -0.32298472881574014);
        SLOW_FALLING_JUMP_0.put(51, -0.32959854619367945);
        SLOW_FALLING_JUMP_0.put(53, -0.335950456650707);
        SLOW_FALLING_JUMP_0.put(55, -0.34205083169109685);
        SLOW_FALLING_JUMP_0.put(57, -0.34790963210794246);
        SLOW_FALLING_JUMP_0.put(59, -0.35353642424730936);
        SLOW_FALLING_JUMP_0.put(61, -0.35894039562830926);
        SLOW_FALLING_JUMP_0.put(63, -0.36413036994464676);
        SLOW_FALLING_JUMP_0.put(65, -0.369114821472067);
        SLOW_FALLING_JUMP_0.put(67, -0.37390188890535114);
        SLOW_FALLING_JUMP_0.put(69, -0.3784993886472421);
        SLOW_FALLING_JUMP_0.put(71, -0.38291482757121287);
        SLOW_FALLING_JUMP_0.put(73, -0.387155415278869);
    }

    static {
        SLOW_FALLING_JUMP_1.put(1, 0.25889248171158386);
        SLOW_FALLING_JUMP_1.put(2, 0.1753146354894426);
        SLOW_FALLING_JUMP_1.put(3, 0.09340834459764835);
        SLOW_FALLING_JUMP_1.put(4, 0.013140177961432187);
        SLOW_FALLING_JUMP_1.put(5, -0.06552262687304733);
        SLOW_FALLING_JUMP_1.put(6, -0.0740121757760619);
        SLOW_FALLING_JUMP_1.put(7, -0.08233193386294602);
        SLOW_FALLING_JUMP_1.put(8, -0.09048529694678109);
        SLOW_FALLING_JUMP_1.put(9, -0.0984755929244443);
        SLOW_FALLING_JUMP_1.put(11, -0.11397996369062469);
        SLOW_FALLING_JUMP_1.put(13, -0.1288703619540854);
        SLOW_FALLING_JUMP_1.put(15, -0.14317110100297725);
        SLOW_FALLING_JUMP_1.put(17, -0.15690553132014884);
        SLOW_FALLING_JUMP_1.put(19, -0.17009607871020194);
        SLOW_FALLING_JUMP_1.put(21, -0.1827642809167287);
        SLOW_FALLING_JUMP_1.put(23, -0.19493082278947327);
        SLOW_FALLING_JUMP_1.put(25, -0.20661557005888653);
        SLOW_FALLING_JUMP_1.put(27, -0.21783760177325462);
        SLOW_FALLING_JUMP_1.put(29, -0.22861524145125145);
        SLOW_FALLING_JUMP_1.put(31, -0.23896608700091804);
        SLOW_FALLING_JUMP_1.put(33, -0.24890703945376913);
        SLOW_FALLING_JUMP_1.put(35, -0.2584543305611362);
        SLOW_FALLING_JUMP_1.put(37, -0.2676235492975536);
        SLOW_FALLING_JUMP_1.put(39, -0.2764296673147868);
        SLOW_FALLING_JUMP_1.put(41, -0.2848870633877567);
        SLOW_FALLING_JUMP_1.put(43, -0.29300954689240655);
        SLOW_FALLING_JUMP_1.put(45, -0.3008103803539228);
        SLOW_FALLING_JUMP_1.put(47, -0.308302301101989);
        SLOW_FALLING_JUMP_1.put(49, -0.3154975420684991);
        SLOW_FALLING_JUMP_1.put(51, -0.3224078517617386);
        SLOW_FALLING_JUMP_1.put(53, -0.3290445134494462);
        SLOW_FALLING_JUMP_1.put(55, -0.33541836358243415);
        SLOW_FALLING_JUMP_1.put(57, -0.3415398094884381);
        SLOW_FALLING_JUMP_1.put(59, -0.3474188463654144);
        SLOW_FALLING_JUMP_1.put(61, -0.35306507360184014);
        SLOW_FALLING_JUMP_1.put(63, -0.358487710450774);
        SLOW_FALLING_JUMP_1.put(65, -0.3636956110832159);
        SLOW_FALLING_JUMP_1.put(67, -0.3686972790453069);
        SLOW_FALLING_JUMP_1.put(69, -0.3735008811430731);
        SLOW_FALLING_JUMP_1.put(71, -0.3781142607773518);
        SLOW_FALLING_JUMP_1.put(73, -0.38254495075058514);
        SLOW_FALLING_JUMP_1.put(75, -0.3868001855665142);
        SLOW_FALLING_JUMP_1.put(77, -0.39088691324279523);
    }

    static {
        SLOW_FALLING_JUMP_2.put(1, 0.3530117096467933);
        SLOW_FALLING_JUMP_2.put(2, 0.2675514806611403);
        SLOW_FALLING_JUMP_2.put(3, 0.18380045462518524);
        SLOW_FALLING_JUMP_2.put(4, 0.10172444751252385);
        SLOW_FALLING_JUMP_2.put(5, 0.02128995897662378);
        SLOW_FALLING_JUMP_2.put(6, -0.05753584132270362);
        SLOW_FALLING_JUMP_2.put(7, -0.06618512578440061);
        SLOW_FALLING_JUMP_2.put(8, -0.07466142472182469);
        SLOW_FALLING_JUMP_2.put(9, -0.08296819784217746);
        SLOW_FALLING_JUMP_2.put(11, -0.09908666087389406);
        SLOW_FALLING_JUMP_2.put(13, -0.11456683337212326);
        SLOW_FALLING_JUMP_2.put(15, -0.12943399161812863);
        SLOW_FALLING_JUMP_2.put(17, -0.14371241095338405);
        SLOW_FALLING_JUMP_2.put(19, -0.15742540541675965);
        SLOW_FALLING_JUMP_2.put(21, -0.1705953658120336);
        SLOW_FALLING_JUMP_2.put(23, -0.1832437962679876);
        SLOW_FALLING_JUMP_2.put(25, -0.19539134935074287);
        SLOW_FALLING_JUMP_2.put(27, -0.20705785978555014);
        SLOW_FALLING_JUMP_2.put(29, -0.21826237684327054);
        SLOW_FALLING_JUMP_2.put(31, -0.229023195444384);
        SLOW_FALLING_JUMP_2.put(33, -0.23935788603117203);
        SLOW_FALLING_JUMP_2.put(35, -0.24928332325707458);
        SLOW_FALLING_JUMP_2.put(37, -0.25881571353988875);
        SLOW_FALLING_JUMP_2.put(39, -0.2679706215238582);
        SLOW_FALLING_JUMP_2.put(41, -0.27676299549391103);
        SLOW_FALLING_JUMP_2.put(43, -0.2852071917834422);
        SLOW_FALLING_JUMP_2.put(45, -0.29331699821558743);
        SLOW_FALLING_JUMP_2.put(47, -0.30110565661620114);
        SLOW_FALLING_JUMP_2.put(49, -0.30858588443531687);
        SLOW_FALLING_JUMP_2.put(51, -0.31576989551244594);
        SLOW_FALLING_JUMP_2.put(53, -0.32266942001947996);
        SLOW_FALLING_JUMP_2.put(55, -0.3292957236139671);
        SLOW_FALLING_JUMP_2.put(57, -0.33565962583382714);
        SLOW_FALLING_JUMP_2.put(59, -0.3417715177636893);
        SLOW_FALLING_JUMP_2.put(61, -0.3476413790016295);
        SLOW_FALLING_JUMP_2.put(63, -0.3532787939539759);
        SLOW_FALLING_JUMP_2.put(65, -0.3586929674849557);
        SLOW_FALLING_JUMP_2.put(67, -0.3638927399465217);
        SLOW_FALLING_JUMP_2.put(69, -0.36888660161298503);
        SLOW_FALLING_JUMP_2.put(71, -0.3736827065441588);
        SLOW_FALLING_JUMP_2.put(73, -0.3782888858993516);
        SLOW_FALLING_JUMP_2.put(75, -0.38271266072428034);
        SLOW_FALLING_JUMP_2.put(77, -0.3869612542315082);
        SLOW_FALLING_JUMP_2.put(79, -0.39104160359470086);
        SLOW_FALLING_JUMP_2.put(81, -0.39496037127563);
    }

    static {
        SLOW_FALLING_JUMP_3.put(1, 0.44713093758201694);
        SLOW_FALLING_JUMP_3.put(2, 0.35978832583283804);
        SLOW_FALLING_JUMP_3.put(3, 0.2741925646527221);
        SLOW_FALLING_JUMP_3.put(4, 0.1903087170636013);
        SLOW_FALLING_JUMP_3.put(5, 0.10810254482629489);
        SLOW_FALLING_JUMP_3.put(6, 0.027540494465782217);
        SLOW_FALLING_JUMP_3.put(7, -0.05141031642411065);
        SLOW_FALLING_JUMP_3.put(8, -0.06018211126693984);
        SLOW_FALLING_JUMP_3.put(9, -0.06877847038022367);
        SLOW_FALLING_JUMP_3.put(11, -0.08545884608895449);
        SLOW_FALLING_JUMP_3.put(13, -0.10147867954320589);
        SLOW_FALLING_JUMP_3.put(15, -0.11686412819155123);
        SLOW_FALLING_JUMP_3.put(17, -0.13164031364858886);
        SLOW_FALLING_JUMP_3.put(19, -0.1458313627139347);
        SLOW_FALLING_JUMP_3.put(21, -0.1594604467668006);
        SLOW_FALLING_JUMP_3.put(23, -0.1725498196006754);
        SLOW_FALLING_JUMP_3.put(25, -0.18512085375968468);
        SLOW_FALLING_JUMP_3.put(27, -0.19719407543593093);
        SLOW_FALLING_JUMP_3.put(29, -0.20878919798515483);
        SLOW_FALLING_JUMP_3.put(31, -0.21992515411490388);
        SLOW_FALLING_JUMP_3.put(33, -0.23062012679821464);
        SLOW_FALLING_JUMP_3.put(35, -0.24089157896308677);
        SLOW_FALLING_JUMP_3.put(37, -0.2507562820062219);
        SLOW_FALLING_JUMP_3.put(39, -0.26023034317763916);
        SLOW_FALLING_JUMP_3.put(41, -0.2693292318808318);
        SLOW_FALLING_JUMP_3.put(43, -0.2780678049315526);
        SLOW_FALLING_JUMP_3.put(45, -0.28646033081612643);
        SLOW_FALLING_JUMP_3.put(47, -0.29452051298942195);
        SLOW_FALLING_JUMP_3.put(49, -0.3022615122499843);
        SLOW_FALLING_JUMP_3.put(51, -0.30969596822922085);
        SLOW_FALLING_JUMP_3.put(53, -0.3168360200295979);
        SLOW_FALLING_JUMP_3.put(55, -0.32369332604561407);
        SLOW_FALLING_JUMP_3.put(57, -0.33027908299973774);
        SLOW_FALLING_JUMP_3.put(59, -0.3366040442246856);
        SLOW_FALLING_JUMP_3.put(61, -0.3426785372215875);
        SLOW_FALLING_JUMP_3.put(63, -0.34851248052288497);
        SLOW_FALLING_JUMP_3.put(65, -0.35411539988756147);
        SLOW_FALLING_JUMP_3.put(67, -0.3594964438548516);
        SLOW_FALLING_JUMP_3.put(69, -0.36466439868220846);
        SLOW_FALLING_JUMP_3.put(71, -0.36962770269158796);
        SLOW_FALLING_JUMP_3.put(73, -0.37439446004775334);
        SLOW_FALLING_JUMP_3.put(75, -0.37897245399081214);
        SLOW_FALLING_JUMP_3.put(77, -0.38336915954486983);
        SLOW_FALLING_JUMP_3.put(79, -0.38759175572334925);
        SLOW_FALLING_JUMP_3.put(81, -0.3916471372510273);
        SLOW_FALLING_JUMP_3.put(83, -0.39554192582181713);
        SLOW_FALLING_JUMP_3.put(85, -0.39928248091079865);
    }

    static {
        SLOW_FALLING_JUMP_4.put(1, 0.5412501094178168);
        SLOW_FALLING_JUMP_4.put(2, 0.4520251160271158);
        SLOW_FALLING_JUMP_4.put(3, 0.36458462080238974);
        SLOW_FALLING_JUMP_4.put(4, 0.27889293381436175);
        SLOW_FALLING_JUMP_4.put(5, 0.1949150789316576);
        SLOW_FALLING_JUMP_4.put(6, 0.11261677954485094);
        SLOW_FALLING_JUMP_4.put(7, 0.031964444576075834);
        SLOW_FALLING_JUMP_4.put(8, -0.04707484523166272);
        SLOW_FALLING_JUMP_4.put(9, -0.0559333494156391);
        SLOW_FALLING_JUMP_4.put(11, -0.07312239143436727);
        SLOW_FALLING_JUMP_4.put(13, -0.08963074803175175);
        SLOW_FALLING_JUMP_4.put(15, -0.10548537432502769);
        SLOW_FALLING_JUMP_4.put(17, -0.12071215800980895);
        SLOW_FALLING_JUMP_4.put(19, -0.1353359616298917);
        SLOW_FALLING_JUMP_4.put(21, -0.1493806631733321);
        SLOW_FALLING_JUMP_4.put(23, -0.16286919506069353);
        SLOW_FALLING_JUMP_4.put(25, -0.1758235815895688);
        SLOW_FALLING_JUMP_4.put(27, -0.18826497489619953);
        SLOW_FALLING_JUMP_4.put(29, -0.20021368949298335);
        SLOW_FALLING_JUMP_4.put(31, -0.21168923543844187);
        SLOW_FALLING_JUMP_4.put(33, -0.2227103501934522);
        SLOW_FALLING_JUMP_4.put(35, -0.23329502921617973);
        SLOW_FALLING_JUMP_4.put(37, -0.24346055534529398);
        SLOW_FALLING_JUMP_4.put(39, -0.2532235270197418);
        SLOW_FALLING_JUMP_4.put(41, -0.2625998853808511);
        SLOW_FALLING_JUMP_4.put(43, -0.27160494030138693);
        SLOW_FALLING_JUMP_4.put(45, -0.28025339538370986);
        SLOW_FALLING_JUMP_4.put(47, -0.2885593719680912);
        SLOW_FALLING_JUMP_4.put(49, -0.2965364321902513);
        SLOW_FALLING_JUMP_4.put(51, -0.3041976011258214);
        SLOW_FALLING_JUMP_4.put(53, -0.3115553880579398);
        SLOW_FALLING_JUMP_4.put(55, -0.31862180690262676);
        SLOW_FALLING_JUMP_4.put(57, -0.32540839582522096);
        SLOW_FALLING_JUMP_4.put(59, -0.3319262360801929);
        SLOW_FALLING_JUMP_4.put(61, -0.33818597010473184);
        SLOW_FALLING_JUMP_4.put(63, -0.34419781889592116);
        SLOW_FALLING_JUMP_4.put(65, -0.3499715986997245);
        SLOW_FALLING_JUMP_4.put(67, -0.3555167370391388);
        SLOW_FALLING_JUMP_4.put(69, -0.3608422881076052);
        SLOW_FALLING_JUMP_4.put(71, -0.3659569475528599);
        SLOW_FALLING_JUMP_4.put(73, -0.3708690666752972);
        SLOW_FALLING_JUMP_4.put(75, -0.37558666606410895);
        SLOW_FALLING_JUMP_4.put(77, -0.38011744869348263);
        SLOW_FALLING_JUMP_4.put(79, -0.3844688125001312);
        SLOW_FALLING_JUMP_4.put(81, -0.38864786246269034);
        SLOW_FALLING_JUMP_4.put(83, -0.39266142220296274);
        SLOW_FALLING_JUMP_4.put(85, -0.3965160451275693);
        SLOW_FALLING_JUMP_4.put(87, -0.40021802512846705);
    }

    static {
        SLOW_FALLING_JUMP_5.put(1, 0.6353692812536167);
        SLOW_FALLING_JUMP_5.put(2, 0.5442619062213794);
        SLOW_FALLING_JUMP_5.put(3, 0.45497667695204314);
        SLOW_FALLING_JUMP_5.put(4, 0.3674771505651222);
        SLOW_FALLING_JUMP_5.put(5, 0.2817276130370061);
        SLOW_FALLING_JUMP_5.put(6, 0.19769306462391967);
        SLOW_FALLING_JUMP_5.put(7, 0.1153392055762481);
        SLOW_FALLING_JUMP_5.put(8, 0.03463242213877038);
        SLOW_FALLING_JUMP_5.put(9, -0.044460227169324185);
        SLOW_FALLING_JUMP_5.put(11, -0.062103604400093104);
        SLOW_FALLING_JUMP_5.put(13, -0.07904830455211709);
        SLOW_FALLING_JUMP_5.put(15, -0.09532199521156315);
        SLOW_FALLING_JUMP_5.put(17, -0.11095124832928605);
        SLOW_FALLING_JUMP_5.put(19, -0.1259615836078325);
        SLOW_FALLING_JUMP_5.put(21, -0.14037751017048095);
        SLOW_FALLING_JUMP_5.put(23, -0.15422256658018796);
        SLOW_FALLING_JUMP_5.put(25, -0.1675193592736406);
        SLOW_FALLING_JUMP_5.put(27, -0.1802895994735394);
        SLOW_FALLING_JUMP_5.put(29, -0.19255413863891135);
        SLOW_FALLING_JUMP_5.put(31, -0.20433300251183084);
        SLOW_FALLING_JUMP_5.put(33, -0.2156454238157295);
        SLOW_FALLING_JUMP_5.put(35, -0.2265098736588982);
        SLOW_FALLING_JUMP_5.put(37, -0.23694409169443986);
        SLOW_FALLING_JUMP_5.put(39, -0.2469651150858425);
        SLOW_FALLING_JUMP_5.put(41, -0.25658930632556576);
        SLOW_FALLING_JUMP_5.put(43, -0.2658323799519877);
        SLOW_FALLING_JUMP_5.put(45, -0.27470942820835376);
        SLOW_FALLING_JUMP_5.put(47, -0.2832349456856207);
        SLOW_FALLING_JUMP_5.put(49, -0.29142285298951265);
        SLOW_FALLING_JUMP_5.put(51, -0.29928651947027163);
        SLOW_FALLING_JUMP_5.put(53, -0.3068387850523635);
        SLOW_FALLING_JUMP_5.put(55, -0.3140919811997236);
        SLOW_FALLING_JUMP_5.put(57, -0.32105795105081825);
        SLOW_FALLING_JUMP_5.put(59, -0.32774806875622176);
        SLOW_FALLING_JUMP_5.put(61, -0.3341732580505976);
        SLOW_FALLING_JUMP_5.put(63, -0.34034401008911175);
        SLOW_FALLING_JUMP_5.put(65, -0.34627040057759473);
        SLOW_FALLING_JUMP_5.put(67, -0.3519621062242777);
        SLOW_FALLING_JUMP_5.put(69, -0.35742842054013124);
        SLOW_FALLING_JUMP_5.put(71, -0.36267826901344336);
        SLOW_FALLING_JUMP_5.put(73, -0.36772022368346313);
        SLOW_FALLING_JUMP_5.put(75, -0.3725625171370268);
        SLOW_FALLING_JUMP_5.put(77, -0.3772130559508753);
        SLOW_FALLING_JUMP_5.put(79, -0.38167943360153345);
        SLOW_FALLING_JUMP_5.put(81, -0.3859689428642099);
        SLOW_FALLING_JUMP_5.put(83, -0.39008858772044164);
        SLOW_FALLING_JUMP_5.put(85, -0.39404509479436456);
        SLOW_FALLING_JUMP_5.put(87, -0.3978449243360842);
        SLOW_FALLING_JUMP_5.put(89, -0.40149428076999527);
        SLOW_FALLING_JUMP_5.put(91, -0.40499912282555783);
    }

    static {
        SLOW_FALLING_JUMP_6.put(1, 0.7294885091888403);
        SLOW_FALLING_JUMP_6.put(2, 0.6364987513930771);
        SLOW_FALLING_JUMP_6.put(3, 0.54536878697958);
        SLOW_FALLING_JUMP_6.put(4, 0.45606142011619966);
        SLOW_FALLING_JUMP_6.put(5, 0.3685401988866772);
        SLOW_FALLING_JUMP_6.put(6, 0.2827694004124055);
        SLOW_FALLING_JUMP_6.put(7, 0.19871401627167984);
        SLOW_FALLING_JUMP_6.put(8, 0.11633973821054155);
        SLOW_FALLING_JUMP_6.put(9, 0.03561294413945859);
        SLOW_FALLING_JUMP_6.put(11, -0.052429330298565446);
        SLOW_FALLING_JUMP_6.put(13, -0.06975713134333716);
        SLOW_FALLING_JUMP_6.put(15, -0.08639875211451908);
        SLOW_FALLING_JUMP_6.put(17, -0.10238136532530007);
        SLOW_FALLING_JUMP_6.put(19, -0.11773106765042485);
        SLOW_FALLING_JUMP_6.put(21, -0.1324729223372998);
        SLOW_FALLING_JUMP_6.put(23, -0.14663100012968755);
        SLOW_FALLING_JUMP_6.put(25, -0.16022841857078163);
        SLOW_FALLING_JUMP_6.put(27, -0.1732873797499508);
        SLOW_FALLING_JUMP_6.put(29, -0.185829206554601);
        SLOW_FALLING_JUMP_6.put(31, -0.19787437748665582);
        SLOW_FALLING_JUMP_6.put(33, -0.20944256010010065);
        SLOW_FALLING_JUMP_6.put(35, -0.22055264311453016);
        SLOW_FALLING_JUMP_6.put(37, -0.23122276725690938);
        SLOW_FALLING_JUMP_6.put(39, -0.24147035488215352);
        SLOW_FALLING_JUMP_6.put(41, -0.2513121384205306);
        SLOW_FALLING_JUMP_6.put(43, -0.2607641876987117);
        SLOW_FALLING_JUMP_6.put(45, -0.26984193617883534);
        SLOW_FALLING_JUMP_6.put(47, -0.2785602061585166);
        SLOW_FALLING_JUMP_6.put(49, -0.2869332329729133);
        SLOW_FALLING_JUMP_6.put(51, -0.29497468823848294);
        SLOW_FALLING_JUMP_6.put(53, -0.302697702176161);
        SLOW_FALLING_JUMP_6.put(55, -0.31011488505062346);
        SLOW_FALLING_JUMP_6.put(57, -0.3172383477605365);
        SLOW_FALLING_JUMP_6.put(59, -0.3240797216134439);
        SLOW_FALLING_JUMP_6.put(61, -0.330650177317537);
        SLOW_FALLING_JUMP_6.put(63, -0.336960443221372);
        SLOW_FALLING_JUMP_6.put(65, -0.3430208228313205);
        SLOW_FALLING_JUMP_6.put(67, -0.3488412116352748);
        SLOW_FALLING_JUMP_6.put(69, -0.3544311132601905);
        SLOW_FALLING_JUMP_6.put(71, -0.3597996549897289);
        SLOW_FALLING_JUMP_6.put(73, -0.3649556026674645);
        SLOW_FALLING_JUMP_6.put(75, -0.3699073750099302);
        SLOW_FALLING_JUMP_6.put(77, -0.3746630573527341);
        SLOW_FALLING_JUMP_6.put(79, -0.3792304148525574);
        SLOW_FALLING_JUMP_6.put(81, -0.3836169051661358);
        SLOW_FALLING_JUMP_6.put(83, -0.3878296906272851);
        SLOW_FALLING_JUMP_6.put(85, -0.39187564994165314);
        SLOW_FALLING_JUMP_6.put(87, -0.39576138941842487);
        SLOW_FALLING_JUMP_6.put(89, -0.3994932537571856);
        SLOW_FALLING_JUMP_6.put(91, -0.4030773364076481);
        SLOW_FALLING_JUMP_6.put(93, -0.4065194895191411);
        SLOW_FALLING_JUMP_6.put(95, -0.4098253334961015);
    }

}
