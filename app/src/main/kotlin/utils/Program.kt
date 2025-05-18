package utils

import configuration.Configuration

object Program {
    const val PROGNAME = """
███████╗██╗ ██████╗ ███╗   ███╗ █████╗      ██╗  ██╗ ██████╗ █████╗ ███████╗███████╗███████╗████████╗███████╗
██╔════╝██║██╔════╝ ████╗ ████║██╔══██╗     ╚██╗██╔╝██╔════╝██╔══██╗██╔════╝██╔════╝██╔════╝╚══██╔══╝██╔════╝
█████╗  ██║██║  ███╗██╔████╔██║███████║█████╗╚███╔╝ ██║     ███████║███████╗███████╗█████╗     ██║   ███████╗
██╔══╝  ██║██║   ██║██║╚██╔╝██║██╔══██║╚════╝██╔██╗ ██║     ██╔══██║╚════██║╚════██║██╔══╝     ██║   ╚════██║
██║     ██║╚██████╔╝██║ ╚═╝ ██║██║  ██║     ██╔╝ ██╗╚██████╗██║  ██║███████║███████║███████╗   ██║   ███████║
╚═╝     ╚═╝ ╚═════╝ ╚═╝     ╚═╝╚═╝  ╚═╝     ╚═╝  ╚═╝ ╚═════╝╚═╝  ╚═╝╚══════╝╚══════╝╚══════╝   ╚═╝   ╚══════╝
=============================================================================================================
"""
    fun displayProgName() {
        println(PROGNAME)
    }

    fun getEnv(value: String): String? {
        return System.getenv(value)
    }

    fun getConfiguration(args: Array<String>): Configuration {
        check(!args.isEmpty()) { "No configuration file found. Usage: ./figma-xcassets config.json" }
        Logger.info("Reading configuration file at ${args[0]}")
        return Configuration.decode(args[0])
    }
}
