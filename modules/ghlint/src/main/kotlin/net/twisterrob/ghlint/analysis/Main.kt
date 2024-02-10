package net.twisterrob.ghlint.analysis

import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.FileName
import net.twisterrob.ghlint.reporting.SarifReporter
import net.twisterrob.ghlint.reporting.TextReporter
import net.twisterrob.ghlint.rules.AlwaysDoubleCurlyIfRule
import net.twisterrob.ghlint.rules.CreatePullRequestRule
import net.twisterrob.ghlint.rules.ExplicitJobPermissionsRule
import net.twisterrob.ghlint.rules.IdNamingRule
import net.twisterrob.ghlint.rules.MandatoryNameRule
import net.twisterrob.ghlint.rules.MandatoryShellRule
import net.twisterrob.ghlint.rules.MissingJobTimeoutRule
import net.twisterrob.ghlint.rules.NeverUseAlwaysRule
import net.twisterrob.ghlint.rules.QuoteGithubEnvRule
import net.twisterrob.ghlint.rules.QuoteGithubOutputRule
import net.twisterrob.ghlint.rules.RemoveEmptyEnvRule
import net.twisterrob.ghlint.rules.SetDefaultShellRule
import net.twisterrob.ghlint.rules.UploadArtifactShouldFailOnMissingFilesRule
import net.twisterrob.ghlint.rules.UseEnvInsteadOfTemplatingRule
import net.twisterrob.ghlint.rules.UseGhTokenWithGhCliRule
import net.twisterrob.ghlint.ruleset.ReflectiveRuleSet
import java.nio.file.Path

public fun main(vararg args: String) {
	val defaultRuleSet = ReflectiveRuleSet(
		"Default",
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
	val files = args.map { File(FileName(it)) }

	val validation = Validator().validateWorkflows(files)
	val findings = Analyzer().analyzeWorkflows(files, defaultRuleSet)
	val allFindings = validation + findings

	TextReporter(System.out).report(allFindings)
	SarifReporter.report(allFindings, Path.of("report.sarif"), Path.of("."))
}
