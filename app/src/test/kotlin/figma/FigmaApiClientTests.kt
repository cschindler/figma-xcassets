package figma

import kotlin.test.Test
import kotlin.test.assertEquals

import io.ktor.http.*
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.client.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json
import kotlinx.coroutines.test.runTest

class FigmaApiClientTests {
    private val json = Json { ignoreUnknownKeys = true }
    private val dummyApiKey = "dummy_api_key"
    private val dummyFileKey = "dummy_file_key"
    private val dummyResponse = """
        {
            "name": "Figma",
            "document": {
                "id": "0",
                "name": "Document",
                "type": "DOCUMENT"
            }
        }
        """.trimIndent()
    private val dummyImages = """
        {
            "images": {
                "0:1": "https://image.svg",
                "0:2": "https://image.svg",
                "0:3": "https://image.svg"
            }
        }
        """.trimIndent()

    @Test
    fun testApiGetFileRequest() = runTest {
        val expectedFileOptions = FileOptions(depth = 1)

        val mockEngine = MockEngine { request ->
            assertEquals(dummyApiKey, request.headers["X-FIGMA-TOKEN"])
            assertEquals(URLProtocol.HTTPS, request.url.protocol)
            assertEquals("api.figma.com", request.url.host)
            assertEquals("/v1/files/${dummyFileKey}", request.url.encodedPath)
            assertEquals(expectedFileOptions.depth.toString(), request.url.parameters["depth"])

            respond(
                content = dummyResponse,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
        }

        val apiClient = FigmaApiClient(httpClient, dummyApiKey)
        apiClient.getFile(dummyFileKey, expectedFileOptions)
    }

    @Test
    fun testApiGetFileResponse() = runTest {
        val expectedFile = FigmaFile(
            "Figma",
            document = FigmaNode("0", "Document", "DOCUMENT")
        )

        val mockEngine = MockEngine { request ->
            respond(
                content = dummyResponse,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }

        val apiClient = FigmaApiClient(httpClient, dummyApiKey)
        val actualFile = apiClient.getFile(dummyFileKey, FileOptions(depth = 1))

        assertEquals(expectedFile, actualFile)
    }

    @Test
    fun testApiGetImageRequest() = runTest {
        val expectedIds = listOf("0:1", "0:2", "0:3")
        val expectedFormat = FigmaFormat.SVG
        val expectedScale = 1

        val mockEngine = MockEngine { request ->
            assertEquals(dummyApiKey, request.headers["X-FIGMA-TOKEN"])
            assertEquals(URLProtocol.HTTPS, request.url.protocol)
            assertEquals("api.figma.com", request.url.host)
            assertEquals("/v1/images/${dummyFileKey}", request.url.encodedPath)
            assertEquals(expectedIds.joinToString(","), request.url.parameters["ids"])
            assertEquals(expectedFormat.toString().lowercase(), request.url.parameters["format"])
            assertEquals(expectedScale.toString(), request.url.parameters["scale"])

            respond(
                content = dummyImages,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
        }

        val apiClient = FigmaApiClient(httpClient, dummyApiKey)
        apiClient.getImages(
            dummyFileKey,
            expectedIds,
            expectedFormat,
            expectedScale
        )
    }

    @Test
    fun testApiGetImageResponse() = runTest {
        val expectedIds = listOf("0:1", "0:2", "0:3")
        val expectedFormat = FigmaFormat.SVG
        val expectedScale = 1
        val expectedImages = FigmaImages(
            mapOf(
                "0:1" to "https://image.svg",
                "0:2" to "https://image.svg",
                "0:3" to "https://image.svg"
            )
        )

        val mockEngine = MockEngine { request ->
            respond(
                content = dummyImages,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
        }

        val apiClient = FigmaApiClient(httpClient, dummyApiKey)
        val actualImages = apiClient.getImages(
            dummyFileKey,
            expectedIds,
            expectedFormat,
            expectedScale
        )

        assertEquals(expectedImages, actualImages)
    }
}