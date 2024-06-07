package net.twisterrob.ghlint.rules.permissions

import net.twisterrob.ghlint.model.WorkflowStep
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

internal fun stepWith(vararg pair: Pair<String, String>): WorkflowStep.Uses =
	stepWith(mapOf(*pair))

internal fun stepWith(map: Map<String, String>?): WorkflowStep.Uses {
	val mock: WorkflowStep.Uses = mock()
	whenever(mock.with).thenReturn(map)
	return mock
}
