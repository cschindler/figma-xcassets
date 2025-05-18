package utils

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import java.nio.file.Path
import kotlin.io.path.*

interface DownloaderInterface {
    suspend fun downloadAsset(asset: Asset, path: Path)
}

class Downloader(private val httpClient: HttpClient): DownloaderInterface {
    override suspend fun downloadAsset(asset: Asset, path: Path) {
        for ((scale, imageUrl) in asset.images) {
            try {
                val bytes: ByteArray = httpClient.get(imageUrl).body()
                val filename = asset.filename(scale)
                val file = path.resolve(filename)
                file.writeBytes(bytes)
                Logger.info("Created $filename at $path")
            } catch (e: Exception) {
                Logger.error("${e.message}")
            }
        }
    }
}