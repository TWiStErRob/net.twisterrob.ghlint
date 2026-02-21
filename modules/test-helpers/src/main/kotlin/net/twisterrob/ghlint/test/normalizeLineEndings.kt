package net.twisterrob.ghlint.test

public fun String.normalizeLineEndings(): String =
	this
		.replace("\r\n", "\n")
		.replace("\r", "\n")
