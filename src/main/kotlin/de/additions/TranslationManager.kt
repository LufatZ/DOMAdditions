package de.additions

import de.additions.Additions.MODID
import de.additions.Additions.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import java.io.File
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

/**
 * Manages the downloading, processing, and registration of runtime translations for the mod.
 * Handles the download of translation files, extracts them, and organizes them by language mappings.
 */
object TranslationManager {

    /**
     * Represents a mapping between Minecraft language codes and Crowdin language codes.
     */
    private data class LanguageMapping(
        val minecraftCode: String,
        val crowdinCodes: List<String>
    )

    /**
     * Maps Minecraft language codes to their corresponding Crowdin language codes.
     * The first entry in the Crowdin codes list is considered the primary mapping,
     * with subsequent entries serving as fallbacks.
     */
    private val languageMappings = listOf(
        LanguageMapping("af_za", listOf("af")),
        LanguageMapping("ar_sa", listOf("ar")),
        LanguageMapping("ast_es", listOf("ast")),
        LanguageMapping("az_az", listOf("az")),
        LanguageMapping("ba_ru", listOf("ba")),
        LanguageMapping("be_by", listOf("be")),
        LanguageMapping("bg_bg", listOf("bg")),
        LanguageMapping("br_fr", listOf("br-FR")),
        LanguageMapping("bs_ba", listOf("bs")),
        LanguageMapping("ca_es", listOf("ca")),
        LanguageMapping("cs_cz", listOf("cs")),
        LanguageMapping("cy_gb", listOf("cy")),
        LanguageMapping("da_dk", listOf("da")),
        LanguageMapping("de_at", listOf("de-AT", "de")),
        LanguageMapping("de_ch", listOf("de-CH", "de")),
        LanguageMapping("de_de", listOf("de")),
        LanguageMapping("el_gr", listOf("el")),
        LanguageMapping("en_au", listOf("en-AU", "en-GB", "en-US")),
        LanguageMapping("en_ca", listOf("en-CA", "en-GB", "en-US")),
        LanguageMapping("en_gb", listOf("en-GB", "en-US")),
        LanguageMapping("en_nz", listOf("en-NZ", "en-GB", "en-US")),
        LanguageMapping("en_pt", listOf("en-PT", "en-GB", "en-US")),
        LanguageMapping("en_ud", listOf("en-UD", "en-GB", "en-US")),
        LanguageMapping("en_us", listOf("en-US")),
        LanguageMapping("en_ws", listOf("en-WS")),
        LanguageMapping("en_7s", listOf("en-PT")),
        LanguageMapping("eo_uy", listOf("eo")),
        LanguageMapping("es_ar", listOf("es-AR", "es-ES")),
        LanguageMapping("es_cl", listOf("es-CL", "es-ES")),
        LanguageMapping("es_ec", listOf("es-EC", "es-ES")),
        LanguageMapping("es_es", listOf("es-ES")),
        LanguageMapping("es_mx", listOf("es-MX", "es-ES")),
        LanguageMapping("es_uy", listOf("es-UY", "es-ES")),
        LanguageMapping("es_ve", listOf("es-VE", "es-ES")),
        LanguageMapping("esan", listOf("esan")),
        LanguageMapping("et_ee", listOf("et")),
        LanguageMapping("eu_es", listOf("eu")),
        LanguageMapping("fa_ir", listOf("fa")),
        LanguageMapping("fi_fi", listOf("fi")),
        LanguageMapping("fil_ph", listOf("fil")),
        LanguageMapping("fo_fo", listOf("fo")),
        LanguageMapping("fr_ca", listOf("fr-CA", "fr")),
        LanguageMapping("fr_fr", listOf("fr")),
        LanguageMapping("fra_de", listOf("fra-DE")),
        LanguageMapping("fy_nl", listOf("fy-NL")),
        LanguageMapping("ga_ie", listOf("ga-IE")),
        LanguageMapping("gd_gb", listOf("gd")),
        LanguageMapping("gl_es", listOf("gl")),
        LanguageMapping("haw_us", listOf("haw")),
        LanguageMapping("he_il", listOf("he")),
        LanguageMapping("hi_in", listOf("hi")),
        LanguageMapping("hr_hr", listOf("hr")),
        LanguageMapping("hu_hu", listOf("hu")),
        LanguageMapping("hy_am", listOf("hy-AM")),
        LanguageMapping("id_id", listOf("id")),
        LanguageMapping("ig_ng", listOf("ig")),
        LanguageMapping("io_en", listOf("ido")),
        LanguageMapping("is_is", listOf("is")),
        LanguageMapping("it_it", listOf("it")),
        LanguageMapping("ja_jp", listOf("ja")),
        LanguageMapping("jbo_en", listOf("jbo")),
        LanguageMapping("ka_ge", listOf("ka")),
        LanguageMapping("kk_kz", listOf("kk")),
        LanguageMapping("kn_in", listOf("kn")),
        LanguageMapping("ko_kr", listOf("ko")),
        LanguageMapping("kw_gb", listOf("kw")),
        LanguageMapping("la_la", listOf("la-LA")),
        LanguageMapping("lb_lu", listOf("lb")),
        LanguageMapping("li_li", listOf("li")),
        LanguageMapping("lol_us", listOf("lol")),
        LanguageMapping("lt_lt", listOf("lt")),
        LanguageMapping("lv_lv", listOf("lv")),
        LanguageMapping("mi_NZ", listOf("mi")),
        LanguageMapping("mk_mk", listOf("mk")),
        LanguageMapping("mn_mn", listOf("mn")),
        LanguageMapping("ms_my", listOf("ms")),
        LanguageMapping("mt_mt", listOf("mt")),
        LanguageMapping("nds_de", listOf("nds")),
        LanguageMapping("nl_be", listOf("nl-BE", "nl")),
        LanguageMapping("nl_nl", listOf("nl")),
        LanguageMapping("nn_no", listOf("nn-NO", "no")),
        LanguageMapping("no_no", listOf("no", "nb")),
        LanguageMapping("oc_fr", listOf("oc")),
        LanguageMapping("pl_pl", listOf("pl")),
        LanguageMapping("pt_br", listOf("pt-BR", "pt-PT")),
        LanguageMapping("pt_pt", listOf("pt-PT", "pt-BR")),
        LanguageMapping("qya_aa", listOf("qya-AA")),
        LanguageMapping("ro_ro", listOf("ro")),
        LanguageMapping("ru_ru", listOf("ru")),
        LanguageMapping("se_no", listOf("se")),
        LanguageMapping("sk_sk", listOf("sk")),
        LanguageMapping("sl_si", listOf("sl")),
        LanguageMapping("so_so", listOf("so")),
        LanguageMapping("sq_al", listOf("sq")),
        LanguageMapping("sr_sp", listOf("sr")),
        LanguageMapping("sv_se", listOf("sv-SE")),
        LanguageMapping("ta_in", listOf("ta")),
        LanguageMapping("th_th", listOf("th")),
        LanguageMapping("tl_ph", listOf("tl")),
        LanguageMapping("tlh_aa", listOf("tlh-AA")),
        LanguageMapping("tr_tr", listOf("tr")),
        LanguageMapping("tt_ru", listOf("tt-RU")),
        LanguageMapping("uk_ua", listOf("uk")),
        LanguageMapping("val_es", listOf("val-ES")),
        LanguageMapping("vec_it", listOf("vec")),
        LanguageMapping("vi_vn", listOf("vi")),
        LanguageMapping("yi_de", listOf("yi")),
        LanguageMapping("yo_ng", listOf("yo")),
        LanguageMapping("zh_cn", listOf("zh-CN", "zh-HK")),
        LanguageMapping("zh_hk", listOf("zh-HK", "zh-CN")),
        LanguageMapping("zh_tw", listOf("zh-TW"))
    )

