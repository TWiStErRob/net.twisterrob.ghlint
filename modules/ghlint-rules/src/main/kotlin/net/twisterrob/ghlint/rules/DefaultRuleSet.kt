package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.ruleset.ReflectiveRuleSet
import net.twisterrob.ghlint.ruleset.RuleSet

public class DefaultRuleSet : RuleSet by ReflectiveRuleSet(
	id = "default",
	name = "Default",
	DoubleCurlyIfRule::class,
	ExplicitJobPermissionsRule::class,
	IdNamingRule::class,
	MissingShellRule::class,
	ComponentCountRule::class,
	DuplicateStepIdRule::class,
	MissingNameRule::class,
	MissingJobTimeoutRule::class,
	ImplicitStatusCheckRule::class,
	SafeEnvironmentFileRedirectRule::class,
	EnvironmentFileOverwriteRule::class,
	EmptyEnvRule::class,
	DuplicateShellRule::class,
	RedundantShellRule::class,
	FailFastActionsRule::class,
	MissingGhTokenRule::class,
	ScriptInjectionRule::class,
)
