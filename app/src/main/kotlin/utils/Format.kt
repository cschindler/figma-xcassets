package utils

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Format {
    @SerialName("png")
    PNG,
    @SerialName("svg")
    SVG;

    val fileExtension: String
        get() = when (this) {
            PNG -> ".png"
            SVG -> ".svg"
        }
}