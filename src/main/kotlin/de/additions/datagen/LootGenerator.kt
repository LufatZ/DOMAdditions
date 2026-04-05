package de.additions.datagen

import de.additions.blocks.BlockRegistry
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider
import net.minecraft.core.HolderLookup
import java.util.concurrent.CompletableFuture

/**
 * Generates block loot tables for the mod's custom blocks.
 * This provider defines what items should drop when specific blocks are broken.
 *
 * It iterates through lists of registered blocks (trapdoors, lanterns, stairs, slabs)
 * and adds the appropriate loot drop definitions.
 *
 * @param generator The Fabric data output instance, used for generating data files.
 * @param registryLookup A future providing asynchronous access to the registry wrapper lookup,
 * necessary for accessing registry data during generation.
 */
class LootGenerator(
    generator: FabricPackOutput,
    registryLookup: CompletableFuture<HolderLookup.Provider>,
) : FabricBlockLootSubProvider(generator, registryLookup) {
    /**
     * Called by the data generation process to generate all loot tables.
     * This method defines the loot drops for various block types registered in [BlockRegistry].
     */
    override fun generate() {
        // Generate standard drops (block drops itself) for trapdoors.
        BlockRegistry.registeredTrapdoors.forEach { trapdoor ->
            dropSelf(trapdoor) // Adds the standard drop behavior (the block drops itself).
        }

        // Generate standard drops for lanterns.
        BlockRegistry.registeredLanterns.keys.toList().forEach { lantern ->
            dropSelf(lantern) // Adds the standard drop behavior.
        }

        // Generate standard drops for stairs.
        BlockRegistry.registeredStairs.forEach { stair ->
            dropSelf(stair) // Adds the standard drop behavior.
        }

        // Generate specific drops for slabs using the slabDrops helper method.
        BlockRegistry.registeredSlabs.forEach { slab ->
            // slabDrops(slab) creates the LootTable.Builder for slabs (drops 1, or 2 if double slab).
            // addDrop(block, builder) registers this specific builder for the given block.
            add(slab, createSlabItemTable(slab))
        }
    }
}
