package me.vekster.lightanticheat.util.detection.specific;

import me.vekster.lightanticheat.version.VerUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PassableUtil extends GroundUtil {

    private static final Set<Material> FLOWERS;

    static {
        FLOWERS = new HashSet<>(Arrays.asList(
                VerUtil.material.get("DANDELION"), VerUtil.material.get("POPPY"),
                VerUtil.material.get("BLUE_ORCHID"), VerUtil.material.get("ALLIUM"),
                VerUtil.material.get("AZURE_BLUET"), VerUtil.material.get("RED_TULIP"),
                VerUtil.material.get("ORANGE_TULIP"), VerUtil.material.get("WHITE_TULIP"),
                VerUtil.material.get("PINK_TULIP"), VerUtil.material.get("OXEYE_DAISY"),
                VerUtil.material.get("CORNFLOWER"), VerUtil.material.get("LILY_OF_THE_VALLEY"),
                VerUtil.material.get("SUNFLOWER"), VerUtil.material.get("LILAC"),
                VerUtil.material.get("ROSE_BUSH"), VerUtil.material.get("PEONY"),
                VerUtil.material.get("TORCHFLOWER"), VerUtil.material.get("PITCHER_PLANT")
        ));
    }

    public static boolean isActuallyPassable(Block block) {
        String downBlockName = block.getRelative(BlockFace.DOWN).getType().name().toLowerCase();
        if (downBlockName.endsWith("_wall") || downBlockName.endsWith("_fence") ||
                downBlockName.endsWith("_fence_gate") || downBlockName.endsWith("shulker_box") ||
                downBlockName.endsWith("_door"))
            return false;

        if (block.isEmpty())
            return true;
        if (!VerUtil.isPassable(block) || block.isLiquid())
            return false;

        Material material = block.getType();
        if (material == Material.STRING || material == Material.LEVER ||
                material == Material.TRIPWIRE_HOOK || material == Material.REDSTONE ||
                material == Material.ITEM_FRAME || material == VerUtil.material.get("GLOW_ITEM_FRAME") ||
                material == Material.PAINTING || material == Material.TORCH ||
                material == VerUtil.material.get("REDSTONE_TORCH") || material == VerUtil.material.get("SOUL_TORCH") ||
                material == VerUtil.material.get("MANGROVE_PROPAGULE") || material == Material.SUGAR_CANE ||
                material == Material.GRASS || material == VerUtil.material.get("TALL_GRASS") ||
                material == VerUtil.material.get("FERN") || material == VerUtil.material.get("LARGE_FERN"))
            return true;

        if (FLOWERS.contains(material))
            return true;

        if (material == Material.SNOW)
            return VerUtil.getSnowLayers(block) > 1;

        String typeName = material.name().toLowerCase();
        if (typeName.endsWith("_button") || typeName.endsWith("_pressure_plate") ||
                typeName.endsWith("_banner") || typeName.endsWith("_rail") ||
                typeName.endsWith("_sign") || typeName.endsWith("_sapling"))
            return true;
        return false;
    }

}
