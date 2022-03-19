package de.lufatz.blocks;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ShovelItem;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Blocks implements ModInitializer {

    // missing_blocks

    // missing_slabs
    public static final Block dripstone_slab = new SlabBlock(FabricBlockSettings.of(Material.STONE, MapColor.TERRACOTTA_BROWN).sounds(BlockSoundGroup.DRIPSTONE_BLOCK).hardness(1.5f).strength(1.0f).requiresTool()); //TODO requires tool

    public static final Block dirt_slab = new SlabBlock(FabricBlockSettings.of(Material.SOIL, MapColor.DIRT_BROWN).strength(0.5f).sounds(BlockSoundGroup.GRAVEL)); //TODO requires tool and loottables

    public static final Block dirt_path_slab = new FarmLandSlab(FabricBlockSettings.of(Material.SOIL).strength(0.65f).sounds(BlockSoundGroup.GRASS)); //TODO requires tool and loottables

    @Override
    public void onInitialize() {
        // missing_blocks

        // missing_slaps
        Registry.register(Registry.BLOCK, new Identifier("blocks", "dripstone_slab"), dripstone_slab);
        Registry.register(Registry.ITEM, new Identifier("blocks", "dripstone_slab"), new BlockItem(dripstone_slab, new FabricItemSettings().group(ItemGroup.BUILDING_BLOCKS)));

        Registry.register(Registry.BLOCK, new Identifier("blocks", "dirt_slab"), dirt_slab);
        Registry.register(Registry.ITEM, new Identifier("blocks", "dirt_slab"), new BlockItem(dirt_slab, new FabricItemSettings().group(ItemGroup.BUILDING_BLOCKS)));

        Registry.register(Registry.BLOCK, new Identifier("blocks", "dirt_path_slab"), dirt_path_slab);
        Registry.register(Registry.ITEM, new Identifier("blocks", "dirt_path_slab"), new BlockItem(dirt_path_slab, new FabricItemSettings().group(ItemGroup.BUILDING_BLOCKS)));
    }
}
