package figma

import kotlinx.serialization.Serializable

@Serializable
data class FigmaFile(
    val name: String,
    val document: FigmaNode
)

@Serializable
data class FigmaNode(
    val id: String,
    val name: String,
    val type: String,
    val children: List<FigmaNode> = emptyList(),
    val devStatus: FigmaDevStatus? = null
)

@Serializable
data class FigmaDevStatus(
    val type: FigmaDevStatusType,
)

@Serializable
enum class FigmaDevStatusType {
    READY_FOR_DEV
}

@Serializable
data class FigmaImages(
    val images: Map<String, String>
)

enum class FigmaType(val type: String) {
    CANVAS("CANVAS")
}

enum class FigmaFormat(val rawValue: String) {
    PNG("png"),
    SVG("svg")
}