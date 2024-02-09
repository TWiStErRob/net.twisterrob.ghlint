package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.Step
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.VisitorRule

public class UseGhTokenWithGhCliRule : VisitorRule {

	override fun visitRunStep(reporting: Reporting, step: Step.Run) {
		super.visitRunStep(reporting, step)
		if (usesGhCli(step.run)) {
			val hasGhToken = step.env.orEmpty().containsKey("GH_TOKEN")
					|| step.parent.env.orEmpty().containsKey("GH_TOKEN")
					|| step.parent.parent.env.orEmpty().containsKey("GH_TOKEN")
			if (!hasGhToken) {
				reporting.report(MissingGhToken, step) { "${it} should have GH_TOKEN" }
			}
		}
	}

	private fun usesGhCli(script: String): Boolean =
		script.contains(GH_CLI_START_OF_LINE) || script.contains(GH_CLI_EMBEDDED)

	internal companion object {

		private val GH_CLI_START_OF_LINE = Regex("""^\s*gh\s+""")
		private val GH_CLI_EMBEDDED = Regex("""^\$\(gh\s+""")

		val MissingGhToken =
			Issue("MissingGhToken", "GH_TOKEN is required for using the `gh` CLI tool.")
	}
}
