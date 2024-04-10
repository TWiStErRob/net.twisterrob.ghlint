# Reflective constructor invocations, see net.twisterrob.ghlint.ruleset.ReflectiveRuleSet#createRules().
-keepclassmembers class * implements net.twisterrob.ghlint.rule.Rule {
	public <init>();
}
