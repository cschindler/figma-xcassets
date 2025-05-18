package utils

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readBytes

import kotlin.test.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach

class DownloaderTests {
    private lateinit var tempDir: Path

    @BeforeEach
    fun setUp() {
        tempDir = Files.createTempDirectory("test-tmp")
    }

    @AfterEach
    fun tearDown() {
        deleteRecursively(tempDir)
    }

    private fun deleteRecursively(path: Path) {
        if (Files.exists(path)) {
            Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .forEach { Files.deleteIfExists(it) }
        }
    }

    @Test
    fun testDownloadAsset() = runTest {
        val asset = Asset(
            "img",
            Format.SVG,
            mapOf(
                1 to "https://dummy.com/img.svg",
                2 to "https://dummy.com/img2.svg",
                3 to "https://dummy.com/img3.svg"
            )
        )

        val expectedBytes = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05)

        val mockEngine = MockEngine { request ->
            respond(
                content = expectedBytes,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "image/svg")
            )
        }
        val httpClient = HttpClient(mockEngine)

        val downloader = Downloader(httpClient)
        downloader.downloadAsset(asset, tempDir)

        asset.images.forEach { (scale, _) ->
            val expectedFile = tempDir.resolve(asset.filename(scale))
            assert(expectedFile.exists())
            val actualBytes = expectedFile.readBytes()
            assert(expectedBytes.contentEquals(actualBytes))
        }
    }
}