    /**
     * Downloads and processes translation files from a specified URL.
     *
     * @param url The URL to download the translation files from.
     * @param logging Whether to log detailed messages during the process.
     */
    suspend fun downloadTranslations(url: String, logging: Boolean = false) {
        val tempDir = createTempDirectory(logging)

        logInfo("Starting translation process for $MODID with download translations from $url", logging)
        try {
            val zipFile = downloadZipFile(url, tempDir, logging)
            if (zipFile != null) {
                processTranslations(zipFile, logging)
            } else {
                logError("Skipping translation processing due to download failure", logging)
            }
        } finally {
            cleanup(tempDir, logging)
        }
    }

    private fun createTempDirectory(logging: Boolean): Path {
        return Files.createTempDirectory("mod-translations").also {
            logInfo("Created temp directory: $it", logging)
        }
    }

    private fun downloadZipFile(url: String, tempDir: Path, logging: Boolean): Path? {
        val zipPath = tempDir.resolve("$MODID-latest.zip")

        return try {
            URI(url).toURL().openStream().use { inputStream ->
                Files.copy(inputStream, zipPath, StandardCopyOption.REPLACE_EXISTING)
            }
            logInfo("Downloaded translation file to $zipPath", logging)
            zipPath
        } catch (e: Exception) {
            logError("Failed to download translation file: ${e.message}", logging)
            null
        }
    }

