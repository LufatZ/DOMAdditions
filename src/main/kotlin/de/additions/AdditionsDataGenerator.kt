package de.additions

import com.google.common.base.Supplier
import com.google.gson.JsonObject
import de.additions.blocks.BlockRegistry
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.data.client.BlockStateModelGenerator
import net.minecraft.data.client.ItemModelGenerator
import net.minecraft.data.client.ModelIds
import net.minecraft.data.client.Models
import net.minecraft.data.client.TextureKey
import net.minecraft.data.client.TextureMap
import net.minecraft.util.Identifier

object AdditionsDataGenerator : DataGeneratorEntrypoint {
	override fun onInitializeDataGenerator(generator: FabricDataGenerator) {
		val pack: FabricDataGenerator.Pack = generator.createPack()
		pack.addProvider(::ModelGenerator)
	}

	class ModelGenerator(generator: FabricDataOutput) : FabricModelProvider(generator) {
		override fun generateBlockStateModels(p0: BlockStateModelGenerator?) {
			BlockRegistry.registeredLanterns.forEach { lantern ->
				//p0?.registerLantern(lantern)
			}
			BlockRegistry.registeredStairs.forEach { stair ->
				//p0?.registerSimpleCubeAll(stair)
			}
			BlockRegistry.registeredSlabs.forEachIndexed { index, slab ->
				//bestimmte Blöcke werden manuell erstellt
				if (BlockRegistry.blockVariantsParents[index] !in listOf<Block>(Blocks.GRASS_BLOCK,Blocks.DIRT_PATH)) {
					val parentId =
						BlockRegistry.blockVariantsParents[index].defaultState.registryEntry.idAsString.replace(
							"minecraft:",
							""
						)

					// Erstelle die TextureMap für das Slab mit dem "all" texture slot
					val textureMap = TextureMap().put(TextureKey.ALL, Identifier.of("block/$parentId"))

					// Bottom Slab Model
					val bottomModel = Models.SLAB.upload(
						slab,
						"",
						textureMap,
						p0?.modelCollector
					)

					// Top Slab Model
					val topModel = Models.SLAB_TOP.upload(
						slab,
						"",
						textureMap,
						p0?.modelCollector
					)

					// Verwende den existierenden Full Block statt einen neuen zu erstellen
					val fullBlockId = Identifier.of("block/$parentId")

					// Registriere den BlockState
					p0?.blockStateCollector?.accept(
						BlockStateModelGenerator.createSlabBlockState(
							slab,
							bottomModel,
							topModel,
							fullBlockId  // Verwende direkt die ID des existierenden Full Blocks
						)
					)
				}
			}

			BlockRegistry.registeredChains.forEach { chain ->
				//p0?.registerSimpleCubeAll(chain)
			}
			BlockRegistry.registeredTrapdoors.forEachIndexed { index, trapdoor ->
				//p0?.registerParentedTrapdoor(BlockRegistry.trapdoorVariantsParents[index], trapdoor)
			}
		}

		override fun generateItemModels(p0: ItemModelGenerator?) {
			BlockRegistry.registeredSlabs.forEach { slab ->
				// Erzeuge den Pfad für das Blockmodell
				val parentId = slab.defaultState.registryEntry.idAsString.split(":").toMutableList()
				parentId.add(1, ":block/")
				val parentPath: String = parentId.joinToString("")

				// Erstelle das JSON-Objekt für das Modell
				val jsonObject = JsonObject().apply {
					addProperty("parent", parentPath)
				}

				// Lade das Modell manuell hoch
				p0?.writer?.accept(
					ModelIds.getItemModelId(slab.asItem()), // Modell-ID
					Supplier { jsonObject } // JSON-Inhalt
				)
			}
		}
	}
}