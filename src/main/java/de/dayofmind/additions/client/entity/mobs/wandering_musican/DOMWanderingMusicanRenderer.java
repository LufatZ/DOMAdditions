package de.dayofmind.additions.client.entity.mobs.wandering_musican;

import de.dayofmind.additions.entity.mobs.wandering_musician.DOMWanderingMusician;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

import static de.dayofmind.additions.Additions.MOD_ID;

public class DOMWanderingMusicanRenderer extends GeoEntityRenderer<DOMWanderingMusician> {
    public DOMWanderingMusicanRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new DOMWanderingMusicanModel());
    }
    @Override
    public Identifier getTextureResource(DOMWanderingMusician instance) {
        return new Identifier(MOD_ID, "textures/mob/dom_wandering_musican.png");
    }
}
