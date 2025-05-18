package utils

enum class LogLevel(val label: String) {
    DEBUG("DEBUG"),
    INFO("INFO"),
    WARN("WARN"),
    ERROR("ERROR");
}

object Logger {
    var minimumLevel: LogLevel = LogLevel.DEBUG

    fun debug(message: String) = log(LogLevel.DEBUG, message)
    fun info(message: String) = log(LogLevel.INFO, message)
    fun warn(message: String) = log(LogLevel.WARN, message)
    fun error(message: String) = log(LogLevel.ERROR, message)

    private fun log(level: LogLevel, message: String) {
        if (level.ordinal >= minimumLevel.ordinal) {
            val timestamp = java.time.LocalTime.now().toString().substring(0, 8)
            val formatedMsg = "[$timestamp] [${level.label}] $message"
            println(formatedMsg)
        }
    }
}