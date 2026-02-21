package net.twisterrob.ghlint.test

public fun Class<*>.readResourceText(path: String): String =
	(getResourceAsStream(path)
		?: error("Cannot find resource: $path"))
		.reader()
		.use { it.readText() }
