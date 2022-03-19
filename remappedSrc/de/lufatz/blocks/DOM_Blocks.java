package de.lufatz.blocks;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.block.SlabBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

//register mod items and mod blocks
public class DOM_Blocks implements ModInitializer {

    // missing_blocks

    // missing_slabs //TODO requires tool and loottables
    public static final Block DRIPSTONE_SLAB = new SlabBlock(FabricBlockSettings.of(Material.STONE, MapColor.TERRACOTTA_BROWN).sounds(BlockSoundGroup.DRIPSTONE_BLOCK).hardness(1.5f).strength(1.0f).requiresTool());

    public static final Block DIRT_SLAB = new SlabBlock(FabricBlockSettings.of(Material.SOIL, MapColor.DIRT_BROWN).strength(0.5f).sounds(BlockSoundGroup.GRAVEL));

    public static final Block DIRT_PATH_SLAB = new ShortenedCubeSlab(FabricBlockSettings.of(Material.SOIL).strength(0.65f).sounds(BlockSoundGroup.GRASS));

    public static final Block GRASS_SLAB = new GrassSlab(FabricBlockSettings.of(Material.SOLID_ORGANIC).ticksRandomly().strength(0.6F).sounds(BlockSoundGroup.GRASS));

    public static final  Block FARMLAND_SLAB = new FarmLandSlab(FabricBlockSettings.of(Material.SOIL).ticksRandomly().strength(0.6F).sounds(BlockSoundGroup.GRAVEL));

        //TODO make it (GOLD_SLAB/IRON_SLAB)
    public static final  Block GOLD_SLAB = new SlabBlock(FabricBlockSettings.of(Material.METAL, MapColor.GOLD).requiresTool().strength(3.0F, 6.0F).sounds(BlockSoundGroup.METAL));

    public static final  Block IRON_SLAB = new SlabBlock(FabricBlockSettings.of(Material.METAL, MapColor.IRON_GRAY).requiresTool().strength(5.0F, 6.0F).sounds(BlockSoundGroup.METAL));

    // missing_stairs

         //TODO stairs are private
    public static final  Block GOLD_STAIR = new NewStairBlock(FabricBlockSettings.of(Material.METAL, MapColor.GOLD).requiresTool().strength(3.0F, 6.0F).sounds(BlockSoundGroup.METAL));

    public static final  Block IRON_STAIR = new NewStairBlock(FabricBlockSettings.of(Material.METAL, MapColor.IRON_GRAY).requiresTool().strength(5.0F, 6.0F).sounds(BlockSoundGroup.METAL));

    // missing_decoration

    @Override
    public void onInitialize() {
        System.out.println("DayOfMind Blocks is loading");
        // missing_blocks

        // missing_slaps
        Registry.register(Registry.BLOCK, new Identifier("blocks", "dripstone_slab"), DRIPSTONE_SLAB);
        Registry.register(Registry.ITEM, new Identifier("blocks", "dripstone_slab"), new BlockItem(DRIPSTONE_SLAB, new FabricItemSettings().group(ItemGroup.BUILDING_BLOCKS)));

        Registry.register(Registry.BLOCK, new Identifier("blocks", "dirt_slab"), DIRT_SLAB);
        Registry.register(Registry.ITEM, new Identifier("blocks", "dirt_slab"), new BlockItem(DIRT_SLAB, new FabricItemSettings().group(ItemGroup.BUILDING_BLOCKS)));

        Registry.register(Registry.BLOCK, new Identifier("blocks", "dirt_path_slab"), DIRT_PATH_SLAB);
        Registry.register(Registry.ITEM, new Identifier("blocks", "dirt_path_slab"), new BlockItem(DIRT_PATH_SLAB, new FabricItemSettings().group(ItemGroup.BUILDING_BLOCKS)));

        Registry.register(Registry.BLOCK, new Identifier("blocks", "grass_slab"), GRASS_SLAB);
        Registry.register(Registry.ITEM, new Identifier("blocks", "grass_slab"), new BlockItem(GRASS_SLAB, new FabricItemSettings().group(ItemGroup.BUILDING_BLOCKS)));

        Registry.register(Registry.BLOCK, new Identifier("blocks", "farmland_slab"), FARMLAND_SLAB);
        Registry.register(Registry.ITEM, new Identifier("blocks", "farmland_slab"), new BlockItem(FARMLAND_SLAB, new FabricItemSettings().group(ItemGroup.BUILDING_BLOCKS)));

        // missing_decoration
    }


}
