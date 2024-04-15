package net.twisterrob.ghlint.rule

import net.twisterrob.ghlint.model.Step
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions

class WorkflowVisitorTest {

	private val subject: WorkflowVisitor = spy(object : WorkflowVisitor {})
	private val reporting: Reporting = mock()

	@Test fun `visitRunStep does nothing`() {
		val step: Step.Run = mock()

		subject.visitRunStep(reporting, step)

		verify(subject).visitRunStep(reporting, step)
		verifyNoMoreInteractions(subject, reporting, step)
	}
}
