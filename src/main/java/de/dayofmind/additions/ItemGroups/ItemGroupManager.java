package de.dayofmind.additions.ItemGroups;


import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static de.dayofmind.additions.Additions.MOD_ID;
import static de.dayofmind.additions.block.DOMBlocksRegistry.*;
import static de.dayofmind.additions.block.DOMBlocksRegistry.GRASS_STAIR;
import static de.dayofmind.additions.config.DOMConfig.*;
import static de.dayofmind.additions.item.DOMItemsRegistry.*;
import static net.minecraft.item.Items.*;

public class ItemGroupManager {
    public static void ItemGroupRegister(){
        Registry.register(Registries.ITEM_GROUP, new Identifier(MOD_ID, "blocks"), DayOfMindBlocks);
        Registry.register(Registries.ITEM_GROUP, new Identifier(MOD_ID, "items"), DayOfMindItems);
        //Items to be listed in their original item groups
        if (OriginalItemGroup){
            modifyFunctionalGroup();
            modifyRedstoneGroup();
            modifyBuildingBlocksGroup();
            modifyNaturalGroup();
            modifyIngredientsGroup();
            modifyToolsGroup();
        }
    }

    private static final net.minecraft.item.ItemGroup DayOfMindBlocks = FabricItemGroup.builder()
            .icon(() -> new ItemStack(COPPER_LANTERN))
            .displayName(Text.translatable("itemGroup.additions.blocks"))
            .entries((displayContext, entries) -> {
                if (EnabledLantern){
                    entries.add(NETHERITE_LANTERN);
                    entries.add(COPPER_LANTERN);
                }
                if (EnabledRedstoneLantern){
                    entries.add(REDSTONE_CHAIN);
                    entries.add(REDSTONE_LANTERN);
                    entries.add(NETHERITE_REDSTONE_LANTERN);
                    entries.add(COPPER_REDSTONE_LANTERN);
                }
                if (EnabledBlockVariants){
                    entries.add(DIRT_STAIR);
                    entries.add(DIRT_SLAB);

                    entries.add(GRASS_STAIR);
                    entries.add(GRASS_SLAB);

                    entries.add(DIRT_PATH_STAIR);
                    entries.add(DIRT_PATH_SLAB);

                    entries.add(GOLD_STAIR);
                    entries.add(GOLD_SLAB);

                    entries.add(IRON_STAIR);
                    entries.add(IRON_SLAB);

                    entries.add(DIAMOND_STAIR);
                    entries.add(DIAMOND_SLAB);

                    entries.add(SMOOTH_BASALT_STAIR);
                    entries.add(SMOOTH_BASALT_SLAB);

                    entries.add(POLISHED_BASALT_STAIR);
                    entries.add(POLISHED_BASALT_SLAB);

                    entries.add(MAGMA_STAIR);
                    entries.add(MAGMA_SLAB);

                    entries.add(OBSIDIAN_STAIR);
                    entries.add(OBSIDIAN_SLAB);

                    entries.add(CRYING_OBSIDIAN_STAIR);
                    entries.add(CRYING_OBSIDIAN_SLAB);
                }
                if (EnabledDecorativeTrapdoor) {
                    entries.add(Decorative_Iron_Trapdoor);
                }
                if (EnabledInstruments) {
                    entries.add(GUITAR_ITEM);
                }
            })
            .build();
    private static final net.minecraft.item.ItemGroup DayOfMindItems = FabricItemGroup.builder()
            .icon(() -> new ItemStack(HAMMER))
            .displayName(Text.translatable("itemGroup.additions.items"))
            .entries((displayContext, entries) -> {
                entries.add(COPPER_NUGGET);
                entries.add(NETHERITE_NUGGET);
                if (EnabledTools){
                    entries.add(BIG_SHOVEL);
                    entries.add(HAMMER);
                }
    		})
            .build();
    private static void modifyFunctionalGroup() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(content -> {
            // Add items to the FUNCTIONAL group here...
            if (EnabledLantern) {
                content.addAfter(LANTERN, COPPER_LANTERN);
                content.addAfter(LANTERN, NETHERITE_LANTERN);
            }
            if (EnabledDecorativeTrapdoor) {
                content.add(Decorative_Iron_Trapdoor);
            }
            if (EnabledBlockVariants) {
                content.addAfter(MAGMA_BLOCK, MAGMA_STAIR);
                content.addAfter(MAGMA_STAIR, MAGMA_SLAB);

                content.addAfter(CRYING_OBSIDIAN, CRYING_OBSIDIAN_STAIR);
                content.addAfter(CRYING_OBSIDIAN_STAIR, CRYING_OBSIDIAN_SLAB);
            }
            if (EnabledInstruments) {
                content.addAfter(NOTE_BLOCK, GUITAR_ITEM);
            }
            if (EnabledRedstoneLantern){
                content.addAfter(CHAIN, REDSTONE_CHAIN);
            }
        });
    }

    private static void modifyRedstoneGroup() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(content -> {
            // Add items to the REDSTONE group here...
            if (EnabledRedstoneLantern) {
                content.addAfter(REDSTONE_LAMP, COPPER_REDSTONE_LANTERN);
                content.addAfter(REDSTONE_LAMP, NETHERITE_REDSTONE_LANTERN);
                content.addAfter(REDSTONE_LAMP, REDSTONE_LANTERN);
                content.addAfter(REDSTONE, REDSTONE_CHAIN);
            }
            if (EnabledInstruments) {
                content.addAfter(NOTE_BLOCK, GUITAR_ITEM);
            }
        });
    }

    private static void modifyBuildingBlocksGroup() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(content -> {
            // Add items to the BUILDING_BLOCKS group here...
            if (EnabledBlockVariants){
                content.addAfter(GOLD_BLOCK, GOLD_STAIR);
                content.addAfter(GOLD_STAIR, GOLD_SLAB);

                content.addAfter(IRON_BLOCK, IRON_STAIR);
                content.addAfter(IRON_STAIR, IRON_SLAB);

                content.addAfter(DIAMOND_BLOCK, DIAMOND_STAIR);
                content.addAfter(DIAMOND_STAIR, DIAMOND_SLAB);

                content.addAfter(SMOOTH_BASALT, SMOOTH_BASALT_STAIR);
                content.addAfter(SMOOTH_BASALT_STAIR, SMOOTH_BASALT_SLAB);

                content.addAfter(POLISHED_BASALT, POLISHED_BASALT_STAIR);
                content.addAfter(POLISHED_BASALT_STAIR, POLISHED_BASALT_SLAB);
            }
            if (EnabledDecorativeTrapdoor) {
                content.addAfter(IRON_TRAPDOOR, Decorative_Iron_Trapdoor);
            }
            if (EnabledRedstoneLantern) {
                content.addAfter(CHAIN, REDSTONE_CHAIN);
            }
        });
    }

    private static void modifyNaturalGroup() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL).register(content -> {
            // Add items to the NATURAL group here...
            if (EnabledBlockVariants){
                content.addAfter(DIRT, DIRT_STAIR);
                content.addAfter(DIRT_STAIR, DIRT_SLAB);

                content.addAfter(GRASS_BLOCK, GRASS_STAIR);
                content.addAfter(GRASS_STAIR, GRASS_SLAB);

                content.addAfter(DIRT_PATH, DIRT_PATH_STAIR);
                content.addAfter(DIRT_PATH_STAIR, DIRT_PATH_SLAB);

                content.addAfter(SMOOTH_BASALT, SMOOTH_BASALT_STAIR);
                content.addAfter(SMOOTH_BASALT_STAIR, SMOOTH_BASALT_SLAB);

                content.addAfter(MAGMA_BLOCK, MAGMA_STAIR);
                content.addAfter(MAGMA_STAIR, MAGMA_SLAB);

                content.addAfter(OBSIDIAN, OBSIDIAN_STAIR);
                content.addAfter(OBSIDIAN_STAIR, OBSIDIAN_SLAB);

                content.addAfter(CRYING_OBSIDIAN, CRYING_OBSIDIAN_STAIR);
                content.addAfter(CRYING_OBSIDIAN_STAIR, CRYING_OBSIDIAN_SLAB);
            }
        });
    }

    private static void modifyIngredientsGroup() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(content -> {
            // Add items to the INGREDIENTS group here...
            content.addAfter(IRON_NUGGET, COPPER_NUGGET);
            content.addAfter(IRON_NUGGET, NETHERITE_NUGGET);
        });
    }

    private static void modifyToolsGroup() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(content -> {
            // Add items to the TOOLS group here...
            if (EnabledTools){
                content.addAfter(IRON_SHOVEL, BIG_SHOVEL);
                content.addAfter(IRON_PICKAXE, HAMMER);
            }
            if (EnabledInstruments) {
                content.add(GUITAR_ITEM);
            }
        });
    }
}