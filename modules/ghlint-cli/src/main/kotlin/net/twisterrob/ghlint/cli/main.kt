package net.twisterrob.ghlint.cli

import com.github.ajalt.clikt.core.main

public fun main(vararg args: String) {
	val command = CLI()
	val actualArgs: List<String> =
		if (args.firstOrNull() == "--no-exit") {
			command.configureContext {
				exitProcess = { }
			}
			args.drop(1)
		} else {
			args.toList()
		}
	command.main(actualArgs)
}
