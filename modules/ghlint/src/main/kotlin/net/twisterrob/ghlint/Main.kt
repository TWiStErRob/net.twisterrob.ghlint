package net.twisterrob.ghlint

import net.twisterrob.ghlint.analysis.Analyzer
import net.twisterrob.ghlint.analysis.Validator
import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.FileName
import net.twisterrob.ghlint.model.SnakeWorkflow
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
	val workflows = files.map(SnakeWorkflow::from)

	val validationResults = Validator().validateWorkflows(files)
	val analysisResults = Analyzer().analyzeWorkflows(workflows, defaultRuleSet)
	val allFindings = validationResults + analysisResults

	TextReporter(System.out).report(allFindings)
	SarifReporter.report(allFindings, Path.of("report.sarif"), Path.of("."))
}
