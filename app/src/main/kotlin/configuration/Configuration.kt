package configuration

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json
import java.io.File

import utils.Format

@Serializable
data class Configuration(
    val outputName: String = "Assets",
    val outputPath: String = ".",
    val outputFormat: OutputFormat = OutputFormat.XCASSETS,
    val resources: List<Resource>,
    val xcassets: XcassetsSettings? = null
) {
    companion object {
        fun decode(filePath: String): Configuration {
            val file = File(filePath)
            check(file.exists()) { "File not found at $filePath" }
            val content = file.readText()
            val decoder = Json { ignoreUnknownKeys = true }
            return decoder.decodeFromString(serializer(), content)
        }
    }
}

@Serializable
data class Resource(
    val fileKey: String,
    val pageName: String,
    val layerName: String? = null,
    val includePatterns: List<String> = emptyList(),
    val excludePatterns: List<String> = emptyList(),
    val exportOptions: ExportOptions = ExportOptions(Format.SVG),
    val maxDepth: Int = 3,
    val readyToDevOnly: Boolean = false,
    val outputFolderName: String? = null
)

@Serializable
data class ExportOptions(
    val format: Format,
    val scales: List<Int> = listOf(1)
)

@Serializable
enum class OutputFormat {
    @SerialName("raw")
    RAW,
    @SerialName("xcassets")
    XCASSETS
}

@Serializable
data class XcassetsSettings(
    val appearances: Appearances? = null
)

@Serializable
data class Appearances(
    val mode: AppearanceMode = AppearanceMode.SUFFIX,
    val lightPattern: String? = null,
    val darkPattern: String? = null
)

@Serializable
enum class AppearanceMode {
    @SerialName("suffix")
    SUFFIX,
    @SerialName("prefix")
    PREFIX
}