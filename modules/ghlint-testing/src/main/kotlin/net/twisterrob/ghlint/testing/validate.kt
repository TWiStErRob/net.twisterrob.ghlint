package net.twisterrob.ghlint.testing

import net.twisterrob.ghlint.BuiltInRuleSet
import net.twisterrob.ghlint.results.Finding
import org.intellij.lang.annotations.Language

internal fun validate(
	@Language("yaml") yaml: String,
	fileName: String = "test.yml",
): List<Finding> {
	val file = load(yaml, fileName)
	return BuiltInRuleSet().createRules().flatMap { it.check(file) }
}