    private suspend fun processTranslations(zipPath: Path, logging: Boolean) {
        val outputDir = createOutputDirectory(logging)

        withContext(Dispatchers.IO) {
            ZipFile(zipPath.toFile()).use { zip ->
                zip.entries().asSequence()
                    .filter { it.isValidTranslationEntry() }
                    .forEach { entry ->
                        processTranslationEntry(entry, zip, outputDir, logging)
                    }
            }
        }

        // Register the resource pack after processing the translations.
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (client.options != null && !client.options.resourcePacks.contains("file/$MODID")) {
                registerTranslationResourcePack(logging)
            }
        }
    }

    private fun createOutputDirectory(logging: Boolean): Path {
        val baseDir = Path.of("resourcepacks", MODID)
        val langDir = baseDir.resolve("assets").resolve(MODID).resolve("lang")
        Files.createDirectories(langDir)
        logDirectoryStructure(baseDir, logging)

        val packMcmeta = baseDir.resolve("pack.mcmeta")
        val mcmetaContent = """
            {
                "pack": {
                    "pack_format": 46,
                    "description": "$MODID Translations"
                }
            }
        """.trimIndent()
        Files.writeString(packMcmeta, mcmetaContent)
        logInfo("Created pack.mcmeta file", logging)
        logInfo("Created output directory structure at: $langDir", logging)
        return langDir
    }

    private fun ZipEntry.isValidTranslationEntry(): Boolean =
        !isDirectory && name.endsWith("en_us.json")

    private suspend fun processTranslationEntry(
        entry: ZipEntry,
        zip: ZipFile,
        outputDir: Path,
        logging: Boolean
    ) = withContext(Dispatchers.IO) {
        val folderName = entry.name.split("/").firstOrNull() ?: return@withContext

        languageMappings
            .filter { mapping -> mapping.crowdinCodes.any { it == folderName } }
            .forEach { mapping ->
                val targetPath = outputDir.resolve("${mapping.minecraftCode}.json")

                // Extract to temporary file first
                zip.getInputStream(entry).use { input ->
                    Files.copy(input, targetPath, StandardCopyOption.REPLACE_EXISTING)
                }
                logInfo("$mapping successfully moved to $targetPath", logging)
            }
    }

    private fun cleanup(tempDir: Path, logging: Boolean) {
        runCatching {
            Files.walk(tempDir)
                .sorted(Comparator.reverseOrder())
                .forEach { Files.delete(it) }
            logInfo("Cleaned up temporary files", logging)
        }.onFailure {
            logger.error("Failed to clean up temporary files", it)
        }
    }

    private fun logInfo(message: String, logging: Boolean) {
        if (logging) logger.info(message)
    }

    private fun logError(message: String, logging: Boolean) {
        if (logging) logger.error(message)
    }

    private fun logDirectoryStructure(path: Path, logging: Boolean) {
        if (!logging) return

        Files.walk(path).use { paths ->
            paths.forEach { p ->
                logInfo("Found path: $p", true)
            }
        }
    }

    /**
     * Registers the downloaded translations as a built-in resource pack.
     */
    fun registerTranslationResourcePack(logging: Boolean) {
        try {
            val client = MinecraftClient.getInstance()
            val options = client.options

            val activeResourcePacks = options.resourcePacks
            logInfo("Active ResourcePacks: $activeResourcePacks", logging)

            if (!activeResourcePacks.contains("file/$MODID")) {
                activeResourcePacks.add("file/$MODID")
                options.resourcePacks = activeResourcePacks

                options.write()
                logInfo("Translation pack successfully registered: $MODID", logging)
            } else {
                logInfo("Translation pack already registered: $MODID", logging)
            }
        } catch (e: Exception) {
            logError("Error registering translation pack: ${e.message}", logging)
            e.printStackTrace()
        }
    }

    fun TranslationManager.clearTranslations(logging: Boolean) {
        try {
            File(Path.of(MinecraftClient.getInstance().runDirectory.path, "resourcepacks", MODID).toString()).deleteRecursively()
            logInfo("Deleted translation pack: $MODID", logging)
        }catch (e: Exception) {
            logError("Error deleting translation pack: ${e.message}", logging)
        }
    }
}
