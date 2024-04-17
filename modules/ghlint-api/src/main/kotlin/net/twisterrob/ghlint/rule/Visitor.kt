package net.twisterrob.ghlint.rule

import net.twisterrob.ghlint.model.Action
import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.InvalidContent
import net.twisterrob.ghlint.model.Workflow
import javax.annotation.OverridingMethodsMustInvokeSuper

public interface Visitor : WorkflowVisitor, ActionVisitor, InvalidContentVisitor {

	@OverridingMethodsMustInvokeSuper
	public fun visitFile(reporting: Reporting, file: File) {
		when (file.content) {
			is Workflow -> visitWorkflowFile(reporting, file)
			is Action -> visitActionFile(reporting, file)
			is InvalidContent -> visitInvalidContentFile(reporting, file)
		}
	}
}
