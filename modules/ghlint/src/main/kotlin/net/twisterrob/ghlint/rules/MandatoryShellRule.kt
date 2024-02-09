package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.Step
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.VisitorRule

public class MandatoryShellRule : VisitorRule {

	override fun visitRunStep(reporting: Reporting, step: Step.Run) {
		val shell = step.shell ?: step.parent.defaults?.run?.shell
		if (shell == null) {
			reporting.report(MandatoryShell, step) { "${it} must have a shell defined." }
		}
	}

	internal companion object {

		val MandatoryShell =
			Issue("MandatoryShell", "Run step must have a shell")
	}
}
