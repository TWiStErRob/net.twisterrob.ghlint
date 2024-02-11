package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.Step
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.VisitorRule
import net.twisterrob.ghlint.rule.report

public class UploadArtifactShouldFailOnMissingFilesRule : VisitorRule {

	override val issues: List<Issue> = listOf(ShouldFailOnMissingFiles)

	override fun visitUsesStep(reporting: Reporting, step: Step.Uses) {
		super.visitUsesStep(reporting, step)
		if (step.uses.startsWith("actions/upload-artifact@")) {
			val isSpecified = step.with.orEmpty().containsKey("if-no-files-found")
			if (!isSpecified) {
				reporting.report(ShouldFailOnMissingFiles, step) { "${it} should have if-no-files-found: error." }
			}
		}
	}

	internal companion object {

		val ShouldFailOnMissingFiles =
			Issue("ShouldFailOnMissingFiles", "Shell should be set on job.")
	}
}
