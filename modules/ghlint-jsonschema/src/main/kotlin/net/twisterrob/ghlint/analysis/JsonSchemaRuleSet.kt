package net.twisterrob.ghlint.analysis

import net.twisterrob.ghlint.ruleset.ReflectiveRuleSet
import net.twisterrob.ghlint.ruleset.RuleSet

public class JsonSchemaRuleSet : RuleSet by ReflectiveRuleSet(
	"built-in",
	"Internal",
	SyntaxErrorRule::class,
	JsonSchemaValidationRule::class,
)
