package net.twisterrob.ghlint.rule

import net.twisterrob.ghlint.model.Action
import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.InvalidContent
import net.twisterrob.ghlint.model.Workflow
import org.junit.jupiter.api.Test
import org.mockito.Mockito.spy
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

class VisitorsTest {

	private val subject: VisitorRule = spy(OverrideEverythingVisitorRule())

	@Test fun testWorkflow() {
		val workflowFile: File = mock()
		val workflow: Workflow = mock()
		whenever(workflowFile.content).thenReturn(workflow)

		subject.check(workflowFile)

		verify(workflowFile, atLeastOnce()).content
		verifyNoMoreInteractions(subject, workflowFile)
	}

	@Test fun testAction() {
		val actionFile: File = mock()
		val action: Action = mock()
		whenever(actionFile.content).thenReturn(action)
		whenever(action.runs).thenReturn(mock<Action.Runs.CompositeRuns>())

		subject.check(actionFile)

		verify(actionFile, atLeastOnce()).content
		verifyNoMoreInteractions(subject, actionFile)
	}

	@Test fun testInvalid() {
		val invalidFile: File = mock()
		val invalid: InvalidContent = mock()
		whenever(invalidFile.content).thenReturn(invalid)

		subject.check(invalidFile)

		verify(invalidFile, atLeastOnce()).content
		verifyNoMoreInteractions(subject, invalidFile)
	}
}
