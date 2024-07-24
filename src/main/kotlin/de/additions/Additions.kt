package de.additions

import de.additions.blocks.AdditionsBlocks
import de.additions.config.AdditionsConfig
import de.additions.itemGroups.ItemGroups
import eu.midnightdust.lib.config.MidnightConfig
import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

object Additions : ModInitializer {
    private val logger = LoggerFactory.getLogger("additions")
	const val MODID = "additions"

	override fun onInitialize() {
		logger.info("Enjoy DayOfMind")
		MidnightConfig.init(MODID, AdditionsConfig::class.java)				//config screen registration with midnightlib
		AdditionsBlocks.registerBlocks()
		ItemGroups.registerItemGroups()
	}
}