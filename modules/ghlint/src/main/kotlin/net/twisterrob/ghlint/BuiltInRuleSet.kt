package net.twisterrob.ghlint

import net.twisterrob.ghlint.analysis.ValidationRule
import net.twisterrob.ghlint.ruleset.ReflectiveRuleSet
import net.twisterrob.ghlint.ruleset.RuleSet

public class BuiltInRuleSet : RuleSet by ReflectiveRuleSet(
	"built-in",
	"Internal",
	ValidationRule::class,
)
