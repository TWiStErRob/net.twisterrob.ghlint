package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.ruleset.ReflectiveRuleSet
import net.twisterrob.ghlint.ruleset.RuleSet

public class DefaultRuleSet : RuleSet by ReflectiveRuleSet(
	id = "default",
	name = "Default",
	AlwaysDoubleCurlyIfRule::class,
	CreatePullRequestRule::class,
	ExplicitJobPermissionsRule::class,
	IdNamingRule::class,
	MandatoryNameRule::class,
	MandatoryShellRule::class,
	MissingJobTimeoutRule::class,
	NeverUseAlwaysRule::class,
	QuoteGithubOutputRule::class,
	QuoteGithubEnvRule::class,
	RemoveEmptyEnvRule::class,
	SetDefaultShellRule::class,
	UploadArtifactShouldFailOnMissingFilesRule::class,
	UseGhTokenWithGhCliRule::class,
	UseEnvInsteadOfTemplatingRule::class,
)
