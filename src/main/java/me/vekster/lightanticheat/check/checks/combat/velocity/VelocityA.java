package me.vekster.lightanticheat.check.checks.combat.velocity;

import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.buffer.Buffer;
import me.vekster.lightanticheat.check.checks.combat.CombatCheck;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.player.cache.PlayerCache;
import me.vekster.lightanticheat.player.cache.history.HistoryElement;
import me.vekster.lightanticheat.util.async.AsyncUtil;
import me.vekster.lightanticheat.util.hook.plugin.simplehook.EnchantsSquaredHook;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import me.vekster.lightanticheat.version.VerUtil;
import me.vekster.lightanticheat.version.identifier.LACVersion;
import me.vekster.lightanticheat.version.identifier.VerIdentifier;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Anti-Knockback
 */
public class VelocityA extends CombatCheck implements Listener {
    public VelocityA() {
        super(CheckName.VELOCITY_A);
    }

    private static Optional<Boolean> isPandaSpigot = Optional.empty();
    private static final Set<Material> NETHERITE_ARMOR = new HashSet<>();

    static {
        NETHERITE_ARMOR.add(VerUtil.material.get("NETHERITE_HELMET"));
        NETHERITE_ARMOR.add(VerUtil.material.get("NETHERITE_CHESTPLATE"));
        NETHERITE_ARMOR.add(VerUtil.material.get("NETHERITE_LEGGINGS"));
        NETHERITE_ARMOR.add(VerUtil.material.get("NETHERITE_BOOTS"));
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (isPandaSpigot()) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK)
            return;
        if (!(event.getEntity() instanceof Player))
            return;
        Player player = (Player) event.getEntity();
        if (isExternalNPC(player))
            return;
        LACPlayer lacPlayer = LACPlayer.getLacPlayer(player);
        PlayerCache cache = lacPlayer.cache;
        Buffer buffer = getBuffer(player);

