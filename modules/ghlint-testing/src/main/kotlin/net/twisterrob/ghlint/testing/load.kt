package net.twisterrob.ghlint.testing

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.RawFile
import net.twisterrob.ghlint.yaml.SnakeYaml

public fun load(file: RawFile): File {
	val loadedFile = loadUnsafe(file)
	val validation = validate(loadedFile)
	@Suppress("detekt.ForbiddenMethodCall") // TODO logging.
	if (isDebugEnabled) validation.forEach { println(it.testString()) }
	validation shouldHave noFindings()
	return loadedFile
}

public fun loadUnsafe(file: RawFile): File =
	SnakeYaml.load(file)
