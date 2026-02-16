package me.vekster.lightanticheat.event.playerattack;

import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.util.hook.server.folia.FoliaUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.Nullable;

public class LACAsyncPlayerAttackEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final LACPlayer lacPlayer;
    private final int entityId;
    private final EntityDamageEvent.DamageCause damageCause;

    public LACAsyncPlayerAttackEvent(Player player, LACPlayer lacPlayer, int entityId) {
        this(player, lacPlayer, entityId, null);
    }

    public LACAsyncPlayerAttackEvent(Player player, LACPlayer lacPlayer, int entityId,
                                     @Nullable EntityDamageEvent.DamageCause damageCause) {
        super(!FoliaUtil.isFolia());

        this.player = player;
        this.lacPlayer = lacPlayer;
        this.entityId = entityId;
        this.damageCause = damageCause;
    }

    public Player getPlayer() {
        return player;
    }

    public LACPlayer getLacPlayer() {
        return lacPlayer;
    }

    public int getEntityId() {
        return entityId;
    }

    public @Nullable EntityDamageEvent.DamageCause getDamageCause() {
        return damageCause;
    }

    public boolean hasDamageCause() {
        return damageCause != null;
    }

    public boolean isEntityAttackCause() {
        if (damageCause == null)
            return false;
        String causeName = damageCause.name();
        return causeName.equals("ENTITY_ATTACK") || causeName.equals("ENTITY_SWEEP_ATTACK");
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
