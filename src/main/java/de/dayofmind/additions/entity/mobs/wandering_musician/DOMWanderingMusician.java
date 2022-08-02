package de.dayofmind.additions.entity.mobs.wandering_musician;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class DOMWanderingMusician extends PassiveEntity implements IAnimatable {

    private final AnimationFactory factory = new AnimationFactory(this);

    public DOMWanderingMusician(EntityType<? extends PassiveEntity> entityType, World world) {
        super(entityType, world);
    }

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    public static DefaultAttributeContainer.Builder setAttributes(){
        return PassiveEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3f);
    }

    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(2, new WanderAroundPointOfInterestGoal(this, 0.75f, false));
        this.goalSelector.add(3, new WanderAroundFarGoal(this, 0.35, 1));
        //this.goalSelector.add(4, new LookAroundGoal(this));
        this.goalSelector.add(9, new LookAtEntityGoal(this, PlayerEntity.class, 3.0F, 1.0F));
        this.goalSelector.add(10, new LookAtEntityGoal(this, MobEntity.class, 8.0F));
    }
    public boolean isWalking() {
        if (this.goalSelector.getRunningGoals().anyMatch(goal -> goal.getGoal() instanceof WanderAroundPointOfInterestGoal))
            return true;
        else {
            return this.goalSelector.getRunningGoals().anyMatch(goal -> goal.getGoal() instanceof WanderAroundFarGoal);
        }
    }
    //TODO why is walking not working???
    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        if (isWalking()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.musican.moving", true));
        }
        else {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.musican.idle", true));
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController(this, "controller",
                0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }
}
