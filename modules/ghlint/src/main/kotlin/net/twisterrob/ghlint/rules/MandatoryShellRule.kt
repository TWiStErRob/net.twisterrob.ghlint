package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.Issue
import net.twisterrob.ghlint.model.Reporting
import net.twisterrob.ghlint.model.Step
import net.twisterrob.ghlint.model.VisitorRule

public class MandatoryShellRule : VisitorRule {

	override fun visitRunStep(reporting: Reporting, step: Step.Run) {
		val shell = step.shell ?: step.parent.defaults?.shell
		if (shell == null) {
			reporting.report(MandatoryShell, step)
		}
	}

	internal companion object {

		val MandatoryShell =
			Issue("MandatoryShell", "Run step must have a shell")
	}
}
