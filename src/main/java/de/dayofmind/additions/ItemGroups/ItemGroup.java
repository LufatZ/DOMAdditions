package de.dayofmind.additions.ItemGroups;

import de.dayofmind.additions.config.DOMConfig;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import static de.dayofmind.additions.Additions.MOD_ID;
import static de.dayofmind.additions.block.DOMBlocksRegistry.*;
import static de.dayofmind.additions.item.DOMItemsRegistry.*;

public class ItemGroup {
    public static void DayOfMindBlocks(){
        FabricItemGroupBuilder.create(new Identifier(MOD_ID, "blocks"))
                .icon(() -> new ItemStack(COPPER_LANTERN))
                .appendItems((stacks) -> {
                    if (DOMConfig.Features.EnabledLantern){
                        stacks.add(new ItemStack(NETHERITE_LANTERN));
                        stacks.add(new ItemStack(COPPER_LANTERN));
                    }
                    if (DOMConfig.Features.EnabledDirt){
                        stacks.add(new ItemStack(DIRT_STAIR));
                        stacks.add(new ItemStack(DIRT_SLAB));
                    }
                    if (DOMConfig.Features.EnabledGrass){
                        stacks.add(new ItemStack(GRASS_STAIR));
                        stacks.add(new ItemStack(GRASS_SLAB));
                    }
                    if (DOMConfig.Features.EnabledDirtPath) {
                        stacks.add(new ItemStack(DIRT_PATH_STAIR));
                        stacks.add(new ItemStack(DIRT_PATH_SLAB));
                    }
                    if (DOMConfig.Features.EnabledGold) {
                        stacks.add(new ItemStack(GOLD_STAIR));
                        stacks.add(new ItemStack(GOLD_SLAB));
                    }
                    if (DOMConfig.Features.EnabledIron) {
                        stacks.add(new ItemStack(IRON_STAIR));
                        stacks.add(new ItemStack(IRON_SLAB));
                    }
                    if (DOMConfig.Features.EnabledDia) {
                        stacks.add(new ItemStack(DIAMOND_STAIR));
                        stacks.add(new ItemStack(DIAMOND_SLAB));
                    }
                    if (DOMConfig.Features.EnabledBasalt) {
                        stacks.add(new ItemStack(SMOOTH_BASALT_STAIR));
                        stacks.add(new ItemStack(SMOOTH_BASALT_SLAB));

                        stacks.add(new ItemStack(POLISHED_BASALT_STAIR));
                        stacks.add(new ItemStack(POLISHED_BASALT_SLAB));
                    }
                    if (DOMConfig.Features.EnabledMagma) {
                        stacks.add(new ItemStack(MAGMA_STAIR));
                        stacks.add(new ItemStack(MAGMA_SLAB));
                    }
                    if (DOMConfig.Features.EnabledObsidian) {
                        stacks.add(new ItemStack(OBSIDIAN_STAIR));
                        stacks.add(new ItemStack(OBSIDIAN_SLAB));

                        stacks.add(new ItemStack(CRYING_OBSIDIAN_STAIR));
                        stacks.add(new ItemStack(CRYING_OBSIDIAN_SLAB));
                    }
                    if (DOMConfig.Features.EnabledDecorativeTrapdoor) {
                        stacks.add(new ItemStack(Decorative_Iron_Trapdoor));
                    }
                    if (DOMConfig.ExperimentalSettings.EnabledGuitar) {
                        stacks.add(new ItemStack(GUITAR_ITEM));
                    }
                })
                .build();
    };
    public static void DayOfMindItems(){
        FabricItemGroupBuilder.create(new Identifier(MOD_ID, "items"))
                .icon(() -> new ItemStack(COPPER_NUGGET))
                .appendItems((stacks) -> {
                    stacks.add(new ItemStack(COPPER_NUGGET));
                    stacks.add(new ItemStack(NETHERITE_NUGGET));
                })
                .build();
    }
}
