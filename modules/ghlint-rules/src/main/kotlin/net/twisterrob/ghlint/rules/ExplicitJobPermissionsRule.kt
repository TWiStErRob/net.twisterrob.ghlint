package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.Job
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.VisitorRule
import net.twisterrob.ghlint.rule.report

public class ExplicitJobPermissionsRule : VisitorRule {

	override fun visitNormalJob(reporting: Reporting, job: Job.NormalJob) {
		super.visitNormalJob(reporting, job)
		if (job.permissions == null && job.parent.permissions == null) {
			reporting.report(MissingJobPermissions, job) { "${it} is missing permissions." }
		}
		if (job.permissions == null && job.parent.permissions != null) {
			reporting.report(ExplicitJobPermissions, job) { "${it} should have explicit permissions." }
		}
	}

	internal companion object {

		val MissingJobPermissions =
			Issue("MissingJobPermissions", "Missing permission declaration.")

		val ExplicitJobPermissions =
			Issue("ExplicitJobPermissions", "Permissions should be declared on the job level.")
	}
}
