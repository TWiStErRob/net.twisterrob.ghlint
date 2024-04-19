package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.ruleset.ReflectiveRuleSet
import net.twisterrob.ghlint.ruleset.RuleSet

public class DefaultRuleSet : RuleSet by ReflectiveRuleSet(
	id = "default",
	name = "Default",
	ComponentCountRule::class,
	DoubleCurlyIfRule::class,
	DuplicateShellRule::class,
	DuplicateStepIdRule::class,
	EmptyEnvRule::class,
	EnvironmentFileOverwriteRule::class,
	ExplicitJobPermissionsRule::class,
	FailFastActionsRule::class,
	IdNamingRule::class,
	ImplicitStatusCheckRule::class,
	JobDependenciesRule::class,
	MissingGhTokenRule::class,
	MissingJobTimeoutRule::class,
	MissingNameRule::class,
	MissingShellRule::class,
	PreferGitHubTokenRule::class,
	RedundantShellRule::class,
	SafeEnvironmentFileRedirectRule::class,
	ScriptInjectionRule::class,
)
