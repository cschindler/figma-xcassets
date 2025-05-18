package exporter.raw

import java.nio.file.Path
import kotlin.io.path.*

import utils.Asset
import utils.DownloaderInterface
import utils.Logger

interface RawExporterInterface {
    suspend fun export(
        assets: List<Asset>,
        outputName: String,
        outputPath: String,
        outputFolderName: String? = null,
    )
}

class RawExporter(private val downloader: DownloaderInterface) : RawExporterInterface {
    override suspend fun export(
        assets: List<Asset>,
        outputName: String,
        outputPath: String,
        outputFolderName: String?,
    ) {
        var assetsDir = createDir(outputName, Path(outputPath))

        outputFolderName?.let {
            assetsDir = createDir(outputFolderName, assetsDir)
        }

        Logger.info("Exporting assets at $assetsDir")

        assets.forEach { asset ->
            downloader.downloadAsset(asset, assetsDir)
        }
    }

    private fun createDir(directoryName: String, path: Path): Path {
        return path.resolve(directoryName).createDirectories()
    }
}