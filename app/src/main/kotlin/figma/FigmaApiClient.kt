package figma

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

import utils.Logger

data class FileOptions(val depth: Int)

interface FigmaApiClientInterface {
    suspend fun getFile(
        fileKey: String,
        options: FileOptions
    ): FigmaFile

    suspend fun getImages(
        fileKey: String,
        ids: List<String>,
        format: FigmaFormat,
        scale: Int
    ): FigmaImages
}

class FigmaApiClient(
    private var httpClient: HttpClient,
    private var apiKey: String
): FigmaApiClientInterface {
    private val baseURL = "api.figma.com"

    override suspend fun getFile(
        fileKey: String,
        options: FileOptions
    ): FigmaFile {
        val url = URLBuilder()
        url.protocol = URLProtocol.HTTPS
        url.host = baseURL
        url.path("/v1/files/${fileKey}")
        url.parameters.append("depth", options.depth.toString())

        Logger.info("Fetching ${url.buildString()}")

        val file: FigmaFile = httpClient.get {
            headers {
                append("X-FIGMA-TOKEN", apiKey)
            }

            url(url.build())
        }.body()
        return file
    }

    override suspend fun getImages(
        fileKey: String,
        ids: List<String>,
        format: FigmaFormat,
        scale: Int
    ): FigmaImages {
        val url = URLBuilder()
        url.protocol = URLProtocol.HTTPS
        url.host = baseURL
        url.path("/v1/images/${fileKey}")
        url.parameters.append("ids", ids.joinToString(","))
        url.parameters.append("format", format.rawValue)
        url.parameters.append("scale", scale.toString())

        Logger.info("Fetching ${url.buildString()}")

        val images: FigmaImages = httpClient.get {
            headers {
                append("X-FIGMA-TOKEN", apiKey)
            }

            url(url.build())
        }.body()
        return images
    }
}
