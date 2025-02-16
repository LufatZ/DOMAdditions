package de.additions.datagen.models

import de.additions.datagen.models.BlockModels.generateLanternModels
import de.additions.datagen.models.BlockModels.generateSlabModels
import de.additions.datagen.models.BlockModels.generateStairModels
import de.additions.datagen.models.BlockModels.generateTrapdoorModels
import de.additions.datagen.models.ItemModelsGenerator.generateItemsModels
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.client.data.BlockStateModelGenerator
import net.minecraft.client.data.ItemModelGenerator

/**
 * Comprehensive Model Generation Class
 *
 * Handles the intricate process of generating models and textures for various block types.
 * Supports complex scenarios like different textures for block sides, special block variants,
 * and custom texture mappings.
 *
 * Key Features:
 * - Dynamic model generation for stairs, slabs, trapdoors, and lanterns
 * - Intelligent texture mapping based on parent block characteristics
 * - Flexible handling of block-specific model generation rules
 *
 * @param generator The FabricDataOutput used for generating mod resources
 */
class ModelGenerator(generator: FabricDataOutput) : FabricModelProvider(generator) {
    companion object {
        /**
         * Configuration Lists for Special Block Texture Handling
         *
         * These lists and maps define special rules for texture generation for specific block types.
         * They help manage unique cases where block textures differ from standard generation methods.
         */
        val hasSideAndTop = listOf<Block>(
            Blocks.PODZOL, Blocks.MYCELIUM, Blocks.POLISHED_BASALT,
            Blocks.MUDDY_MANGROVE_ROOTS, Blocks.SMOOTH_RED_SANDSTONE,
            Blocks.QUARTZ_BLOCK, Blocks.BASALT, Blocks.SMOOTH_SANDSTONE,
            Blocks.DIRT_PATH, Blocks.GRASS_BLOCK
        )

        /**
         * Mapping for blocks with alternative texture sources
         *
         * Used when a block's texture should be derived from another block,
         * typically for smoothed or processed variants.
         */
        val hasNoTexture = mapOf<Block, Block>(
            Blocks.SMOOTH_RED_SANDSTONE to Blocks.RED_SANDSTONE,
            Blocks.SMOOTH_QUARTZ to Blocks.QUARTZ_BLOCK,
            Blocks.SMOOTH_SANDSTONE to Blocks.SANDSTONE
        )

        val snowyOvergrownBlocks = listOf<Block>(
            Blocks.GRASS_BLOCK, Blocks.PODZOL, Blocks.MYCELIUM
        )

        /**
         * Blocks where "_block" should be removed
         */
        val removeBlock = listOf<Block>(Blocks.MAGMA_BLOCK)

        /**
         * Blocks where bottom texture should match top texture
         */
        val bottomAllSide = listOf<Block>(Blocks.SMOOTH_QUARTZ)
    }

    /**
     * Generates block state models for all registered block variants.
     *
     * Calls specific generation methods for different block types:
     * - Stairs
     * - Slabs
     * - Trapdoors
     * - Lanterns
     *
     * @param generator The BlockStateModelGenerator used for creating block state models
     */
    override fun generateBlockStateModels(generator: BlockStateModelGenerator?) {
        with(generator) {
            // Generate models for registered blocks
            generateStairModels()
            generateSlabModels()
            generateTrapdoorModels()
            generateLanternModels()
        }
    }

    override fun generateItemModels(generator: ItemModelGenerator?) {
        with(generator) {
            // Generate models for registered items
            generateItemsModels()
        }
    }
}