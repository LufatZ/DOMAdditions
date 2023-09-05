package de.dayofmind.additions.ItemGroups;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
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
    }

    private static final net.minecraft.item.ItemGroup DayOfMindBlocks = FabricItemGroup.builder()
            .icon(() -> new ItemStack(COPPER_LANTERN))
            .displayName(Text.translatable("itemGroup.additions.blocks"))
            .entries((displayContext, entries) -> {
                if (EnabledTools){
                    entries.add(BIG_SHOVEL);
                }
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
            .icon(() -> new ItemStack(COPPER_NUGGET))
            .displayName(Text.translatable("itemGroup.additions.items"))
            .entries((displayContext, entries) -> {
                entries.add(COPPER_NUGGET);
                entries.add(NETHERITE_NUGGET);
    		})
            .build();

}
