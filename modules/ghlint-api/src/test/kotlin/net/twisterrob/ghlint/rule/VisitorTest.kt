package net.twisterrob.ghlint.rule

import net.twisterrob.ghlint.model.Action
import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.InvalidContent
import net.twisterrob.ghlint.model.Workflow
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

class VisitorTest {

	private val subject: Visitor = spy(object : Visitor {})
	private val reporting: Reporting = mock()

	@Test fun `workflow content is delegated`() {
		doNothing().whenever(subject).visitWorkflow(any(), any())
		val file: File = mock()
		val content: Workflow = mock()
		whenever(file.content).thenReturn(content)

		subject.visitFile(reporting, file)

		verify(subject).visitFile(eq(reporting), any())
		verify(subject).visitWorkflow(eq(reporting), any())
		verifyNoMoreInteractions(subject, reporting)
	}

	@Test fun `action content is delegated`() {
		doNothing().whenever(subject).visitAction(any(), any())
		val file: File = mock()
		val content: Action = mock()
		whenever(file.content).thenReturn(content)

		subject.visitFile(reporting, file)

		verify(subject).visitFile(eq(reporting), any())
		verify(subject).visitAction(eq(reporting), any())
		verifyNoMoreInteractions(subject, reporting)
	}

	@Test fun `invalid content is delegated`() {
		doNothing().whenever(subject).visitInvalidContent(any(), any())
		val file: File = mock()
		val content: InvalidContent = mock()
		whenever(file.content).thenReturn(content)

		subject.visitFile(reporting, file)

		verify(subject).visitFile(eq(reporting), any())
		verify(subject).visitInvalidContent(eq(reporting), any())
		verifyNoMoreInteractions(subject, reporting)
	}
}
