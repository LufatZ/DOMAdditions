package de.dayofmind.additions.client.block;

import de.dayofmind.additions.block.DOMBlocksRegistry;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;

import static de.dayofmind.additions.block.DOMBlocksRegistry.GRASS_SLAB;
import static de.dayofmind.additions.block.DOMBlocksRegistry.GRASS_STAIR;

public class TextureCutOut {
    public static void CutOut() {
        //slab
        BlockRenderLayerMap.INSTANCE.putBlock(GRASS_SLAB, RenderLayer.getCutoutMipped());
        //stair
        BlockRenderLayerMap.INSTANCE.putBlock(GRASS_STAIR, RenderLayer.getCutoutMipped());
        //lanterns
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), DOMBlocksRegistry.COPPER_LANTERN, DOMBlocksRegistry.NETHERITE_LANTERN);
    }
}
