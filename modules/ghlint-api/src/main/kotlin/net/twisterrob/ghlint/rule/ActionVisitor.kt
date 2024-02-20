package net.twisterrob.ghlint.rule

import net.twisterrob.ghlint.model.Action
import net.twisterrob.ghlint.model.File
import javax.annotation.OverridingMethodsMustInvokeSuper

public interface ActionVisitor {

	@OverridingMethodsMustInvokeSuper
	public fun visitFile(reporting: Reporting, file: File) {
		visitAction(reporting, file.content as Action)
	}

	@OverridingMethodsMustInvokeSuper
	public fun visitAction(reporting: Reporting, action: Action) {
		action.inputs.values.forEach { input ->
			visitInput(reporting, input)
		}
	}

	@OverridingMethodsMustInvokeSuper
	public fun visitInput(reporting: Reporting, input: Action.ActionInput) {
		// No children.
	}

	@OverridingMethodsMustInvokeSuper
	public fun visitRuns(reporting: Reporting, runs: Action.Runs) {
		// No children.
	}
}
