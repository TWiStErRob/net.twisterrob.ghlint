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
	validate: Boolean = true,
): File {
	val file = SnakeYaml.load(RawFile(FileLocation(fileName), yaml))
	if (validate) {
		val validation = validate(file)
		@Suppress("detekt.ForbiddenMethodCall") // TODO logging.
		if (isDebugEnabled) validation.forEach { println(it.testString()) }
		validation shouldHave noFindings()
	}
	return file
}
