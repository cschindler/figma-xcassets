package figma

import utils.Asset
import utils.Format
import configuration.ExportOptions
import configuration.Resource

class PageNotFoundException(name: String): RuntimeException("Page \"$name\" not found")
class LayerNotFoundException(name: String): RuntimeException("Layer \"$name\" not found")

interface FigmaFetcherInterface {
    suspend fun fetchAssets(resource: Resource): List<Asset>
}

class FigmaFetcher(private val apiClient: FigmaApiClientInterface): FigmaFetcherInterface {
    override suspend fun fetchAssets(resource: Resource): List<Asset> {
        val file = apiClient.getFile(resource.fileKey, FileOptions(resource.maxDepth))

        var parentNode = getChildNode(resource.pageName, FigmaType.CANVAS, file.document)
            ?: throw PageNotFoundException(resource.pageName)

        resource.layerName?.let {
            parentNode = getChildNode(resource.layerName, null, parentNode)
                ?: throw LayerNotFoundException(resource.layerName)
        }

        val nodes = filterNodes(
            parentNode,
            resource.readyToDevOnly,
            resource.includePatterns,
            resource.excludePatterns
        )
        .takeIf { it.isNotEmpty() } ?: return emptyList()

        return fetchAssets(resource.fileKey, nodes, resource.exportOptions)
    }

    private fun getChildNode(name: String, type: FigmaType?, node: FigmaNode): FigmaNode? {
        return node.children.firstOrNull { child ->
            child.name == name && (type == null || child.type == type.type)
        }
    }

    private fun filterNodes(
        node: FigmaNode,
        readyToDevOnly: Boolean,
        includePatterns: List<String>,
        excludePatterns: List<String>
    ): List<FigmaNode> {
        val isMatch = matchesPatterns(node.name, includePatterns, excludePatterns)
        val isReady = !readyToDevOnly || node.devStatus?.type == FigmaDevStatusType.READY_FOR_DEV
        val shouldInclude = isMatch && isReady

        return if (shouldInclude) {
            listOf(node)
        } else {
            node.children.flatMap { child ->
                filterNodes(child, readyToDevOnly, includePatterns, excludePatterns)
            }
        }
    }

    private fun matchesPatterns(
        name: String,
        includePatterns: List<String>,
        excludePatterns: List<String>
    ): Boolean {
        val matchesInclude = includePatterns.any { Regex(it).containsMatchIn(name) }
        val matchesExclude = excludePatterns.any { Regex(it).containsMatchIn(name) }
        return matchesInclude && !matchesExclude
    }

    suspend fun fetchAssets(
        fileKey: String,
        nodes: List<FigmaNode>,
        exportOptions: ExportOptions
    ): List<Asset> {
        return when (exportOptions.format.toFigmaFormat()) {
            FigmaFormat.SVG -> fetchSvgAssets(fileKey, nodes)
            FigmaFormat.PNG -> fetchPngAssets(fileKey, nodes, exportOptions.scales)
        }
    }

    private suspend fun fetchSvgAssets(
        fileKey: String,
        nodes: List<FigmaNode>
    ): List<Asset> {
        val ids = nodes.map { it.id }
        val images = apiClient.getImages(fileKey, ids, Format.SVG.toFigmaFormat(), 1)

        return nodes.mapNotNull { node ->
            images.images[node.id]?.let { url ->
                Asset(node.name, Format.SVG, mapOf(1 to url))
            }
        }
    }

    private suspend fun fetchPngAssets(
        fileKey: String,
        nodes: List<FigmaNode>,
        scales: List<Int>
    ): List<Asset> {
        val assets = mutableListOf<Asset>()

        for (scale in scales) {
            val ids = nodes.map { it.id }
            val images = apiClient.getImages(fileKey, ids, Format.PNG.toFigmaFormat(), scale)

            nodes.forEach { node ->
                val imageUrl = images.images[node.id]
                if (imageUrl != null) {
                    val existingIndex = assets.indexOfFirst { it.name == node.name && it.format == Format.PNG }
                    if (existingIndex == -1) {
                        assets += Asset(node.name, Format.PNG, mapOf(scale to imageUrl))
                    } else {
                        val existing = assets[existingIndex]
                        val mergedImages = existing.images + (scale to imageUrl)
                        assets[existingIndex] = existing.copy(images = mergedImages)
                    }
                }
            }
        }

        return assets
    }

    private fun Format.toFigmaFormat(): FigmaFormat = when (this) {
        Format.PNG -> FigmaFormat.PNG
        Format.SVG -> FigmaFormat.SVG
    }
}
