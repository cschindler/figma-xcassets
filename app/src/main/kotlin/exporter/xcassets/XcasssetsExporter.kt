package exporter.xcassets

import java.nio.file.Path
import kotlin.io.path.*
import kotlinx.serialization.json.Json

import configuration.Appearances
import configuration.AppearanceMode
import configuration.XcassetsSettings
import utils.Asset
import utils.Downloader
import utils.Logger

interface XcassetsExporterInterface {
    suspend fun exportXcassets(
        assets: List<Asset>,
        outputFolderName: String? = null,
        outputName: String,
        outputPath: String,
        settings: XcassetsSettings? = null
    )
}

class XcassetsExporter(private val downloader: Downloader) : XcassetsExporterInterface {
    private data class GroupedAsset(val lightAsset: Asset?, val darkAsset: Asset?)

    override suspend fun exportXcassets(
        assets: List<Asset>,
        outputFolderName: String?,
        outputName: String,
        outputPath: String,
        settings: XcassetsSettings?
    ) {
        var xcassetsDir = createDir( "$outputName.xcassets", Path(outputPath))

        outputFolderName?.let {
            xcassetsDir = createDir(outputFolderName, xcassetsDir)
        }

        Logger.info("Exporting xcassets at $xcassetsDir")

        val groupedAssets = groupByAppearances(assets, settings?.appearances)

        groupedAssets.forEach { (baseName, grouped) ->
            val imagesetDir = createDir("$baseName.imageset", xcassetsDir)

            grouped.lightAsset?.let { downloader.downloadAsset(it, imagesetDir) }
            grouped.darkAsset?.let { downloader.downloadAsset(it, imagesetDir) }

            createContentFile(grouped, imagesetDir)
        }

        createDirContentFile(xcassetsDir)
    }

    private fun createDir(directoryName: String, path: Path): Path {
        return path.resolve(directoryName).createDirectories()
    }

    private fun createDirContentFile(path: Path) {
        val contentsFile = path.resolve("Contents.json")
        val prettyJson = Json {
            prettyPrint = true
            encodeDefaults = true
            explicitNulls = false
        }
        contentsFile.writeText(prettyJson.encodeToString(Contents()))
    }

    private fun createContentFile(grouped: GroupedAsset, path: Path) {
        val images = buildList {
            grouped.lightAsset?.images?.forEach { (scaleUnit, _) ->
                Scale.fromUnit(scaleUnit)?.let { scale ->
                    val filename = grouped.lightAsset.filename(scaleUnit)
                    if (grouped.lightAsset.images.size == 1) {
                        add(Image(filename = filename))
                    } else {
                        add(Image(filename = filename, scale = scale))
                    }
                }
            }

            grouped.darkAsset?.images?.forEach { (scaleUnit, _) ->
                Scale.fromUnit(scaleUnit)?.let { scale ->
                    val filename = grouped.darkAsset.filename(scaleUnit)
                    if (grouped.darkAsset.images.size == 1) {
                        add(Image(filename = filename, appearances = listOf(Appearance("dark"))))
                    } else {
                        add(Image(filename = filename, scale = scale, appearances = listOf(Appearance("dark"))))
                    }
                }
            }
        }

        val contents = Contents(images)
        val contentsFile = path.resolve("Contents.json")
        val prettyJson = Json {
            prettyPrint = true
            encodeDefaults = true
            explicitNulls = false
        }
        contentsFile.writeText(prettyJson.encodeToString(contents))
    }

    private fun groupByAppearances(
        assets: List<Asset>,
        appearances: Appearances?
    ): Map<String, GroupedAsset> {
        return when (appearances?.mode) {
            AppearanceMode.SUFFIX -> groupBySuffix(assets, appearances)
            AppearanceMode.PREFIX -> groupByPrefix(assets, appearances)
            else -> assets.associate { it.name to GroupedAsset(lightAsset = it, darkAsset = null) }
        }
    }

    private fun groupBySuffix(assets: List<Asset>, appearances: Appearances): Map<String, GroupedAsset> {
        val grouped = mutableMapOf<String, GroupedAsset>()

        for (asset in assets) {
            val name = asset.name
            when {
                appearances.lightPattern != null && name.endsWith(appearances.lightPattern) -> {
                    val baseName = name.removeSuffix(appearances.lightPattern)
                    val updated = grouped[baseName]?.copy(lightAsset = asset) ?: GroupedAsset(asset, null)
                    grouped[baseName] = updated
                }
                appearances.darkPattern != null && name.endsWith(appearances.darkPattern) -> {
                    val baseName = name.removeSuffix(appearances.darkPattern)
                    val updated = grouped[baseName]?.copy(darkAsset = asset) ?: GroupedAsset(null, asset)
                    grouped[baseName] = updated
                }
                else -> {
                    grouped[name] = GroupedAsset(asset, null)
                }
            }
        }

        return grouped
    }

    private fun groupByPrefix(assets: List<Asset>, appearances: Appearances): Map<String, GroupedAsset> {
        val grouped = mutableMapOf<String, GroupedAsset>()

        for (asset in assets) {
            val name = asset.name
            when {
                appearances.lightPattern != null && name.startsWith(appearances.lightPattern) -> {
                    val baseName = name.removePrefix(appearances.lightPattern)
                    val updated = grouped[baseName]?.copy(lightAsset = asset) ?: GroupedAsset(asset, null)
                    grouped[baseName] = updated
                }
                appearances.darkPattern != null && name.startsWith(appearances.darkPattern) -> {
                    val baseName = name.removePrefix(appearances.darkPattern)
                    val updated = grouped[baseName]?.copy(darkAsset = asset) ?: GroupedAsset(null, asset)
                    grouped[baseName] = updated
                }
                else -> {
                    grouped[name] = GroupedAsset(asset, null)
                }
            }
        }

        return grouped
    }
}
