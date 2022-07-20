package de.dayofmind.additions.client;

import de.dayofmind.additions.client.block.TextureCutOut;
import de.dayofmind.additions.client.block.TintBlocks;
import de.dayofmind.additions.entity.EntityRegister;
import de.dayofmind.additions.entity.mobs.cube.CubeEntityModel;
import de.dayofmind.additions.entity.mobs.cube.CubeEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class AdditionsClient implements ClientModInitializer {

    public static final EntityModelLayer MODEL_CUBE_LAYER = new EntityModelLayer(new Identifier("entitytesting", "cube"), "main");
    @Override
    public void onInitializeClient() {
        TintBlocks.TintGrassBlocks();
        TextureCutOut.CutOut();

        // In 1.17, use EntityRendererRegistry.register (seen below) instead of EntityRendererRegistry.INSTANCE.register (seen above)
        EntityRendererRegistry.register(EntityRegister.CUBE, CubeEntityRenderer::new);

        EntityModelLayerRegistry.registerModelLayer(MODEL_CUBE_LAYER, CubeEntityModel::getTexturedModelData);
    }
}
