package net.twisterrob.ghlint.rules.permissions

import net.twisterrob.ghlint.model.WorkflowStep
import java.net.URI

internal interface InferRequiredPermissions {
	val actionName: String
	val actionUrl: URI

	fun infer(step: WorkflowStep.Uses): Set<RequiredScope>
}
