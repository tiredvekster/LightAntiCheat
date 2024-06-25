package me.vekster.lightanticheat.event.packetrecive.packettype;

import java.lang.reflect.Field;

public class PacketTypeRecognizer {

    public static PacketType getPacketType(Object nmsPacket) {
        String className = nmsPacket.getClass().getName();
        className = className.split("\\.")[className.split("\\.").length - 1].split("\\$")[0];
        switch (className) {
            case "PacketPlayInFlying":
                return PacketType.FLYING;
            case "PacketPlayInArmAnimation":
                return PacketType.ARM_ANIMATION;
            case "PacketPlayInBlockDig":
                return PacketType.BLOCK_DIG;
            case "PacketPlayInSteerVehicle":
                return PacketType.STEER_VEHICLE;
            case "PacketPlayInSetCreativeSlot":
                return PacketType.SET_CREATIVE_SLOT;
            case "ServerboundClientInformationPacket":
                return PacketType.CLIENT_INFORMATION;
            case "ServerboundKeepAlivePacket":
                return PacketType.ALIVE;
            case "PacketPlayInUseEntity":
                return PacketType.USE_ENTITY;
            default:
                return PacketType.OTHER;
        }
    }

    public static int getEntityId(Object nmsPacket) {
        if (getPacketType(nmsPacket) != PacketType.USE_ENTITY)
            return 0;
        Field[] fields = nmsPacket.getClass().getDeclaredFields();
        if (fields.length > 4 + 1) {
            return 0;
        }
        for (Field field : fields) {
            boolean accessible = field.isAccessible();
            if (!accessible) {
                field.setAccessible(true);
            }
            try {
                Object object = field.get(nmsPacket);
                if (object instanceof Integer) {
                    int value = (int) object;
                    if (value != 0) {
                        if (!accessible) {
                            field.setAccessible(false);
                        }
                        return value;
                    }
                }
            } catch (IllegalAccessException ignored) {
            }
            if (!accessible) {
                field.setAccessible(false);
            }
        }
        return 0;
    }

}
