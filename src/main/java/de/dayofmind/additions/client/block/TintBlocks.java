package de.dayofmind.additions.client.block;

import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.color.world.GrassColors;

import static de.dayofmind.additions.block.DOMBlocksRegister.GRASS_SLAB;
import static de.dayofmind.additions.block.DOMBlocksRegister.GRASS_STAIR;

public class TintBlocks {


    public static void TintGrassBlocks() {

        //grass block variants
        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> world != null && pos != null ? BiomeColors.getGrassColor(world, pos) : GrassColors.getColor(0.5, 1.0), GRASS_SLAB, GRASS_STAIR);
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> GrassColors.getColor(0.5D, 1.0D), GRASS_SLAB, GRASS_STAIR);
    }

}
