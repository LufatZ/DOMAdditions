package de.additions

import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import de.additions.Additions.MODID
import de.additions.Additions.logger
import de.additions.Additions.logging
import de.additions.config.AdditionsConfig
import de.additions.helper.DownloadHelper
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.packs.PackType
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.*
import java.util.zip.ZipFile
import kotlin.io.path.Path

/**
 * Handles automatic download and installation of translation files for the mod.
 *
 * This class downloads translation files from a GitHub repository and sets them up
 * as a Minecraft resource pack that gets automatically enabled.
 *
 * Note for contributors: The resource pack activation is currently done through a
 * workaround by directly modifying options.txt. If you know how to properly register
 * a resource pack programmatically in Fabric/Minecraft, contributions are welcome!
 */
internal object ModTranslations {

    // Resource pack configuration
    internal const val RESOURCEPACK_NAME = "§b$MODID§r §6Translations§r"
    private const val RESOURCEPACK_DIR = "resourcepacks/$RESOURCEPACK_NAME"
    internal const val DESTINATION = "$RESOURCEPACK_DIR/assets/$MODID/lang"
    private const val ZIP_PATH = "$DESTINATION.zip"
    private val DOWNLOAD_INTERVAL = AdditionsConfig.TranslationCounter

    // Translation source
    private const val TRANSLATION_URL = "https://github.com/LufatZ/dayofmind-translation-download/releases/latest/download/dayofmind-translation.zip"

    /**
     * Initiates the complete translation download and installation process.
     *
     * This method orchestrates the entire workflow:
     * 1. Downloads the translation pack from GitHub
     * 2. Extracts and processes the translations
     * 3. Creates a proper resource pack structure
     * 4. Automatically enables the resource pack
     */
    fun startTranslationDownload() {
        if (!shouldCheckForTranslations()) {
            if (logging) logger.info("Skipping translation check on this launch.")
            return
        }

        logger.info("Starting translation download and installation process")

        val download = DownloadHelper.downloadFile(TRANSLATION_URL, ZIP_PATH)

        try {
            if (download) {
                if (logging) logger.info("Translation pack downloaded successfully")
                processTranslations()
            } else {
                logger.error("Failed to download translation pack from: $TRANSLATION_URL")
            }
        } catch (e: Exception) {
            logger.error("Error during translation process: ${e.message}", e)
        }

        logger.info("Translation process completed")
    }

    /**
     * Checks if a translation download should be attempted on this game launch.
     *
     * This function maintains a launch counter in the config directory to limit
     * the download checks to a specific interval (e.g., every 3rd launch).
     *
     * A check is ALWAYS triggered if:
     * 1. The resource pack directory does not exist.
     * 2. The launch count matches the DOWNLOAD_INTERVAL.
     *
     * @return `true` if the download and installation process should run, `false` otherwise.
     */
    private fun shouldCheckForTranslations(): Boolean {
        val configDir = FabricLoader.getInstance().configDir
        val counterFile = configDir.resolve("$MODID-translation-counter.properties").toFile()
        val properties = Properties()

        // Load existing properties or create new ones
        if (counterFile.exists()) {
            try {
                counterFile.inputStream().use { properties.load(it) }
            } catch (e: Exception) {
                logger.warn("Could not read translation properties, resetting: ${e.message}")
            }
        }

        // Read, increment, and save the launch count
        val currentCount = properties.getProperty("launchCount", "0").toIntOrNull() ?: 0
        val newCount = currentCount + 1
        properties.setProperty("launchCount", newCount.toString())

        try {
            counterFile.outputStream().use { properties.store(it, "Mod translation launch counter") }
        } catch (e: Exception) {
            logger.error("Could not save translation properties: ${e.message}", e)
            // Continue anyway, as the check logic can proceed
        }

        // Condition 1: Force check if resource pack is missing
        val resourcePackDir = Path(RESOURCEPACK_DIR)
        if (!Files.exists(resourcePackDir)) {
            logger.info("Resource pack directory not found, forcing translation check.")
            return true
        }

        // Condition 2: Check based on the interval
        val isCheckDay = (newCount % DOWNLOAD_INTERVAL == 1)
        return isCheckDay
    }

    /**
     * Processes the downloaded translation pack by extracting files and setting up the resource pack.
     *
     * This method:
     * 1. Extracts all translation files from the ZIP archive
     * 2. Creates the proper resource pack structure with pack.mcmeta
     * 3. Enables the resource pack in the game
     */
    private fun processTranslations() {
        extractTranslationFiles()
        createResourcePackMetadata()
        enableResourcePack()
    }

    /**
     * Extracts all translation files from the downloaded ZIP archive.
     *
     * Files are extracted to the resource pack's lang directory, with filenames
     * converted to lowercase to match Minecraft's expectations.
     */
    private fun extractTranslationFiles() {
        if (logging) logger.info("Extracting translation files to: $DESTINATION")

        try {
            ZipFile(ZIP_PATH).use { zip ->
                zip.entries().asSequence()
                    .filter { !it.isDirectory }
                    .forEach { entry ->
                        val outputFile = File(DESTINATION, entry.name.lowercase())

                        // Ensure parent directories exist
                        outputFile.parentFile?.mkdirs()

                        // Extract the file
                        zip.getInputStream(entry).use { input ->
                            outputFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }

                        if (logging) logger.info("Extracted: ${entry.name} -> ${outputFile.path}")
                    }
            }

            // Clean up the ZIP file
            if (!File(ZIP_PATH).delete() && logging) {
                logger.warn("Could not delete temporary ZIP file: $ZIP_PATH")
            }

            if (logging) logger.info("Translation files extracted successfully")

        } catch (e: Exception) {
            logger.error("Failed to extract translation files: ${e.message}", e)
            throw e
        }
    }

