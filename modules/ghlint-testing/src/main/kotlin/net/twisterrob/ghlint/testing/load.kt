package net.twisterrob.ghlint.testing

import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.FileLocation
import net.twisterrob.ghlint.model.RawFile
import net.twisterrob.ghlint.yaml.SnakeYaml
import org.intellij.lang.annotations.Language

internal fun load(
	@Language("yaml") yaml: String,
	fileName: String = "test.yml",
): File =
	SnakeYaml.load(RawFile(FileLocation(fileName), yaml))
