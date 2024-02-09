package net.twisterrob.ghlint.analysis

import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.FileName
import net.twisterrob.ghlint.reporting.SarifReporter
import net.twisterrob.ghlint.reporting.TextReporter
import net.twisterrob.ghlint.rules.CreatePullRequestRule
import net.twisterrob.ghlint.rules.IdNamingRule
import net.twisterrob.ghlint.rules.MandatoryNameRule
import net.twisterrob.ghlint.rules.MandatoryShellRule
import net.twisterrob.ghlint.rules.RemoveEmptyEnvRule
import net.twisterrob.ghlint.rules.SetDefaultShellRule
import net.twisterrob.ghlint.rules.UploadArtifactShouldFailOnMissingFilesRule
import net.twisterrob.ghlint.rules.UseGhTokenWithGhCliRule
import java.nio.file.Path
import kotlin.io.path.bufferedWriter

public fun main(vararg args: String) {
	val rules = listOf(
		CreatePullRequestRule(),
		IdNamingRule(),
		MandatoryNameRule(),
		MandatoryShellRule(),
		RemoveEmptyEnvRule(),
		SetDefaultShellRule(),
		UploadArtifactShouldFailOnMissingFilesRule(),
		UseGhTokenWithGhCliRule(),
	)
	val files = args.map { File(FileName(it)) }

	val validation = Validator().validateWorkflows(files)
	val findings = Analyzer().analyzeWorkflows(files, rules)
	val allFindings = validation + findings

	TextReporter(System.out).report(allFindings)
	Path.of("report.sarif").bufferedWriter().use { writer ->
		val reporter = SarifReporter(
			target = writer,
			rootDir = Path.of(".")
		)
		reporter.report(allFindings)
	}
}
