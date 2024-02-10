package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.Job
import net.twisterrob.ghlint.model.Step
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.VisitorRule

public class SetDefaultShellRule : VisitorRule {

	override fun visitNormalJob(reporting: Reporting, job: Job.NormalJob) {
		super.visitNormalJob(reporting, job)
		if (job.defaults?.run?.shell == null) {
			val explicitShells = job.steps.count { it is Step.Run && it.shell != null }
			if (explicitShells >= MAX_SHELL_SPECIFICATIONS) {
				reporting.report(SetDefaultShell, job) { "${it} should have shell defined as defaults." }
			}
		}
	}

	internal companion object {

		private const val MAX_SHELL_SPECIFICATIONS = 3

		val SetDefaultShell =
			Issue("SetDefaultShell", "Shell should be set on job.")
	}
}
