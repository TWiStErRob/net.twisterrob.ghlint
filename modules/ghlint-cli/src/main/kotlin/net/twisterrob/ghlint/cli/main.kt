package net.twisterrob.ghlint.cli

import com.github.ajalt.clikt.core.main

public fun main(vararg args: String) {
	val command = CLI()
	val actualArgs: List<String> =
		if ("--no-exit" in args) {
			command.configureContext {
				exitProcess = { }
			}
			args.toList() - "--no-exit"
		} else {
			args.toList()
		}

	// Store original arguments in context data for custom help processing
	command.configureContext {
		data["originalArgs"] = actualArgs
	}

	command.main(actualArgs)
}
