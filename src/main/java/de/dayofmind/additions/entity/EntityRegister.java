package de.dayofmind.additions.entity;

import de.dayofmind.additions.entity.mobs.wandering_musician.DOMWanderingMusician;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import static de.dayofmind.additions.Additions.MOD_ID;

public class EntityRegister {




    /*
     * Registers Entity's
     */
    public static final EntityType<DOMWanderingMusician> WANDERING_MUSICAN = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(MOD_ID, "wandering_musican"),
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, DOMWanderingMusician::new).dimensions(EntityDimensions.fixed(0.6f, 1.9f)).build()
    );

    /*
     * Registers Entity's Attributes
     */
    public static void registerAttributes(){
        FabricDefaultAttributeRegistry.register(EntityRegister.WANDERING_MUSICAN, DOMWanderingMusician.setAttributes());
    }
}
