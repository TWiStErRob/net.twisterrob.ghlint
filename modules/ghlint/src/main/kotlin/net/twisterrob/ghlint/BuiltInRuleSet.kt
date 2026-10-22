package net.twisterrob.ghlint

import net.twisterrob.ghlint.analysis.ValidationRule
import net.twisterrob.ghlint.ruleset.LazyRuleSet
import net.twisterrob.ghlint.ruleset.RuleSet
import net.twisterrob.ghlint.yaml.YamlValidation

public class BuiltInRuleSet internal constructor(validation: YamlValidation) : RuleSet by LazyRuleSet(
	"built-in",
	"Internal",
	{ ValidationRule(validation) },
) {
	public constructor() : this(YamlValidation.default)
}
