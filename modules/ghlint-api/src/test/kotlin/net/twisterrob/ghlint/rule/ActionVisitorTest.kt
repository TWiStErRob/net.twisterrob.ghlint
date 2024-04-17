package net.twisterrob.ghlint.rule

import net.twisterrob.ghlint.model.Action
import net.twisterrob.ghlint.model.ActionStep
import net.twisterrob.ghlint.model.File
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

class ActionVisitorTest {

	private val subject: ActionVisitor = spy(object : ActionVisitor {})
	private val reporting: Reporting = mock()

	@Test fun `visitActionFile delegates actions`() {
		val target: File = mock()
		val child: Action = mock()
		whenever(target.content).thenReturn(child)
		doNothing().whenever(subject).visitAction(reporting, child)

		subject.visitActionFile(reporting, target)

		verify(target).content
		verify(subject).visitActionFile(reporting, target)
		verify(subject).visitAction(reporting, child)
		verifyNoMoreInteractions(subject, reporting, target, child)
	}

	@Test fun `visitAction delegates runs`() {
		val target: Action = mock()
		val child: Action.Runs = mock<Action.Runs.CompositeRuns>()
		whenever(target.runs).thenReturn(child)
		doNothing().whenever(subject).visitRuns(reporting, child)

		subject.visitAction(reporting, target)

		verify(target).runs
		verify(subject).visitAction(reporting, target)
		verify(subject).visitRuns(reporting, child)
		verifyNoMoreInteractions(subject, reporting/*, target*/, child)
	}

	@Test fun `visitAction skips branding`() {
		val target: Action = mock()
		whenever(target.branding).thenReturn(null)
		val runs = mock<Action.Runs.CompositeRuns>()
		whenever(target.runs).thenReturn(runs)
		doNothing().whenever(subject).visitRuns(reporting, runs)

		subject.visitAction(reporting, target)

		verify(target).runs
		verify(subject).visitAction(reporting, target)
		verify(subject).visitRuns(reporting, runs)
		verifyNoMoreInteractions(subject, reporting/*, target*/)
	}

	@Test fun `visitAction delegates branding`() {
		val target: Action = mock()
		val child: Action.Branding = mock()
		whenever(target.branding).thenReturn(child)
		doNothing().whenever(subject).visitBranding(reporting, child)
		val runs = mock<Action.Runs.CompositeRuns>()
		whenever(target.runs).thenReturn(runs)
		doNothing().whenever(subject).visitRuns(reporting, runs)

		subject.visitAction(reporting, target)

		verify(target).runs
		verify(subject).visitAction(reporting, target)
		verify(subject).visitBranding(reporting, child)
		verify(subject).visitRuns(reporting, runs)
		verifyNoMoreInteractions(subject, reporting/*, target*/, child)
	}

	@Test fun `visitActions skips inputs`() {
		val target: Action = mock()
		whenever(target.inputs).thenReturn(null)
		val runs = mock<Action.Runs.CompositeRuns>()
		whenever(target.runs).thenReturn(runs)
		doNothing().whenever(subject).visitRuns(reporting, runs)

		subject.visitAction(reporting, target)

		verify(target).inputs
		verify(subject).visitAction(reporting, target)
		verify(subject).visitRuns(reporting, runs)
		verifyNoMoreInteractions(subject, reporting/*, target*/)
	}

	@Test fun `visitActions delegates inputs`() {
		val target: Action = mock()
		val child1: Action.ActionInput = mock()
		val child2: Action.ActionInput = mock()
		val runs = mock<Action.Runs.CompositeRuns>()
		whenever(target.runs).thenReturn(runs)
		doNothing().whenever(subject).visitRuns(reporting, runs)
		whenever(target.inputs).thenReturn(mapOf("child1" to child1, "child2" to child2))
		doNothing().whenever(subject).visitInput(reporting, child1)
		doNothing().whenever(subject).visitInput(reporting, child2)

		subject.visitAction(reporting, target)

		verify(target).inputs
		verify(subject).visitAction(reporting, target)
		verify(subject).visitInput(reporting, child1)
		verify(subject).visitInput(reporting, child2)
		verify(subject).visitRuns(reporting, runs)
		verifyNoMoreInteractions(subject, reporting/*, target*/, child1, child2)
	}

	@Test fun `visitInput does nothing`() {
		val target: Action.ActionInput = mock()

		subject.visitInput(reporting, target)

		verify(subject).visitInput(reporting, target)
		verifyNoMoreInteractions(subject, reporting, target)
	}

	@Test fun `visitActions skips outputs`() {
		val target: Action = mock()
		whenever(target.outputs).thenReturn(null)
		val runs = mock<Action.Runs.CompositeRuns>()
		whenever(target.runs).thenReturn(runs)
		doNothing().whenever(subject).visitRuns(reporting, runs)

		subject.visitAction(reporting, target)

		verify(target).outputs
		verify(subject).visitAction(reporting, target)
		verify(subject).visitRuns(reporting, runs)
		verifyNoMoreInteractions(subject, reporting/*, target*/)
	}

