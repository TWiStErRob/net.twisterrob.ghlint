package net.twisterrob.ghlint.testing

import net.twisterrob.ghlint.BuiltInRuleSet
import net.twisterrob.ghlint.model.FileLocation
import net.twisterrob.ghlint.model.RawFile
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.yaml.SnakeYaml
import org.intellij.lang.annotations.Language

public fun validate(
	@Language("yaml") yml: String,
	fileName: String = "test.yml",
): List<Finding> {
	val file = SnakeYaml.load(RawFile(FileLocation(fileName), yml))
	return BuiltInRuleSet().createRules().flatMap { it.check(file) }
}
