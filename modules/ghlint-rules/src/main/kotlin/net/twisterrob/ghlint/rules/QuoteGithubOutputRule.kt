package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.Step
import net.twisterrob.ghlint.results.report
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.VisitorRule

public class QuoteGithubOutputRule : VisitorRule {

	override fun visitRunStep(reporting: Reporting, step: Step.Run) {
		super.visitRunStep(reporting, step)
		if (step.run.contains(GITHUB_OUTPUT_REGEX)) {
			reporting.report(QuoteGithubOutput, step) { "${it} must be quoted." }
		}
	}

	internal companion object {

		private val GITHUB_OUTPUT_REGEX = Regex("""(?<!")\$(?!\{)GITHUB_OUTPUT(?!")""")

		val QuoteGithubOutput =
			Issue("QuoteGithubOutput", "GITHUB_OUTPUT must be quoted.")
	}
}
