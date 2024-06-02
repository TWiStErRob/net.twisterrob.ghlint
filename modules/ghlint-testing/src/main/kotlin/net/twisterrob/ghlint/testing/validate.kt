package net.twisterrob.ghlint.testing

import net.twisterrob.ghlint.BuiltInRuleSet
import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.results.Finding
import org.intellij.lang.annotations.Language

internal fun validate(
	@Language("yaml") yaml: String,
	fileName: String = "test.yml",
): List<Finding> =
	validate(load(yaml, fileName, validate = false))

internal fun validate(file: File): List<Finding> =
	BuiltInRuleSet().createRules().flatMap { it.check(file) }
