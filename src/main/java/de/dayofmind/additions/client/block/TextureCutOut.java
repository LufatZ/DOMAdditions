package de.dayofmind.additions.client.block;

import de.dayofmind.additions.block.DOMBlocksRegistry;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;

import static de.dayofmind.additions.block.DOMBlocksRegistry.*;

public class TextureCutOut {
    public static void CutOut() {
        //slab
        BlockRenderLayerMap.INSTANCE.putBlock(GRASS_SLAB, RenderLayer.getCutoutMipped());
        //stair
        BlockRenderLayerMap.INSTANCE.putBlock(GRASS_STAIR, RenderLayer.getCutoutMipped());
        //lanterns
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), COPPER_LANTERN, NETHERITE_LANTERN, COPPER_REDSTONE_LANTERN, NETHERITE_REDSTONE_LANTERN, REDSTONE_LANTERN);
        //trapdoors
        BlockRenderLayerMap.INSTANCE.putBlock(Decorative_Iron_Trapdoor, RenderLayer.getCutoutMipped());
        //chain
        BlockRenderLayerMap.INSTANCE.putBlock(REDSTONE_CHAIN, RenderLayer.getCutoutMipped());
    }
}
