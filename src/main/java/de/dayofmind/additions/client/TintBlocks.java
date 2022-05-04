package de.dayofmind.additions.client;

import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.color.world.GrassColors;

import static de.dayofmind.additions.block.DOMBlocks.GRASS_SLAB;
import static de.dayofmind.additions.block.DOMBlocks.GRASS_STAIR;
import static net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry.ITEM;
import static net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry.BLOCK;

public class TintBlocks {


    public static void TintGrassBlocks() {

        //grass block variants
        BLOCK.register((state, world, pos, tintIndex) -> world != null && pos != null ? BiomeColors.getGrassColor(world, pos) : GrassColors.getColor(0.5, 1.0), GRASS_SLAB, GRASS_STAIR);
        ITEM.register((stack, tintIndex) -> GrassColors.getColor(0.5D, 1.0D), GRASS_SLAB, GRASS_STAIR);
    }

}
