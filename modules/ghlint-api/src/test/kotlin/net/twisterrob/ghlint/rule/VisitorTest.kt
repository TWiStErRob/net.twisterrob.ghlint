package net.twisterrob.ghlint.rule

import net.twisterrob.ghlint.model.Action
import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.InvalidContent
import net.twisterrob.ghlint.model.Workflow
import org.junit.jupiter.api.Test
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

class VisitorTest {

	private val subject: Visitor = spy(object : Visitor {})
	private val reporting: Reporting = mock()

	@Test fun `workflow content is delegated`() {
		val file: File = mock()
		val content: Workflow = mock()
		whenever(file.content).thenReturn(content)
		doNothing().whenever(subject).visitWorkflow(reporting, content)

		subject.visitFile(reporting, file)

		verify(subject).visitFile(reporting, file)
		verify(subject).visitWorkflow(reporting, content)
		verify(file, atLeastOnce()).content
		verifyNoMoreInteractions(subject, reporting, file, content)
	}

	@Test fun `action content is delegated`() {
		val file: File = mock()
		val content: Action = mock()
		whenever(file.content).thenReturn(content)
		doNothing().whenever(subject).visitAction(reporting, content)

		subject.visitFile(reporting, file)

		verify(subject).visitFile(reporting, file)
		verify(subject).visitAction(reporting, content)
		verify(file, atLeastOnce()).content
		verifyNoMoreInteractions(subject, reporting, file, content)
	}

	@Test fun `invalid content is delegated`() {
		val file: File = mock()
		val content: InvalidContent = mock()
		whenever(file.content).thenReturn(content)
		doNothing().whenever(subject).visitInvalidContent(reporting, content)

		subject.visitFile(reporting, file)

		verify(subject).visitFile(reporting, file)
		verify(subject).visitInvalidContent(reporting, content)
		verify(file, atLeastOnce()).content
		verifyNoMoreInteractions(subject, reporting, file, content)
	}
}
