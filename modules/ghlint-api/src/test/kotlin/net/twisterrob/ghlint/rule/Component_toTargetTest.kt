package net.twisterrob.ghlint.rule

import io.kotest.matchers.shouldBe
import net.twisterrob.ghlint.model.Action
import net.twisterrob.ghlint.model.ActionStep
import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.FileLocation
import net.twisterrob.ghlint.model.Job
import net.twisterrob.ghlint.model.Step
import net.twisterrob.ghlint.model.Workflow
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

@Suppress("detekt.ClassNaming")
class Component_toTargetTest {

	@Test fun `workflow as target`() {
		val parent: File = mock()
		whenever(parent.location).thenReturn(FileLocation("workflow-id.yml"))
		val component: Workflow = mock()
		whenever(component.parent).thenReturn(parent)

		val result = component.toTarget()

		result shouldBe """Workflow[workflow-id]"""
	}

	@Test fun `job as target`() {
		val component: Job = mock<Job.BaseJob>()
		whenever(component.id).thenReturn("job-id")

		val result = component.toTarget()

		result shouldBe """Job[job-id]"""
	}

	@Test fun `action with name as target`() {
		val component: Action = actionWithName("Action Name")

		val result = component.toTarget()

		result shouldBe """Action[Action Name]"""
	}

	@Test fun `action with id as target`() {
		val parent: File = mock()
		whenever(parent.location).thenReturn(FileLocation("github://action/name/action.yml"))
		val component: Action = mock()
		whenever(component.parent).thenReturn(parent)

		val result = component.toTarget()

		result shouldBe """Action[action/name]"""
	}

	@Test fun `step with id as target`() {
		val parent: Job.NormalJob = mock()
		whenever(parent.id).thenReturn("job-id")
		val component: Step = mock<Step.BaseStep>()
		whenever(component.parent).thenReturn(parent)
		whenever(component.id).thenReturn("step-id")

		val result = component.toTarget()

		result shouldBe """Step[step-id] in Job[job-id]"""
	}

	@Test fun `step with name as target`() {
		val parent: Job.NormalJob = mock()
		whenever(parent.id).thenReturn("job-id")
		val component: Step = mock<Step.BaseStep>()
		whenever(component.parent).thenReturn(parent)
		whenever(component.name).thenReturn("Step Name")

		val result = component.toTarget()

		result shouldBe """Step["Step Name"] in Job[job-id]"""
	}

	@Test fun `step with uses as target`() {
		val parent: Job.NormalJob = mock()
		whenever(parent.id).thenReturn("job-id")
		val component: Step.Uses = mock()
		whenever(component.parent).thenReturn(parent)
		val uses: Step.UsesAction = mock()
		whenever(uses.uses).thenReturn("action/name")
		whenever(component.uses).thenReturn(uses)

		val result = component.toTarget()

		result shouldBe """Step[action/name] in Job[job-id]"""
	}

	@Test fun `step with index as target`() {
		val parent: Job.NormalJob = mock()
		whenever(parent.id).thenReturn("job-id")
		val component: Step = mock<Step.BaseStep>()
		whenever(component.parent).thenReturn(parent)
		whenever(component.index).thenReturn(Step.Index(42))

		val result = component.toTarget()

		result shouldBe """Step[#42] in Job[job-id]"""
	}

	@Test fun `action step with id as target`() {
		val action = actionWithName("Action Name")
		val parent: Action.Runs.CompositeRuns = mock()
		whenever(parent.parent).thenReturn(action)
		val component: ActionStep = mock<ActionStep.BaseStep>()
		whenever(component.parent).thenReturn(parent)
		whenever(component.id).thenReturn("step-id")

		val result = component.toTarget()

		result shouldBe """Step[step-id] in Action[Action Name]"""
	}

	@Test fun `action step with name as target`() {
		val action = actionWithName("Action Name")
		val parent: Action.Runs.CompositeRuns = mock()
		whenever(parent.parent).thenReturn(action)
		val component: ActionStep = mock<ActionStep.BaseStep>()
		whenever(component.parent).thenReturn(parent)
		whenever(component.name).thenReturn("Step Name")

		val result = component.toTarget()

		result shouldBe """Step["Step Name"] in Action[Action Name]"""
	}

	@Test fun `action step with uses as target`() {
		val action = actionWithName("Action Name")
		val parent: Action.Runs.CompositeRuns = mock()
		whenever(parent.parent).thenReturn(action)
		val component: ActionStep.Uses = mock()
		whenever(component.parent).thenReturn(parent)
		val uses: Step.UsesAction = mock()
		whenever(uses.uses).thenReturn("action/name")
		whenever(component.uses).thenReturn(uses)

		val result = component.toTarget()

		result shouldBe """Step[action/name] in Action[Action Name]"""
	}

	@Test fun `action step with index as target`() {
		val action = actionWithName("Action Name")
		val parent: Action.Runs.CompositeRuns = mock()
		whenever(parent.parent).thenReturn(action)
		val component: ActionStep = mock<ActionStep.BaseStep>()
		whenever(component.parent).thenReturn(parent)
		whenever(component.index).thenReturn(ActionStep.Index(42))

		val result = component.toTarget()

		result shouldBe """Step[#42] in Action[Action Name]"""
	}

	private fun actionWithName(name: String): Action {
		val parent: File = mock()
		whenever(parent.location).thenReturn(FileLocation("action.yml"))
		val component: Action = mock()
		whenever(component.parent).thenReturn(parent)
		whenever(component.name).thenReturn(name)
		return component
	}
}
