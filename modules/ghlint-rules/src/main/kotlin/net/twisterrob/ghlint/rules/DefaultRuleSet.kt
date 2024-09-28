package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.ruleset.LazyRuleSet
import net.twisterrob.ghlint.ruleset.RuleSet

public class DefaultRuleSet : RuleSet by LazyRuleSet(
	id = "default",
	name = "Default",
	::ComponentCountRule,
	::DoubleCurlyIfRule,
	::DuplicateShellRule,
	::DuplicateStepIdRule,
	::EmptyEnvRule,
	::EnvironmentFileOverwriteRule,
	::ExplicitJobPermissionsRule,
	::FailFastActionsRule,
	::IdNamingRule,
	::ImplicitStatusCheckRule,
	::InvalidExpressionUsageRule,
	::JobDependenciesRule,
	::MissingGhRepoRule,
	::MissingGhTokenRule,
	::MissingJobTimeoutRule,
	::MissingNameRule,
	::MissingShellRule,
	::PreferGitHubTokenRule,
	::RedundantShellRule,
	::SafeEnvironmentFileRedirectRule,
	::ScriptInjectionRule,
)
