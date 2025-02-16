package de.additions

import de.additions.blocks.BlockRegistry
import de.additions.config.AdditionsConfig
import de.additions.itemGroups.ItemGroupRegistry
import de.additions.items.ItemRegistry
import eu.midnightdust.lib.config.MidnightConfig
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.impl.FabricLoaderImpl
import org.slf4j.LoggerFactory

object Additions : ModInitializer {
    val logger = LoggerFactory.getLogger("additions")
	const val MODID = "additions"

	override fun onInitialize() {
		logger.info("Enjoy DayOfMind")
		MidnightConfig.init(MODID, AdditionsConfig::class.java)
		BlockRegistry.registerAllBlocks()
		ItemRegistry.registerItems()
		ItemGroupRegistry.registerItemGroups()
		if (FabricLoaderImpl.INSTANCE.isDevelopmentEnvironment) {
			val blocksString = BlockRegistry.registeredBlocks.withIndex().joinToString("\n") { (index, block) -> "$index.) $block" }
			val itemsString = ItemRegistry.registeredItems.withIndex().joinToString("\n") { (index, item) -> "$index.) $item" }

			logger.info("Added Blocks:\n$blocksString\nAdded Items:\n$itemsString")
		}
	}
}