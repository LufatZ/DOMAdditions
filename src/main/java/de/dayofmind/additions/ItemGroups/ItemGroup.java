package de.dayofmind.additions.ItemGroups;

import de.dayofmind.additions.config.DOMConfig;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import static de.dayofmind.additions.Additions.MOD_ID;
import static de.dayofmind.additions.block.DOMBlocksRegistry.*;
import static de.dayofmind.additions.item.DOMItemsRegistry.*;

public class ItemGroup {
    public static void DayOfMindBlocks(){
        FabricItemGroup.builder(new Identifier(MOD_ID, "blocks"))
                .icon(() -> new ItemStack(COPPER_LANTERN))
                .entries((displayContext, entries) -> {
                    if (DOMConfig.Features.EnabledLantern){
                        entries.add(NETHERITE_LANTERN);
                        entries.add(COPPER_LANTERN);
                    }
                    if (DOMConfig.Features.EnabledDirt){
                        entries.add(DIRT_STAIR);
                        entries.add(DIRT_SLAB);
                    }
                    if (DOMConfig.Features.EnabledGrass){
                        entries.add(GRASS_STAIR);
                        entries.add(GRASS_SLAB);
                    }
                    if (DOMConfig.Features.EnabledDirtPath) {
                        entries.add(DIRT_PATH_STAIR);
                        entries.add(DIRT_PATH_SLAB);
                    }
                    if (DOMConfig.Features.EnabledGold) {
                        entries.add(GOLD_STAIR);
                        entries.add(GOLD_SLAB);
                    }
                    if (DOMConfig.Features.EnabledIron) {
                        entries.add(IRON_STAIR);
                        entries.add(IRON_SLAB);
                    }
                    if (DOMConfig.Features.EnabledDia) {
                        entries.add(DIAMOND_STAIR);
                        entries.add(DIAMOND_SLAB);
                    }
                    if (DOMConfig.Features.EnabledBasalt) {
                        entries.add(SMOOTH_BASALT_STAIR);
                        entries.add(SMOOTH_BASALT_SLAB);

                        entries.add(POLISHED_BASALT_STAIR);
                        entries.add(POLISHED_BASALT_SLAB);
                    }
                    if (DOMConfig.Features.EnabledMagma) {
                        entries.add(MAGMA_STAIR);
                        entries.add(MAGMA_SLAB);
                    }
                    if (DOMConfig.Features.EnabledObsidian) {
                        entries.add(OBSIDIAN_STAIR);
                        entries.add(OBSIDIAN_SLAB);

                        entries.add(CRYING_OBSIDIAN_STAIR);
                        entries.add(CRYING_OBSIDIAN_SLAB);
                    }
                    if (DOMConfig.Features.EnabledDecorativeTrapdoor) {
                        entries.add(Decorative_Iron_Trapdoor);
                    }
                    if (DOMConfig.ExperimentalSettings.EnabledGuitar) {
                        entries.add(GUITAR_ITEM);
                    }
                    entries.add(REDSTONE_CHAIN);
                })
                .build();
    };
    public static void DayOfMindItems(){
        FabricItemGroup.builder(new Identifier(MOD_ID, "items"))
                .icon(() -> new ItemStack(COPPER_NUGGET))
                .entries((displayContext, entries) -> {
                    entries.add(COPPER_NUGGET);
                    entries.add(NETHERITE_NUGGET);
                })
                .build();
    }
}
