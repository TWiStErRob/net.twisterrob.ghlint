package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.Job
import net.twisterrob.ghlint.model.Step
import net.twisterrob.ghlint.rule.report
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.VisitorRule

public class AlwaysDoubleCurlyIfRule : VisitorRule {

	override fun visitJob(reporting: Reporting, job: Job) {
		super.visitJob(reporting, job)
		val condition = job.`if`
		if (!isConditionSafe(condition)) {
			reporting.report(AlwaysDoubleCurlyIf, job) { "${it} must have double-curly-braces." }
		}
	}

	override fun visitStep(reporting: Reporting, step: Step) {
		super.visitStep(reporting, step)
		val condition = step.`if`
		if (!isConditionSafe(condition)) {
			reporting.report(AlwaysDoubleCurlyIf, step) { "${it} must have double-curly-braces." }
		}
	}

	private fun isConditionSafe(condition: String?): Boolean =
		condition == null || condition.startsWith("\${{") && condition.endsWith("}}")

	internal companion object {

		val AlwaysDoubleCurlyIf =
			Issue("AlwaysDoubleCurlyIf", "If must be wrapped in double-curly-braces.")
	}
}
