package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.Step
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.VisitorRule
import net.twisterrob.ghlint.rule.report

public class CreatePullRequestRule : VisitorRule {

	override val issues: List<Issue> = listOf(UseGhCliToOpenPr)

	override fun visitUsesStep(reporting: Reporting, step: Step.Uses) {
		super.visitUsesStep(reporting, step)
		if (step.uses.startsWith("peter-evans/create-pull-request@")) {
			reporting.report(UseGhCliToOpenPr, step) { "Use `gh pr create` to open a PR instead of ${it}." }
		}
	}

	internal companion object {

		val UseGhCliToOpenPr =
			Issue("UseGhCliToOpenPr", "Action doesn't allow fast fail, black-listed.")
	}
}
