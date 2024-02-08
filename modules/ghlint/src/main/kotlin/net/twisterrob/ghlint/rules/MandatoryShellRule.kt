package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.Issue
import net.twisterrob.ghlint.model.Job
import net.twisterrob.ghlint.model.Reporting
import net.twisterrob.ghlint.model.Rule
import net.twisterrob.ghlint.model.Step
import net.twisterrob.ghlint.model.Workflow

public class MandatoryShellRule : Rule {

	override fun visitWorkflow(reporting: Reporting, workflow: Workflow) {
		TODO("not implemented") // BRAIS default impl
	}

	override fun visitJob(reporting: Reporting, job: Job) {
		val shell = job.defaults?.shell
		reporting.putState(this, "shell", shell)
		// BRAIS super
	}

	override fun visitStep(reporting: Reporting, step: Step) {
		if (step is Step.Run) {
			val defaultShell = reporting.getState(this, "shell") as? String?
			val shell = step.shell ?: defaultShell
			if (shell == null) {
				reporting.report(MandatoryShell, step)
			}
		}
	}

	internal companion object {

		val MandatoryShell =
			Issue("MandatoryShell", "Run step must have a shell")
	}
}
