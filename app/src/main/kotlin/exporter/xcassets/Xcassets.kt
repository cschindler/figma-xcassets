package exporter.xcassets

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Contents(
    val images: List<Image> = emptyList(),
    val info: Info = Info()
)

@Serializable
data class Info(
    val version: Int = 1,
    val author: String = "xcode"
)

@Serializable
data class Image(
    val filename: String,
    val idiom: String = "universal",
    val scale: Scale? = null,
    val appearances: List<Appearance>? = null
)

@Serializable
data class Appearance(
    val value: String,
    val appearance: String = "luminosity",
)

@Serializable
enum class Scale {
    @SerialName("1x")
    X1,
    @SerialName("2x")
    X2,
    @SerialName("3x")
    X3;

    companion object {
        fun fromUnit(unit: Int): Scale? {
            return when (unit) {
                1 -> X1
                2 -> X2
                3 -> X3
                else -> null
            }
        }
    }
}