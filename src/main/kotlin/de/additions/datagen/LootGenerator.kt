package de.additions.datagen

import de.additions.blocks.BlockRegistry
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider
import net.minecraft.registry.RegistryWrapper
import java.util.concurrent.CompletableFuture

class LootGenerator(
    generator: FabricDataOutput,
    registryLookup: CompletableFuture<RegistryWrapper.WrapperLookup>,
) : FabricBlockLootTableProvider(generator, registryLookup) {
    override fun generate() {
        BlockRegistry.registeredTrapdoors.forEach { trapdoor ->
            addDrop(trapdoor)
        }
        BlockRegistry.registeredLanterns.keys.toList().forEach { lantern ->
            addDrop(lantern)
        }
        BlockRegistry.registeredStairs.forEach { stair ->
            addDrop(stair)
        }
        BlockRegistry.registeredSlabs.forEach { slab ->
            slabDrops(slab)
        }
    }
}
