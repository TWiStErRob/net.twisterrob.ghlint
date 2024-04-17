package net.twisterrob.ghlint.rule.visitor

import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.InvalidContent
import net.twisterrob.ghlint.rule.Reporting
import javax.annotation.OverridingMethodsMustInvokeSuper

public interface InvalidContentVisitor {

	@OverridingMethodsMustInvokeSuper
	public fun visitInvalidContentFile(reporting: Reporting, file: File) {
		visitInvalidContent(reporting, file.content as InvalidContent)
	}

	@OverridingMethodsMustInvokeSuper
	public fun visitInvalidContent(reporting: Reporting, content: InvalidContent) {
		// No op, the system should already have reported this as an error.
	}
}
