package net.twisterrob.ghlint.rule.visitor

import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.collections.containExactly
import io.kotest.matchers.should
import io.kotest.matchers.throwable.shouldHaveMessage
import net.twisterrob.ghlint.model.Action
import net.twisterrob.ghlint.model.Content
import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.InvalidContent
import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Answers
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.eq
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

class VisitorRuleTest {

	@Test fun `check fails when no visitor interface implemented`() {
		val subject = spy(object : VisitorRule {
			override val issues: List<Issue> = emptyList()
		})
		val file: File = mock()
		val content: Workflow = mock()
		whenever(file.content).thenReturn(content)

		val ex = assertThrows<IllegalStateException> { subject.check(file) }

		ex shouldHaveMessage "A VisitorRule must also implement at least one of " +
				"WorkflowVisitor, ActionVisitor, InvalidContentVisitor visitors."
		verify(subject).check(file)
		verifyNoMoreInteractions(subject, file, content)
	}

	@Test fun `check calls visitWorkflowFile for reporting workflow content`() {
		val subject = spy(WorkflowRule())
		val file: File = mock()
		val content: Workflow = mock()
		whenever(file.content).thenReturn(content)
		doNothing().whenever(subject).visitWorkflowFile(any(), eq(file))

		val results = subject.check(file)

		results should beEmpty()
		verify(subject).check(file)
		verify(subject).visitWorkflowFile(any(), eq(file))
		verify(file).content
		verifyNoMoreInteractions(subject, file, content)
	}

	@ValueSource(
		strings = [
			"Action",
			"InvalidContent",
		]
	)
	@ParameterizedTest
	fun `check calls nothing when unhandled content is passed to workflow visitor`(unhandled: String) {
		val subject = spy(WorkflowRule())
		val file: File = mock()
		val content: Content = createContent(unhandled)
		whenever(file.content).thenReturn(content)

		val results = subject.check(file)

		results should beEmpty()
		verify(subject).check(file)
		verify(file).content
		verifyNoMoreInteractions(subject, file, content)
	}

	@Test fun `check calls visitActionFile for reporting action content`() {
		val subject = spy(ActionRule())
		val file: File = mock()
		val content: Action = mock()
		whenever(file.content).thenReturn(content)
		doNothing().whenever(subject).visitActionFile(any(), eq(file))

		val results = subject.check(file)

		results should beEmpty()
		verify(subject).check(file)
		verify(subject).visitActionFile(any(), eq(file))
		verify(file).content
		verifyNoMoreInteractions(subject, file, content)
	}

	@ValueSource(
		strings = [
			"Workflow",
			"InvalidContent",
		]
	)
	@ParameterizedTest
	fun `check calls nothing when unhandled content is passed to action visitor`(unhandled: String) {
		val subject = spy(ActionRule())
		val file: File = mock()
		val content: Content = createContent(unhandled)
		whenever(file.content).thenReturn(content)

		val results = subject.check(file)

		results should beEmpty()
		verify(subject).check(file)
		verify(file).content
		verifyNoMoreInteractions(subject, file, content)
	}

	@Test fun `check calls visitInvalidContentFile for any() action content`() {
		val subject = spy(InvalidContentRule())
		val file: File = mock()
		val content: InvalidContent = mock()
		whenever(file.content).thenReturn(content)
		doNothing().whenever(subject).visitInvalidContentFile(any(), eq(file))

		val results = subject.check(file)

		results should beEmpty()
		verify(subject).check(file)
		verify(subject).visitInvalidContentFile(any(), eq(file))
		verify(file).content
		verifyNoMoreInteractions(subject, file, content)
	}

	@ValueSource(
		strings = [
			"Workflow",
			"Action",
		]
	)
	@ParameterizedTest
	fun `check calls nothing when unhandled content is passed to invalid visitor`(unhandled: String) {
		val subject = spy(InvalidContentRule())
		val file: File = mock()
		val content: Content = createContent(unhandled)
		whenever(file.content).thenReturn(content)

		val results = subject.check(file)

		results should beEmpty()
		verify(subject).check(file)
		verify(file).content
		verifyNoMoreInteractions(subject, file, content)
	}

	@Test fun `check collects findings for workflows`() {
		val subject = spy(AllVisitorsRule())
		val file: File = mock()
		val content: Workflow = mock()
		whenever(file.content).thenReturn(content)
		val finding: Finding = mock(Answers.RETURNS_MOCKS)
		doAnswer { invocation ->
			invocation.getArgument<Reporting>(0).report(finding)
		}.whenever(subject).visitWorkflowFile(any(), eq(file))

		val results = subject.check(file)

		results should containExactly(finding)
		verify(subject).check(file)
		verify(file).content
		verify(subject).visitWorkflowFile(any(), eq(file))
		verifyNoMoreInteractions(subject, file, content)
	}

	@Test fun `duplicate findings are reported for actions`() {
		val subject = spy(AllVisitorsRule())
		val file: File = mock()
		val content: Action = mock()
		whenever(file.content).thenReturn(content)
		val finding: Finding = mock(Answers.RETURNS_MOCKS)
		doAnswer { invocation ->
			invocation.getArgument<Reporting>(0).report(finding)
			invocation.getArgument<Reporting>(0).report(finding)
		}.whenever(subject).visitActionFile(any(), eq(file))

		val results = subject.check(file)

		results should containExactly(finding, finding)
		verify(subject).check(file)
		verify(file).content
		verify(subject).visitActionFile(any(), eq(file))
		verifyNoMoreInteractions(subject, file, content)
	}

	@Test fun `multiple findings are reported for invalid`() {
		val subject = spy(AllVisitorsRule())
		val file: File = mock()
		val content: InvalidContent = mock()
		whenever(file.content).thenReturn(content)
		val finding1: Finding = mock(Answers.RETURNS_MOCKS)
		val finding2: Finding = mock(Answers.RETURNS_MOCKS)
		doAnswer { invocation ->
			invocation.getArgument<Reporting>(0).report(finding1)
			invocation.getArgument<Reporting>(0).report(finding2)
		}.whenever(subject).visitInvalidContentFile(any(), eq(file))

		val results = subject.check(file)

		results should containExactly(finding1, finding2)
		verify(subject).check(file)
		verify(file).content
		verify(subject).visitInvalidContentFile(any(), eq(file))
		verifyNoMoreInteractions(subject, file, content)
	}

	private fun createContent(unhandled: String): Content {
		val className = Content::class.java.packageName + ".${unhandled}"

		@Suppress("UNCHECKED_CAST")
		val clazz = Class.forName(className) as? Class<Content>
			?: error("Unknown content type: ${unhandled}")

		return mock(clazz)
	}

	private class WorkflowRule : VisitorRule, WorkflowVisitor {

		override val issues: List<Issue> = emptyList()
	}

	private class ActionRule : VisitorRule, ActionVisitor {

		override val issues: List<Issue> = emptyList()
	}

	private class InvalidContentRule : VisitorRule, InvalidContentVisitor {

		override val issues: List<Issue> = emptyList()
	}

	private class AllVisitorsRule : VisitorRule, WorkflowVisitor, ActionVisitor, InvalidContentVisitor {

		override val issues: List<Issue> = emptyList()
	}
}
