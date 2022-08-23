package de.dayofmind.additions.client.block;

import de.dayofmind.additions.block.DOMBlocksRegister;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;

import static de.dayofmind.additions.block.DOMBlocksRegister.GRASS_SLAB;
import static de.dayofmind.additions.block.DOMBlocksRegister.GRASS_STAIR;

public class TextureCutOut {
    public static void CutOut() {
        //slab
        BlockRenderLayerMap.INSTANCE.putBlock(GRASS_SLAB, RenderLayer.getCutoutMipped());
        //stair
        BlockRenderLayerMap.INSTANCE.putBlock(GRASS_STAIR, RenderLayer.getCutoutMipped());
        //lanterns
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), DOMBlocksRegister.COPPER_LANTERN, DOMBlocksRegister.NETHERITE_LANTERN);
    }
}
