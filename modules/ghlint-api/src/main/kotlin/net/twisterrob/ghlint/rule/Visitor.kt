package net.twisterrob.ghlint.rule

import net.twisterrob.ghlint.model.Action
import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.Workflow
import javax.annotation.OverridingMethodsMustInvokeSuper

public interface Visitor : WorkflowVisitor, ActionVisitor, YamlVisitor {

	@OverridingMethodsMustInvokeSuper
	public override fun visitFile(reporting: Reporting, file: File) {
		super<YamlVisitor>.visitFile(reporting, file)
		when (file.content) {
			is Workflow -> super<WorkflowVisitor>.visitFile(reporting, file)
			is Action -> super<ActionVisitor>.visitFile(reporting, file)
		}
	}
}
