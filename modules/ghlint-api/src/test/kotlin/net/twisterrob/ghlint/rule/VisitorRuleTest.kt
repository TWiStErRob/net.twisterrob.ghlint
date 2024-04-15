package net.twisterrob.ghlint.rule

import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.collections.containExactly
import io.kotest.matchers.should
import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.results.Finding
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

class VisitorRuleTest {

	private val subject: VisitorRule = spy(object : VisitorRule {
		override val issues: List<Issue> = emptyList()
	})

	@Test fun `check calls visitFile for reporting`() {
		val file: File = mock()
		val content: Workflow = mock()
		whenever(file.content).thenReturn(content)
		doNothing().whenever(subject).visitFile(any(), eq(file))

		val results = subject.check(file)

		results should beEmpty()
		verify(subject).check(file)
		verify(subject).visitFile(any(), eq(file))
		verifyNoMoreInteractions(subject, file, content)
	}

	@Test fun `check collects findings from visitFile`() {
		val file: File = mock()
		val content: Workflow = mock()
		whenever(file.content).thenReturn(content)
		val finding: Finding = mock()
		doAnswer {
			it.getArgument<Reporting>(0).report(finding)
		}.whenever(subject).visitFile(any(), eq(file))

		val results = subject.check(file)

		results should containExactly(finding)
		verify(subject).check(file)
		verify(subject).visitFile(any(), eq(file))
		verifyNoMoreInteractions(subject, file, content)
	}

	@Test fun `duplicate findings are reported`() {
		val file: File = mock()
		val content: Workflow = mock()
		whenever(file.content).thenReturn(content)
		val finding: Finding = mock()
		doAnswer {
			it.getArgument<Reporting>(0).report(finding)
			it.getArgument<Reporting>(0).report(finding)
		}.whenever(subject).visitFile(any(), eq(file))

		val results = subject.check(file)

		results should containExactly(finding, finding)
		verify(subject).check(file)
		verify(subject).visitFile(any(), eq(file))
		verifyNoMoreInteractions(subject, file, content)
	}

	@Test fun `multiple findings are reported`() {
		val file: File = mock()
		val content: Workflow = mock()
		whenever(file.content).thenReturn(content)
		val finding1: Finding = mock()
		val finding2: Finding = mock()
		doAnswer {
			it.getArgument<Reporting>(0).report(finding1)
			it.getArgument<Reporting>(0).report(finding2)
		}.whenever(subject).visitFile(any(), eq(file))

		val results = subject.check(file)

		results should containExactly(finding1, finding2)
		verify(subject).check(file)
		verify(subject).visitFile(any(), eq(file))
		verifyNoMoreInteractions(subject, file, content)
	}
}
