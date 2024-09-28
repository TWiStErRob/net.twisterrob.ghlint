package net.twisterrob.ghlint

import net.twisterrob.ghlint.analysis.ValidationRule
import net.twisterrob.ghlint.ruleset.LazyRuleSet
import net.twisterrob.ghlint.ruleset.RuleSet

public class BuiltInRuleSet : RuleSet by LazyRuleSet(
	"built-in",
	"Internal",
	::ValidationRule,
)
