package de.dayofmind.additions.unused.client;

import de.dayofmind.additions.unused.block.TraverseBlocks;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;

public class TextureCutOut {
    public static void CutOut() {
        //TRAVERSE BLOCKS (TEMPORARY)
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), TraverseBlocks.FIR_DOOR, TraverseBlocks.FIR_TRAPDOOR);
    }
}
