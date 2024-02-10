package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.Step
import net.twisterrob.ghlint.results.report
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.VisitorRule

public class UseGhTokenWithGhCliRule : VisitorRule {

	override fun visitRunStep(reporting: Reporting, step: Step.Run) {
		super.visitRunStep(reporting, step)
		if (usesGhCli(step.run)) {
			val hasGhToken = step.env.hasTokenVar || step.parent.env.hasTokenVar || step.parent.parent.env.hasTokenVar
			if (!hasGhToken) {
				reporting.report(MissingGhToken, step) { "${it} should have ${TOKEN_ENV_VAR}" }
			}
		}
	}

	private fun usesGhCli(script: String): Boolean =
		script.contains(GH_CLI_START_OF_LINE) || script.contains(GH_CLI_EMBEDDED)

	internal companion object {

		private val GH_CLI_START_OF_LINE = Regex("""^\s*gh\s+""")
		private val GH_CLI_EMBEDDED = Regex("""^\$\(gh\s+""")
		private const val TOKEN_ENV_VAR = "GH_TOKEN"

		private val Map<String, String>?.hasTokenVar: Boolean
			get() = orEmpty().containsKey(TOKEN_ENV_VAR)

		val MissingGhToken =
			Issue("MissingGhToken", "GH_TOKEN is required for using the `gh` CLI tool.")
	}
}
