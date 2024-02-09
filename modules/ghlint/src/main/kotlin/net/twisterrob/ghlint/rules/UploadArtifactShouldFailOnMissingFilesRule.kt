package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.Step
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.VisitorRule

public class UploadArtifactShouldFailOnMissingFilesRule : VisitorRule {

	override fun visitUsesStep(reporting: Reporting, step: Step.Uses) {
		super.visitUsesStep(reporting, step)
		if (step.uses.startsWith("actions/upload-artifact@")) {
			val specified = step.with.orEmpty().containsKey("if-no-files-found")
			if (!specified) {
				reporting.report(ShouldFailOnMissingFiles, step) { "${it} should have if-no-files-found: error." }
			}
		}
	}

	internal companion object {

		val ShouldFailOnMissingFiles =
			Issue("ShouldFailOnMissingFiles", "Shell should be set on job.")
	}
}
