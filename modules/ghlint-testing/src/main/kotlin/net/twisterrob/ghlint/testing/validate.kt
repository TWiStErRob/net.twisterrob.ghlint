package net.twisterrob.ghlint.testing

import net.twisterrob.ghlint.BuiltInRuleSet
import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.RawFile
import net.twisterrob.ghlint.results.Finding

internal fun validate(file: RawFile): List<Finding> =
	validate(loadUnsafe(file))

internal fun validate(file: File): List<Finding> =
	BuiltInRuleSet().createRules().flatMap { it.check(file) }