    /**
     * Creates the pack.mcmeta file and pack icon for the resource pack.
     *
     * The metadata includes the current pack format version and a descriptive name.
     * Also copies the mod's icon as the resource pack icon if available.
     */
    private fun createResourcePackMetadata() {
        val packMcMetaPath = Path(RESOURCEPACK_DIR).resolve("pack.mcmeta")
        val currentPackFormat = net.minecraft.SharedConstants.getCurrentVersion()
            .packVersion(PackType.CLIENT_RESOURCES)

        val packMetadata = """
            {
                "pack": {
                    "pack_format": $currentPackFormat,
                    "min_format": $currentPackFormat,
                    "max_format": $currentPackFormat,
                    "description": "§7Community translations from crowdin for $MODID"
                }
            }
        """.trimIndent()

        try {
            Files.writeString(packMcMetaPath, packMetadata)
            if (logging) {
                logger.info("Created pack.mcmeta at: $packMcMetaPath with pack_format: $currentPackFormat")
            }
        } catch (e: Exception) {
            logger.error("Failed to create pack.mcmeta: ${e.message}", e)
            throw e
        }

        // Copy mod icon as pack icon
        copyModIconAsPackIcon()
    }

    /**
     * Copies the mod's icon to be used as the resource pack icon.
     *
     * Uses the known mod icon location and copies it to pack.png
     * in the resource pack directory. Always updates the icon to ensure
     * it matches the current mod version.
     */
    private fun copyModIconAsPackIcon() {
        val packIconPath = Path(RESOURCEPACK_DIR).resolve("pack.png")
        val iconPath = "assets/$MODID/icon.png"

        try {
            // Try to find the mod icon in jar resources
            val mod = FabricLoader.getInstance().getModContainer(MODID).get()
            val iconResource = mod.rootPaths.firstNotNullOfOrNull { rootPath ->
                val iconFile = rootPath.resolve(iconPath)
                if (Files.exists(iconFile)) iconFile else null
            }

            if (iconResource != null) {
                Files.copy(iconResource, packIconPath, StandardCopyOption.REPLACE_EXISTING)
                if (logging) logger.info("Updated pack icon from mod")
            } else {
                if (logging) logger.info("Mod icon not found at $iconPath, using default pack icon")
            }

        } catch (e: Exception) {
            if (logging) logger.warn("Could not copy mod icon: ${e.message}")
        }
    }

    /**
     * Enables the resource pack by adding it to the 'resourcePacks' list in options.txt.
     *
     * This is a workaround solution since i see no known way to programmatically
     * register resource packs properly in Fabric. The method safely parses and modifies
     * the JSON array in the options file.
     *
     * TODO: Replace with proper resource pack registration if method is discovered
     */
    private fun enableResourcePack() {
        val optionsFile = FabricLoader.getInstance().gameDir.resolve("options.txt").toFile()
        val manualEnablePackLog = "Please manually enable the '$RESOURCEPACK_NAME' resource pack in-game"

        if (!optionsFile.exists()) {
            logger.warn("options.txt not found - resource pack cannot be enabled automatically")
            logger.info(manualEnablePackLog)
            return
        }

        try {
            val lines = optionsFile.readLines().toMutableList()
            val packIdentifier = "file/$RESOURCEPACK_NAME"
            var resourcePackLineFound = false
            var wasModified = false

            // Find and modify the resourcePacks line
            for (i in lines.indices) {
                if (lines[i].startsWith("resourcePacks:")) {
                    resourcePackLineFound = true
                    val currentPacksJson = lines[i].substringAfter("resourcePacks:")

                    // Check if our pack is already enabled
                    if (currentPacksJson.contains("\"$packIdentifier\"")) {
                        logger.info("Translation resource pack is already enabled")
                        return
                    }

                    // Parse and modify the resource pack list using Gson for safety
                    val gson = Gson()
                    val listType = object : TypeToken<MutableList<String>>() {}.type
                    val currentPacks: MutableList<String> = gson.fromJson(currentPacksJson, listType)

                    // Add our pack if not already present
                    if (!currentPacks.contains(packIdentifier)) {
                        currentPacks.add(packIdentifier)
                        lines[i] = "resourcePacks:${gson.toJson(currentPacks)}"
                        wasModified = true

                        if (logging) logger.info("Added translation pack to resource pack list")
                    }
                    break
                }
            }

            if (!resourcePackLineFound) {
                logger.warn("'resourcePacks' entry not found in options.txt - cannot enable automatically")
                logger.info(manualEnablePackLog)
                return
            }

            if (wasModified) {
                optionsFile.writeText(lines.joinToString(System.lineSeparator()))
                logger.info("Successfully enabled translation resource pack")
            }

        } catch (e: Exception) {
            logger.error("Failed to enable resource pack in options.txt: ${e.message}", e)
            logger.info(manualEnablePackLog)
        }
    }
}