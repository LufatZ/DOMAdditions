package de.additions.helper

import de.additions.Additions.logger
import de.additions.Additions.logging
import java.io.FileOutputStream
import java.net.URI
import java.nio.channels.Channels
import java.nio.file.Files
import java.nio.file.Paths

internal object DownloadHelper {

    /**
     * Downloads a file from the given URL and saves it to the specified destination path.
     *
     * Creates the destination directory if it doesn't exist. Uses NIO channels for
     * efficient file transfer with proper resource management.
     *
     * @param urlString the URL to download from
     * @param destinationPath the local file path where the download should be saved
     */
    internal fun downloadFile(urlString: String, destinationPath: String) : Boolean {
        logger.info("Starting download from: $urlString")

        return try {
            // Ensure destination directory exists
            val destinationFile = Paths.get(destinationPath)
            val parentDirectory = destinationFile.parent

            if (parentDirectory != null && !Files.exists(parentDirectory)) {
                if (logging) logger.info("Creating directory: $parentDirectory")
                Files.createDirectories(parentDirectory)
            }

            // Open URL and create download stream
            val url = URI(urlString).toURL()

            // Use NIO channels for efficient transfer
            url.openStream().use { inputStream ->
                Channels.newChannel(inputStream).use { readableByteChannel ->
                    FileOutputStream(destinationPath).use { fileOutputStream ->
                        // Transfer file in one go
                        fileOutputStream.channel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE)
                    }
                }
            }

            // Report success
            logger.info("Download completed successfully!")
            if (logging) logger.info("✓ Download saved to: ${destinationFile.toAbsolutePath()}")

            true

        } catch (e: Exception) {
            // Log error and show user feedback
            if (logging)logger.error("Error downloading file from: $urlString", e)
            logger.warn("✗ Download failed: ${e.message}")

            false
        }
    }
}