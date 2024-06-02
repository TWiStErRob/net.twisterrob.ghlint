package net.twisterrob.ghlint.testing

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.FileLocation
import net.twisterrob.ghlint.model.RawFile
import net.twisterrob.ghlint.yaml.SnakeYaml
import org.intellij.lang.annotations.Language

public fun load(
	@Language("yaml") yaml: String,
	fileName: String = "test.yml",
): File {
	val file = loadUnsafe(yaml, fileName)
	val validation = validate(file)
	@Suppress("detekt.ForbiddenMethodCall") // TODO logging.
	if (isDebugEnabled) validation.forEach { println(it.testString()) }
	validation shouldHave noFindings()
	return file
}

public fun loadUnsafe(
	@Language("yaml") yaml: String,
	fileName: String = "test.yml",
): File =
	SnakeYaml.load(RawFile(FileLocation(fileName), yaml))
