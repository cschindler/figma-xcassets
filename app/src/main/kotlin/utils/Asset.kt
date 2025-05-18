package utils

data class Asset(
    val name: String,
    val format: Format,
    val images: Map<Int, String>
) {
    fun filename(scale: Int): String {
        val scaleSuffix = if (scale == 1) "" else "@${scale}x"
        return "$name$scaleSuffix${format.fileExtension}"
    }
}