package de.dayofmind.additions.client.entity.mobs.wandering_musican;

import de.dayofmind.additions.entity.mobs.wandering_musician.DOMWanderingMusician;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.model.provider.data.EntityModelData;

import static de.dayofmind.additions.Additions.MOD_ID;

public class DOMWanderingMusicanModel extends AnimatedGeoModel<DOMWanderingMusician> {
    @Override
    public Identifier getModelResource(DOMWanderingMusician object) {
        return new Identifier(MOD_ID, "geo/dom_wandering_musican.geo.json");
    }

    @Override
    public Identifier getTextureResource(DOMWanderingMusician object) {
        return new Identifier(MOD_ID, "textures/mob/dom_wandering_musican.png");
    }

    @Override
    public Identifier getAnimationResource(DOMWanderingMusician animatable) {
        return new Identifier(MOD_ID, "animations/dom_wandering_musican.animation.json");
    }

    @SuppressWarnings({ "unchecked"})
    @Override
    public void setLivingAnimations(DOMWanderingMusician entity, Integer uniqueID, AnimationEvent customPredicate) {
        super.setLivingAnimations(entity, uniqueID, customPredicate);
        IBone head = this.getAnimationProcessor().getBone("head");

        EntityModelData extraData = (EntityModelData) customPredicate.getExtraDataOfType(EntityModelData.class).get(0);
        if (head != null) {
            head.setRotationX(extraData.headPitch * ((float) Math.PI / 180F));
            head.setRotationY(extraData.netHeadYaw * ((float) Math.PI / 180F));
        }
    }
}
