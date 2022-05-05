package de.dayofmind.additions.client;

import de.dayofmind.additions.block.TraverseBlocks;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;

import static de.dayofmind.additions.block.DOMBlocks.GRASS_SLAB;
import static de.dayofmind.additions.block.DOMBlocks.GRASS_STAIR;

public class TextureCutOut {
    public static void CutOut() {
        //slab
        BlockRenderLayerMap.INSTANCE.putBlock(GRASS_SLAB, RenderLayer.getCutoutMipped());
        //stair
        BlockRenderLayerMap.INSTANCE.putBlock(GRASS_STAIR, RenderLayer.getCutoutMipped());

        //TRAVERSE BLOCKS (TEMPORARY)
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), TraverseBlocks.FIR_DOOR, TraverseBlocks.FIR_TRAPDOOR);
    }
}
