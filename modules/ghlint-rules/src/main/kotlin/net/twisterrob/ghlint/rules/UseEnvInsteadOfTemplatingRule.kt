package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.Step
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.VisitorRule
import net.twisterrob.ghlint.rule.report

public class UseEnvInsteadOfTemplatingRule : VisitorRule {

	override val issues: List<Issue> = listOf(UseEnvInsteadOfTemplating)

	override fun visitRunStep(reporting: Reporting, step: Step.Run) {
		super.visitRunStep(reporting, step)
		if (step.run.contains("\${{")) {
			reporting.report(UseEnvInsteadOfTemplating, step) { "${it} script must not contain GitHub Expressions." }
		}
	}

	override fun visitUsesStep(reporting: Reporting, step: Step.Uses) {
		super.visitUsesStep(reporting, step)
		if (step.uses.startsWith("actions/github-script@")) {
			reporting.report(UseEnvInsteadOfTemplating, step) { "${it} script must not contain GitHub Expressions." }
		}
	}

	internal companion object {

		val UseEnvInsteadOfTemplating =
			Issue("UseEnvInsteadOfTemplating", "Use environment variables instead of templating")
	}
}
