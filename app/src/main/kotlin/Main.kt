import kotlinx.coroutines.runBlocking

import utils.Logger
import utils.Program

fun main(args: Array<String>) {
    Program.displayProgName()

    val apiKey = Program.getEnv("FIGMA_API_KEY")
        ?: return Logger.error("Required environment variable 'FIGMA_API_KEY' not found")

    val configuration = runCatching { Program.getConfiguration(args) }
        .onFailure { Logger.error(it.message ?: "Unknown error") }
        .getOrElse { return }

    val app = App(apiKey, configuration)
    runBlocking {
        app.start()
    }

    Logger.info("Exit")
}