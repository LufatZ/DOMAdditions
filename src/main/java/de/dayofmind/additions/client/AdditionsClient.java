package de.dayofmind.additions.client;

import de.dayofmind.additions.client.block.TextureCutOut;
import de.dayofmind.additions.client.block.TintBlocks;
import de.dayofmind.additions.client.config.DOMConfigBuilder;
import de.dayofmind.additions.client.entity.mobs.wandering_musican.DOMWanderingMusicanRenderer;
import de.dayofmind.additions.entity.EntityRegister;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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

        //ENTITY
        //WanderingMusician
        EntityRendererRegistry.register(EntityRegister.WANDERING_MUSICAN, DOMWanderingMusicanRenderer::new);

        //ModMenuIntegration CONFIG
        //config
        AutoConfig.register(DOMConfigBuilder.ModConfig.class, Toml4jConfigSerializer::new);
    }
}
