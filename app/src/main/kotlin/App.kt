import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.Logger as KtorLogger
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

import configuration.Configuration
import configuration.OutputFormat
import configuration.Resource
import configuration.XcassetsSettings
import exporter.xcassets.XcassetsExporter
import exporter.raw.RawExporter
import figma.FigmaApiClient
import figma.FigmaFetcher
import utils.Logger
import utils.Asset
import utils.Downloader

class App(
    val apiKey: String,
    val configuration: Configuration
) {
    val httpClient = HttpClient(CIO) {
        install(Logging) {
            logger = KtorLogger.SIMPLE
            level = LogLevel.NONE // ALL, HEADERS, BODY, INFO, ou NONE
        }

        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    val apiClient = FigmaApiClient(httpClient, apiKey)
    val fetcher = FigmaFetcher(apiClient)

    val downloader = Downloader(httpClient)
    val rawExporter = RawExporter(downloader)
    val xcassetsExporter = XcassetsExporter(downloader)

    suspend fun start() {
        for (resource in configuration.resources) {
            try {
                Logger.info("Looking for assets with fileKey ${resource.fileKey}")

                val fetchedAssets = fetcher.fetchAssets(resource)
                Logger.info("Found ${fetchedAssets.size} asset(s)")

                if (fetchedAssets.isEmpty()) continue

                export(fetchedAssets, resource, configuration)
            } catch (e: Exception) {
                Logger.error("ERROR: ${e.message}")
            }
        }

        httpClient.close()
    }

    private suspend fun export(
        assets: List<Asset>,
        resource: Resource,
        configuration: Configuration
    ) {
        when (configuration.outputFormat) {
            OutputFormat.RAW -> exportRaw(
                assets,
                configuration.outputName,
                configuration.outputPath,
                resource.outputFolderName
            )
            OutputFormat.XCASSETS -> exportXcassets(
                assets,
                configuration.outputName,
                configuration.outputPath,
                resource.outputFolderName,
                configuration.xcassets
            )
        }
    }

    private suspend fun exportRaw(
        assets: List<Asset>,
        outputName: String,
        outputPath: String,
        outputFolderName: String?
    ) {
        rawExporter.export(
            assets,
            outputName,
            outputPath,
            outputFolderName
        )
    }

    private suspend fun exportXcassets(
        assets: List<Asset>,
        outputName: String,
        outputPath: String,
        outputFolderName: String?,
        settings: XcassetsSettings?
    ) {
        xcassetsExporter.exportXcassets(
            assets,
            outputFolderName,
            outputName,
            outputPath,
            settings
        )
    }
}