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
import static de.dayofmind.additions.config.DOMConfig.*;
import static de.dayofmind.additions.item.DOMItemsRegistry.*;

public class ItemGroup {
    public static void itemGroupRegister(){
        Registry.register(Registries.ITEM_GROUP, new Identifier(MOD_ID, "blocks"), DayOfMindBlocks);
        Registry.register(Registries.ITEM_GROUP, new Identifier(MOD_ID, "items"), DayOfMindItems);
        //Items to be listed in their original item groups
        if (OriginalItemGroup){
            ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(content -> {
                if (EnabledLantern) {
                    content.add(COPPER_LANTERN);
                    content.add(NETHERITE_LANTERN);
                }
                if (EnabledDecorativeTrapdoor) {
                    content.add(Decorative_Iron_Trapdoor);
                }
            });
            ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(content -> {
                if (EnabledRedstoneLantern) {
                    content.add(COPPER_REDSTONE_LANTERN);
                    content.add(NETHERITE_REDSTONE_LANTERN);
                    content.add(REDSTONE_CHAIN);
                    content.add(REDSTONE_LANTERN);
                }
            });
            ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(content -> {
                if (EnabledBlockVariants){
                    content.add(DIRT_STAIR);
                    content.add(DIRT_SLAB);

                    content.add(GRASS_STAIR);
                    content.add(GRASS_SLAB);

                    content.add(DIRT_PATH_STAIR);
                    content.add(DIRT_PATH_SLAB);

                    content.add(GOLD_STAIR);
                    content.add(GOLD_SLAB);

                    content.add(IRON_STAIR);
                    content.add(IRON_SLAB);

                    content.add(DIAMOND_STAIR);
                    content.add(DIAMOND_SLAB);

                    content.add(SMOOTH_BASALT_STAIR);
                    content.add(SMOOTH_BASALT_SLAB);

                    content.add(POLISHED_BASALT_STAIR);
                    content.add(POLISHED_BASALT_SLAB);

                    content.add(MAGMA_STAIR);
                    content.add(MAGMA_SLAB);

                    content.add(OBSIDIAN_STAIR);
                    content.add(OBSIDIAN_SLAB);

                    content.add(CRYING_OBSIDIAN_STAIR);
                    content.add(CRYING_OBSIDIAN_SLAB);
                }
            });
            ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(content -> {
                content.add(COPPER_NUGGET);
                content.add(NETHERITE_NUGGET);
            });
            ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(content -> {
                if (EnabledTools){
                    content.add(BIG_SHOVEL);
                    content.add(HAMMER);
                }
                if (EnabledInstruments) {
                    content.add(GUITAR_ITEM);
                }
            });
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

}
