package net.twisterrob.ghlint.rule.visitor

import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.Job
import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.model.WorkflowStep
import net.twisterrob.ghlint.rule.Reporting
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

class WorkflowVisitorTest {

	private val subject: WorkflowVisitor = spy(object : WorkflowVisitor {})
	private val reporting: Reporting = mock()

	@Test fun `visitWorkflowFile delegates workflow`() {
		val target: File = mock()
		val child: Workflow = mock()
		whenever(target.content).thenReturn(child)
		doNothing().whenever(subject).visitWorkflow(reporting, child)

		subject.visitWorkflowFile(reporting, target)

		verify(target).content
		verify(subject).visitWorkflowFile(reporting, target)
		verify(subject).visitWorkflow(reporting, child)
		verifyNoMoreInteractions(subject, reporting, target, child)
	}

	@Test fun `visitWorkflow delegates jobs`() {
		val target: Workflow = mock()
		val child1: Job = mock<Job.BaseJob>()
		val child2: Job = mock<Job.BaseJob>()
		whenever(target.jobs).thenReturn(mapOf("child1" to child1, "child2" to child2))
		doNothing().whenever(subject).visitJob(reporting, child1)
		doNothing().whenever(subject).visitJob(reporting, child2)

		subject.visitWorkflow(reporting, target)

		verify(target).jobs
		verify(subject).visitWorkflow(reporting, target)
		verify(subject).visitJob(reporting, child1)
		verify(subject).visitJob(reporting, child2)
		verifyNoMoreInteractions(subject, reporting, target, child1, child2)
	}

	@Test fun `visitJob fails for base jobs`() {
		val target: Job.BaseJob = mock()

		assertThrows<IllegalStateException> {
			subject.visitJob(reporting, target)
		}

		verify(subject).visitJob(reporting, target)
		verifyNoMoreInteractions(subject, reporting, target)
	}

	@Test fun `visitJob delegates for normal jobs`() {
		val target: Job.NormalJob = mock()
		doNothing().whenever(subject).visitNormalJob(reporting, target)

		subject.visitJob(reporting, target)

		verify(subject).visitJob(reporting, target)
		verify(subject).visitNormalJob(reporting, target)
		verifyNoMoreInteractions(subject, reporting, target)
	}

	@Test fun `visitNormalJob delegates steps`() {
		val target: Job.NormalJob = mock()
		val child1: WorkflowStep = mock<WorkflowStep.BaseStep>()
		val child2: WorkflowStep = mock<WorkflowStep.BaseStep>()
		whenever(target.steps).thenReturn(listOf(child1, child2))
		doNothing().whenever(subject).visitWorkflowStep(reporting, child1)
		doNothing().whenever(subject).visitWorkflowStep(reporting, child2)

		subject.visitNormalJob(reporting, target)

		verify(target).steps
		verify(subject).visitNormalJob(reporting, target)
		verify(subject).visitWorkflowStep(reporting, child1)
		verify(subject).visitWorkflowStep(reporting, child2)
		verifyNoMoreInteractions(subject, reporting, target, child1, child2)
	}

	@Test fun `visitJob delegates for reusable workflow calls`() {
		val target: Job.ReusableWorkflowCallJob = mock()
		doNothing().whenever(subject).visitReusableWorkflowCallJob(reporting, target)

		subject.visitJob(reporting, target)

		verify(subject).visitJob(reporting, target)
		verify(subject).visitReusableWorkflowCallJob(reporting, target)
		verifyNoMoreInteractions(subject, reporting, target)
	}

	@Test fun `visitReusableWorkflowCallJob does nothing`() {
		val target: Job.ReusableWorkflowCallJob = mock()

		subject.visitReusableWorkflowCallJob(reporting, target)

		verify(subject).visitReusableWorkflowCallJob(reporting, target)
		verifyNoMoreInteractions(subject, reporting, target)
	}

	@Test fun `visitWorkflowStep fails for base step`() {
		val target: WorkflowStep.BaseStep = mock()

		assertThrows<IllegalStateException> {
			subject.visitWorkflowStep(reporting, target)
		}

		verify(subject).visitWorkflowStep(reporting, target)
		verifyNoMoreInteractions(subject, reporting, target)
	}

	@Test fun `visitRunStep does nothing`() {
		val target: WorkflowStep.Run = mock()

		subject.visitRunStep(reporting, target)

		verify(subject).visitRunStep(reporting, target)
		verifyNoMoreInteractions(subject, reporting, target)
	}

	@Test fun `visitWorkflowStep delegates for run steps`() {
		val target: WorkflowStep.Run = mock()
		doNothing().whenever(subject).visitRunStep(reporting, target)

		subject.visitWorkflowStep(reporting, target)

		verify(subject).visitWorkflowStep(reporting, target)
		verify(subject).visitRunStep(reporting, target)
		verifyNoMoreInteractions(subject, reporting, target)
	}

	@Test fun `visitUsesStep does nothing`() {
		val target: WorkflowStep.Uses = mock()

		subject.visitUsesStep(reporting, target)

		verify(subject).visitUsesStep(reporting, target)
		verifyNoMoreInteractions(subject, reporting, target)
	}

	@Test fun `visitWorkflowStep delegates for uses steps`() {
		val target: WorkflowStep.Uses = mock()
		doNothing().whenever(subject).visitUsesStep(reporting, target)

		subject.visitWorkflowStep(reporting, target)

		verify(subject).visitWorkflowStep(reporting, target)
		verify(subject).visitUsesStep(reporting, target)
		verifyNoMoreInteractions(subject, reporting, target)
	}
}
