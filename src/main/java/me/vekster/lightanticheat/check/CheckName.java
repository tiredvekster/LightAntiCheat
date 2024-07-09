package me.vekster.lightanticheat.check;

public enum CheckName {
    FLIGHT_A(CheckType.MOVEMENT, "Flight_A", "Acceleration"),
    FLIGHT_B(CheckType.MOVEMENT, "Flight_B", "Height"),
    FLIGHT_C(CheckType.MOVEMENT, "Flight_C", "Vector"),
    SPEED_A(CheckType.MOVEMENT, "Speed_A", "Horizontal"),
    SPEED_B(CheckType.MOVEMENT, "Speed_B", "Ground"),
    SPEED_C(CheckType.MOVEMENT, "Speed_C", "Prediction"),
    SPEED_D(CheckType.MOVEMENT, "Speed_D", "Liquid"),
    SPEED_E(CheckType.MOVEMENT, "Speed_E", "Limiter"),
    SPEED_F(CheckType.MOVEMENT, "Speed_F", "Legal"),
    NOFALL_A(CheckType.MOVEMENT, "NoFall_A", "FallDistance"),
    NOFALL_B(CheckType.MOVEMENT, "NoFall_B", "GroundSpoof"),
    JUMP_A(CheckType.MOVEMENT, "Jump_A", "Speed"),
    JUMP_B(CheckType.MOVEMENT, "Jump_B", "Height"),
    LIQUIDWALK_A(CheckType.MOVEMENT, "LiquidWalk_A", "Jesus"),
    LIQUIDWALK_B(CheckType.MOVEMENT, "LiquidWalk_B", "Jesus"),
    FASTCLIMB_A(CheckType.MOVEMENT, "FastClimb_A", "ClimbingSpeed"),
    NOSLOW_A(CheckType.MOVEMENT, "NoSlow_A", "Cobweb"),
    STEP_A(CheckType.MOVEMENT, "Step_A", "Step"),
    BOAT_A(CheckType.MOVEMENT, "Boat_A", "Boat"),
    VEHICLE_A(CheckType.MOVEMENT, "Vehicle_A", "Vehicle"),
    ELYTRA_A(CheckType.MOVEMENT, "Elytra_A", "Speed"),
    ELYTRA_B(CheckType.MOVEMENT, "Elytra_B", "Acceleration"),
    ELYTRA_C(CheckType.MOVEMENT, "Elytra_C", "Takeoff"),
    TRIDENT_A(CheckType.MOVEMENT, "Trident_A", "TridentBoost"),
    KILLAURA_A(CheckType.COMBAT, "KillAura_A", "AimBot"),
    KILLAURA_B(CheckType.COMBAT, "KillAura_B", "HitBox"),
    KILLAURA_C(CheckType.COMBAT, "KillAura_C", "ThroughBlock"),
    KILLAURA_D(CheckType.COMBAT, "KillAura_D", "Impossible"),
    REACH_A(CheckType.COMBAT, "Reach_A", "Horizontal"),
    REACH_B(CheckType.COMBAT, "Reach_B", "Hitbox"),
    CRITICALS_A(CheckType.COMBAT, "Criticals_A", "Packet"),
    CRITICALS_B(CheckType.COMBAT, "Criticals_B", "MiniJump"),
    AUTOCLICKER_A(CheckType.COMBAT, "AutoClicker_A", "Pattern"),
    AUTOCLICKER_B(CheckType.COMBAT, "AutoClicker_B", "Impossible"),
    VELOCITY_A(CheckType.COMBAT, "Velocity_A", "AntiKnockback"),
    AIRPLACE_A(CheckType.INTERACTION, "AirPlace_A", "AirPlaceA"),
    FASTPLACE_A(CheckType.INTERACTION, "FastPlace_A", "FastPlaceA"),
    BLOCKPLACE_A(CheckType.INTERACTION, "BlockPlace_A", "Rotation"),
    BLOCKPLACE_B(CheckType.INTERACTION, "BlockPlace_B", "Reach"),
    GHOSTBREAK_A(CheckType.INTERACTION, "GhostBreak_A", "ThroughBlock"),
    FASTBREAK_A(CheckType.INTERACTION, "FastBreak_A", "MiningSpeed"),
    BLOCKBREAK_A(CheckType.INTERACTION, "BlockBreak_A", "Rotation"),
    BLOCKBREAK_B(CheckType.INTERACTION, "BlockBreak_B", "Reach"),
    SCAFFOLD_A(CheckType.INTERACTION, "Scaffold_A", "Rotation"),
    SCAFFOLD_B(CheckType.INTERACTION, "Scaffold_B", "Sprint"),
    MOREPACKETS_A(CheckType.PACKET, "MorePackets_A", "PacketRate"),
    MOREPACKETS_B(CheckType.PACKET, "MorePackets_B", "Nuker"),
    TIMER_A(CheckType.PACKET, "Timer_A", "MovementTimer"),
    TIMER_B(CheckType.PACKET, "Timer_B", "Timer"),
    BADPACKETS_A(CheckType.PACKET, "BadPackets_A", "Protocol"),
    BADPACKETS_B(CheckType.PACKET, "BadPackets_B", "Impassible"),
    BADPACKETS_C(CheckType.PACKET, "BadPackets_C", "Impassible"),
    BADPACKETS_D(CheckType.PACKET, "BadPackets_D", "ArmAnimation"),
    SORTING_A(CheckType.INVENTORY, "Sorting_A", "InstantSorting"),
    ITEMSWAP_A(CheckType.INVENTORY, "ItemSwap_A", "WhileWalking"),
    AUTOBOT_A(CheckType.PLAYER, "AutoBot_A", "AutoBot"),
    SKINBLINKER_A(CheckType.PLAYER, "SkinBlinker_A", "SkinBlinker");

    public enum CheckType {
        MOVEMENT,
        COMBAT,
        INTERACTION,
        PACKET,
        INVENTORY,
        PLAYER
    }

    public final CheckType type;
    public final String title;
    public final String description;
    public final String group;
    public final Character check;

    CheckName(CheckType type, String title, String description) {
        this.type = type;
        this.title = title.replace("_", "");
        this.description = description;
        this.group = title.split("_", 2)[0];
        this.check = title.split("_", 2)[1].charAt(0);
    }
}
