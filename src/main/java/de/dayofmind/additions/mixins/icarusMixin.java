package de.dayofmind.additions.mixins;

import dev.cammiescorner.icarus.core.registry.ModItems;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import static dev.cammiescorner.icarus.Icarus.MOD_ID;
import static dev.cammiescorner.icarus.core.registry.ModItems.*;

@SuppressWarnings("MixinTargetIsPublic")
@Mixin(value = ModItems.class, remap = false)
public class icarusMixin {
    /**
     * @author lufatz
     * @reason updating to 1.19.4
     */
    @Overwrite
    public static void register() {
        FabricItemGroup.builder(new Identifier(MOD_ID, "general"))
                .icon(() -> new ItemStack(WHITE_FEATHERED_WINGS))
                .entries((displayContext, entries) -> {
                    entries.add(WHITE_FEATHERED_WINGS);
                    entries.add(ORANGE_FEATHERED_WINGS);
                    entries.add(MAGENTA_FEATHERED_WINGS);
                    entries.add(LIGHT_BLUE_FEATHERED_WINGS);
                    entries.add(YELLOW_FEATHERED_WINGS);
                    entries.add(LIME_FEATHERED_WINGS);
                    entries.add(PINK_FEATHERED_WINGS);
                    entries.add(GREY_FEATHERED_WINGS);
                    entries.add(LIGHT_GREY_FEATHERED_WINGS);
                    entries.add(CYAN_FEATHERED_WINGS);
                    entries.add(PURPLE_FEATHERED_WINGS);
                    entries.add(BLUE_FEATHERED_WINGS);
                    entries.add(BROWN_FEATHERED_WINGS);
                    entries.add(GREEN_FEATHERED_WINGS);
                    entries.add(RED_FEATHERED_WINGS);
                    entries.add(BLACK_FEATHERED_WINGS);
                    entries.add(WHITE_DRAGON_WINGS);
                    entries.add(ORANGE_DRAGON_WINGS);
                    entries.add(MAGENTA_DRAGON_WINGS);
                    entries.add(LIGHT_BLUE_DRAGON_WINGS);
                    entries.add(YELLOW_DRAGON_WINGS);
                    entries.add(LIME_DRAGON_WINGS);
                    entries.add(PINK_DRAGON_WINGS);
                    entries.add(GREY_DRAGON_WINGS);
                    entries.add(LIGHT_GREY_DRAGON_WINGS);
                    entries.add(CYAN_DRAGON_WINGS);
                    entries.add(PURPLE_DRAGON_WINGS);
                    entries.add(BLUE_DRAGON_WINGS);
                    entries.add(BROWN_DRAGON_WINGS);
                    entries.add(GREEN_DRAGON_WINGS);
                    entries.add(RED_DRAGON_WINGS);
                    entries.add(BLACK_DRAGON_WINGS);
                    entries.add(WHITE_MECHANICAL_FEATHERED_WINGS);
                    entries.add(ORANGE_MECHANICAL_FEATHERED_WINGS);
                    entries.add(MAGENTA_MECHANICAL_FEATHERED_WINGS);
                    entries.add(LIGHT_BLUE_MECHANICAL_FEATHERED_WINGS);
                    entries.add(YELLOW_MECHANICAL_FEATHERED_WINGS);
                    entries.add(LIME_MECHANICAL_FEATHERED_WINGS);
                    entries.add(PINK_MECHANICAL_FEATHERED_WINGS);
                    entries.add(GREY_MECHANICAL_FEATHERED_WINGS);
                    entries.add(LIGHT_GREY_MECHANICAL_FEATHERED_WINGS);
                    entries.add(CYAN_MECHANICAL_FEATHERED_WINGS);
                    entries.add(PURPLE_MECHANICAL_FEATHERED_WINGS);
                    entries.add(BLUE_MECHANICAL_FEATHERED_WINGS);
                    entries.add(BROWN_MECHANICAL_FEATHERED_WINGS);
                    entries.add(GREEN_MECHANICAL_FEATHERED_WINGS);
                    entries.add(RED_MECHANICAL_FEATHERED_WINGS);
                    entries.add(BLACK_MECHANICAL_FEATHERED_WINGS);
                    entries.add(WHITE_MECHANICAL_LEATHER_WINGS);
                    entries.add(ORANGE_MECHANICAL_LEATHER_WINGS);
                    entries.add(MAGENTA_MECHANICAL_LEATHER_WINGS);
                    entries.add(LIGHT_BLUE_MECHANICAL_LEATHER_WINGS);
                    entries.add(YELLOW_MECHANICAL_LEATHER_WINGS);
                    entries.add(LIME_MECHANICAL_LEATHER_WINGS);
                    entries.add(PINK_MECHANICAL_LEATHER_WINGS);
                    entries.add(GREY_MECHANICAL_LEATHER_WINGS);
                    entries.add(LIGHT_GREY_MECHANICAL_LEATHER_WINGS);
                    entries.add(CYAN_MECHANICAL_LEATHER_WINGS);
                    entries.add(PURPLE_MECHANICAL_LEATHER_WINGS);
                    entries.add(BLUE_MECHANICAL_LEATHER_WINGS);
                    entries.add(BROWN_MECHANICAL_LEATHER_WINGS);
                    entries.add(GREEN_MECHANICAL_LEATHER_WINGS);
                    entries.add(RED_MECHANICAL_LEATHER_WINGS);
                    entries.add(BLACK_MECHANICAL_LEATHER_WINGS);
                    entries.add(WHITE_LIGHT_WINGS);
                    entries.add(ORANGE_LIGHT_WINGS);
                    entries.add(MAGENTA_LIGHT_WINGS);
                    entries.add(LIGHT_BLUE_LIGHT_WINGS);
                    entries.add(YELLOW_LIGHT_WINGS);
                    entries.add(LIME_LIGHT_WINGS);
                    entries.add(PINK_LIGHT_WINGS);
                    entries.add(GREY_LIGHT_WINGS);
                    entries.add(LIGHT_GREY_LIGHT_WINGS);
                    entries.add(CYAN_LIGHT_WINGS);
                    entries.add(PURPLE_LIGHT_WINGS);
                    entries.add(BLUE_LIGHT_WINGS);
                    entries.add(BROWN_LIGHT_WINGS);
                    entries.add(GREEN_LIGHT_WINGS);
                    entries.add(RED_LIGHT_WINGS);
                    entries.add(BLACK_LIGHT_WINGS);
                    entries.add(FLANDRES_WINGS);
                    entries.add(DISCORDS_WINGS);
                    entries.add(ZANZAS_WINGS);
                })
                .build();ITEMS.keySet().forEach((item) -> {
            Registry.register(Registries.ITEM, (Identifier)ITEMS.get(item), item);
        });
    }

}
