package net.twisterrob.ghlint.rule

import net.twisterrob.ghlint.model.Action
import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.InvalidContent
import net.twisterrob.ghlint.model.Workflow
import javax.annotation.OverridingMethodsMustInvokeSuper

public interface Visitor : WorkflowVisitor, ActionVisitor {

	@OverridingMethodsMustInvokeSuper
	public override fun visitFile(reporting: Reporting, file: File) {
		when (val content = file.content) {
			is Workflow -> super<WorkflowVisitor>.visitFile(reporting, file)
			is Action -> super<ActionVisitor>.visitFile(reporting, file)
			is InvalidContent -> visitInvalidContent(reporting, content)
		}
	}

	@OverridingMethodsMustInvokeSuper
	public fun visitInvalidContent(reporting: Reporting, content: InvalidContent) {
		// No op, the system should already have reported this as an error.
	}
}
