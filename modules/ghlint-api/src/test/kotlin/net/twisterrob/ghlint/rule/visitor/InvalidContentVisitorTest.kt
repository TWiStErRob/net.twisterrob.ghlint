package net.twisterrob.ghlint.rule.visitor

import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.InvalidContent
import net.twisterrob.ghlint.rule.Reporting
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

class InvalidContentVisitorTest {

	private val subject: InvalidContentVisitor = spy(object : InvalidContentVisitor {})
	private val reporting: Reporting = mock()

	@Test fun `visitInvalidContentFile delegates invalid content`() {
		val target: File = mock()
		val child: InvalidContent = mock()
		whenever(target.content).thenReturn(child)
		doNothing().whenever(subject).visitInvalidContent(reporting, child)

		subject.visitInvalidContentFile(reporting, target)

		verify(target).content
		verify(subject).visitInvalidContentFile(reporting, target)
		verify(subject).visitInvalidContent(reporting, child)
		verifyNoMoreInteractions(subject, reporting, target, child)
	}

	@Test fun `visitInvalidContent does nothing`() {
		val target: InvalidContent = mock()

		subject.visitInvalidContent(reporting, target)

		verify(subject).visitInvalidContent(reporting, target)
		verifyNoMoreInteractions(subject, reporting, target)
	}
}