        Scheduler.entityThread(player, () -> {
            if (!isCheckAllowed(player, lacPlayer))
                return;
            if (!isConditionAllowed(player, lacPlayer, cache))
                return;

            if (System.currentTimeMillis() - buffer.getLong("lastCheck") < 350)
                return;
            buffer.put("lastCheck", System.currentTimeMillis());

            if (!cache.history.onEvent.onGround.get(HistoryElement.SECOND).towardsFalse &&
                    !cache.history.onEvent.onGround.get(HistoryElement.FIRST).towardsFalse &&
                    !cache.history.onEvent.onGround.get(HistoryElement.FROM).towardsFalse)
                return;
            if (!cache.history.onPacket.onGround.get(HistoryElement.SECOND).towardsFalse &&
                    !cache.history.onPacket.onGround.get(HistoryElement.FIRST).towardsFalse &&
                    !cache.history.onPacket.onGround.get(HistoryElement.FROM).towardsFalse)
                return;

            buffer.put("lastHit", System.currentTimeMillis());
            buffer.put("lastLocation", player.getLocation().clone());
        });
    }

    @EventHandler
    public void onVelocity(PlayerVelocityEvent event) {
        if (isPandaSpigot()) return;
        if (isExternalNPC(event)) return;
        Player player = event.getPlayer();
        LACPlayer lacPlayer = LACPlayer.getLacPlayer(player);
        PlayerCache cache = lacPlayer.cache;
        Buffer buffer = getBuffer(player);
        if (!buffer.isExists("lastHit") || System.currentTimeMillis() - buffer.getLong("lastHit") > 300)
            return;
        buffer.put("lastHit", 0L);

        Scheduler.entityThread(player, () -> {
            if (!isCheckAllowed(player, lacPlayer))
                return;
            if (!isConditionAllowed(player, lacPlayer, cache))
                return;

            if (!cache.history.onEvent.onGround.get(HistoryElement.SECOND).towardsFalse &&
                    !cache.history.onEvent.onGround.get(HistoryElement.FIRST).towardsFalse &&
                    !cache.history.onEvent.onGround.get(HistoryElement.FROM).towardsFalse)
                return;
            if (!cache.history.onPacket.onGround.get(HistoryElement.SECOND).towardsFalse &&
                    !cache.history.onPacket.onGround.get(HistoryElement.FIRST).towardsFalse &&
                    !cache.history.onPacket.onGround.get(HistoryElement.FROM).towardsFalse)
                return;

            for (Entity entity : AsyncUtil.getNearbyEntities(player, 2, 3, 2)) {
                if (entity instanceof Vehicle)
                    return;
            }

            Vector velocity = player.getVelocity();
            if (Math.abs(velocity.getX()) < 0.2 && Math.abs(velocity.getZ()) < 0.2)
                return;

            buffer.put("lastVelocityChange", System.currentTimeMillis());
            detect(player, false);
        });
    }

    private void detect(Player player1, boolean finalCall) {
        UUID uuid = player1.getUniqueId();
        Scheduler.runTaskLater(player1, () -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) return;
            LACPlayer lacPlayer = LACPlayer.getLacPlayer(player);
            PlayerCache cache = lacPlayer.cache;
            Buffer buffer = getBuffer(player);

            if (!buffer.isExists("lastVelocityChange") ||
                    System.currentTimeMillis() - buffer.getLong("lastVelocityChange") > 300)
                return;
            if (finalCall)
                buffer.put("lastVelocityChange", 0L);

            if (!buffer.isExists("lastLocation") ||
                    buffer.getLocation("lastLocation") == null)
                return;

            if (!isCheckAllowed(player, lacPlayer))
                return;
            if (!isConditionAllowed(player, lacPlayer, cache))
                return;

            double speed = distance(buffer.getLocation("lastLocation"), player.getLocation());
            double hSpeed = distanceHorizontal(buffer.getLocation("lastLocation"), player.getLocation());

            if (speed >= 0.00045 || hSpeed >= 0.000045)
                return;

            if (!finalCall) {
                detect(player, true);
                return;
            }

            buffer.put("flags", buffer.getInt("flags") + 1);
            if (buffer.getInt("flags") <= 1)
                return;

            if (EnchantsSquaredHook.hasEnchantment(player, "Steady", "Burden"))
                return;

            if (getItemStackAttributes(player, "GENERIC_KNOCKBACK_RESISTANCE") != 0 ||
                    getPlayerAttributes(player).getOrDefault("GENERIC_KNOCKBACK_RESISTANCE", 0.0) > 0.41)
                buffer.put("attribute", System.currentTimeMillis());
            if (System.currentTimeMillis() - buffer.getLong("attribute") < 3500)
                return;

            callViolationEvent(player, lacPlayer, null);
        }, 1);
    }

    private static boolean isConditionAllowed(Player player, LACPlayer lacPlayer, PlayerCache cache) {
        for (Block block : getWithinBlocks(player)) {
            if (!isActuallyPassable(block) || !isActuallyPassable(block.getRelative(BlockFace.UP)))
                return false;
        }
        Location location = player.getLocation();
        for (Block block : getCollisionBlockLayer(player, location)) {
            if (!isActuallyPassable(block) || !isActuallyPassable(block.getRelative(BlockFace.UP)))
                return false;
        }

        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE)
            return false;
        if (player.hasPotionEffect(VerUtil.potions.get("LEVITATION")))
            return false;

        ItemStack[] armor = player.getInventory().getArmorContents();
        for (ItemStack itemStack : armor) {
            if (itemStack != null && NETHERITE_ARMOR.contains(itemStack.getType()))
                return false;
        }

        if (player.isFlying() || player.isInsideVehicle() || lacPlayer.isGliding() || lacPlayer.isRiptiding() ||
                lacPlayer.isClimbing() || lacPlayer.isInWater())
            return false;
        if (cache.flyingTicks >= -3 || cache.climbingTicks >= -2 ||
                cache.glidingTicks >= -3 || cache.riptidingTicks >= -3)
            return false;
        long time = System.currentTimeMillis();
        if (time - cache.lastInsideVehicle <= 150 || time - cache.lastInWater <= 150 ||
                time - cache.lastWasFished <= 350 || time - cache.lastTeleport <= 700 ||
                time - cache.lastRespawn <= 500 || time - cache.lastPowderSnowWalk <= 500 ||
                time - cache.lastSlimeBlock <= 2000 || time - cache.lastHoneyBlock <= 1000 ||
                time - cache.lastFlight <= 750 ||
                time - cache.lastGliding <= 2000 || time - cache.lastRiptiding <= 3500)
            return false;
        return true;
    }

    private static boolean isPandaSpigot() {
        if (VerIdentifier.getVersion() != LACVersion.V1_8)
            return false;
        if (isPandaSpigot.isPresent())
            return isPandaSpigot.get();
        isPandaSpigot = Optional.of(Bukkit.getServer().getName().contains("PandaSpigot"));
        return isPandaSpigot.get();
    }

}