	@Test fun `visitActions delegates outputs`() {
		val target: Action = mock()
		val child1: Action.ActionOutput = mock()
		val child2: Action.ActionOutput = mock()
		val runs = mock<Action.Runs.CompositeRuns>()
		whenever(target.runs).thenReturn(runs)
		doNothing().whenever(subject).visitRuns(reporting, runs)
		doNothing().whenever(subject).visitRuns(reporting, runs)
		whenever(target.outputs).thenReturn(mapOf("child1" to child1, "child2" to child2))
		doNothing().whenever(subject).visitOutput(reporting, child1)
		doNothing().whenever(subject).visitOutput(reporting, child2)

		subject.visitAction(reporting, target)

		verify(target).outputs
		verify(subject).visitAction(reporting, target)
		verify(subject).visitOutput(reporting, child1)
		verify(subject).visitOutput(reporting, child2)
		verify(subject).visitRuns(reporting, runs)
		verifyNoMoreInteractions(subject, reporting/*, target*/, child1, child2)
	}

	@Test fun `visitOutput does nothing`() {
		val target: Action.ActionOutput = mock()

		subject.visitOutput(reporting, target)

		verify(subject).visitOutput(reporting, target)
		verifyNoMoreInteractions(subject, reporting, target)
	}

	@Test fun `visitStep fails for base step`() {
		val target: ActionStep.BaseStep = mock()

		assertThrows<IllegalStateException> {
			subject.visitStep(reporting, target)
		}

		verify(subject).visitStep(reporting, target)
		verifyNoMoreInteractions(subject, reporting, target)
	}

	@Test fun `visitUsesStep does nothing`() {
		val target: ActionStep.Uses = mock()

		subject.visitUsesStep(reporting, target)

		verify(subject).visitUsesStep(reporting, target)
		verifyNoMoreInteractions(subject, reporting, target)
	}

	@Test fun `visitUsesStep delegates for run steps`() {
		val target: ActionStep.Uses = mock()
		doNothing().whenever(subject).visitUsesStep(reporting, target)

		subject.visitStep(reporting, target)

		verify(subject).visitStep(reporting, target)
		verify(subject).visitUsesStep(reporting, target)
		verifyNoMoreInteractions(subject, reporting, target)
	}

	@Test fun `visitRunStep does nothing`() {
		val target: ActionStep.Run = mock()

		subject.visitRunStep(reporting, target)

		verify(subject).visitRunStep(reporting, target)
		verifyNoMoreInteractions(subject, reporting, target)
	}

	@Test fun `visitStep delegates for run steps`() {
		val target: ActionStep.Run = mock()
		doNothing().whenever(subject).visitRunStep(reporting, target)

		subject.visitStep(reporting, target)

		verify(subject).visitStep(reporting, target)
		verify(subject).visitRunStep(reporting, target)
		verifyNoMoreInteractions(subject, reporting, target)
	}

	@Test fun `visitCompositeRuns delegates steps`() {
		val target: Action.Runs.CompositeRuns = mock()
		val child1: ActionStep = mock<ActionStep.BaseStep>()
		val child2: ActionStep = mock<ActionStep.BaseStep>()
		whenever(target.steps).thenReturn(listOf(child1, child2))
		doNothing().whenever(subject).visitStep(reporting, child1)
		doNothing().whenever(subject).visitStep(reporting, child2)

		subject.visitCompositeRuns(reporting, target)

		verify(target).steps
		verify(subject).visitCompositeRuns(reporting, target)
		verify(subject).visitStep(reporting, child1)
		verify(subject).visitStep(reporting, child2)
		verifyNoMoreInteractions(subject, reporting, target, child1, child2)
	}

	@Test fun `visitRuns delegates for Composite runs`() {
		val target: Action.Runs.CompositeRuns = mock()
		doNothing().whenever(subject).visitCompositeRuns(reporting, target)

		subject.visitRuns(reporting, target)

		verify(subject).visitRuns(reporting, target)
		verify(subject).visitCompositeRuns(reporting, target)
		verifyNoMoreInteractions(subject, reporting, target)
	}

	@Test fun `visitJavascriptRuns does nothing`() {
		val target: Action.Runs.JavascriptRuns = mock()

		subject.visitJavascriptRuns(reporting, target)

		verify(subject).visitJavascriptRuns(reporting, target)
		verifyNoMoreInteractions(subject, reporting, target)
	}

	@Test fun `visitRuns delegates for Javascript runs`() {
		val target: Action.Runs.JavascriptRuns = mock()
		doNothing().whenever(subject).visitJavascriptRuns(reporting, target)

		subject.visitRuns(reporting, target)

		verify(subject).visitRuns(reporting, target)
		verify(subject).visitJavascriptRuns(reporting, target)
		verifyNoMoreInteractions(subject, reporting, target)
	}

	@Test fun `visitDockerRuns does nothing`() {
		val target: Action.Runs.DockerRuns = mock()

		subject.visitDockerRuns(reporting, target)

		verify(subject).visitDockerRuns(reporting, target)
		verifyNoMoreInteractions(subject, reporting, target)
	}

	@Test fun `visitRuns delegates for Docker runs`() {
		val target: Action.Runs.DockerRuns = mock()
		doNothing().whenever(subject).visitDockerRuns(reporting, target)

		subject.visitRuns(reporting, target)

		verify(subject).visitRuns(reporting, target)
		verify(subject).visitDockerRuns(reporting, target)
		verifyNoMoreInteractions(subject, reporting, target)
	}

	@Test fun `visitBrandingStep does nothing`() {
		val target: Action.Branding = mock()

		subject.visitBranding(reporting, target)

		verify(subject).visitBranding(reporting, target)
		verifyNoMoreInteractions(subject, reporting, target)
	}
}
