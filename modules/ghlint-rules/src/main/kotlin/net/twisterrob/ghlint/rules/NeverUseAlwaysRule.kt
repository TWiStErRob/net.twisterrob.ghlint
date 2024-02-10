package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.Job
import net.twisterrob.ghlint.model.Step
import net.twisterrob.ghlint.rule.report
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.VisitorRule

public class NeverUseAlwaysRule : VisitorRule {

	override fun visitJob(reporting: Reporting, job: Job) {
		super.visitJob(reporting, job)
		if (job.`if`?.contains("always()") == true) {
			reporting.report(NeverUseAlways, job) { "${it} must not use always() condition." }
		}
	}

	override fun visitStep(reporting: Reporting, step: Step) {
		super.visitStep(reporting, step)
		if (step.`if`?.contains("always()") == true) {
			reporting.report(NeverUseAlways, step) { "${it} must not use always() condition." }
		}
	}

	internal companion object {

		val NeverUseAlways =
			Issue("NeverUseAlways", "Must not use always() condition.")
	}
}
