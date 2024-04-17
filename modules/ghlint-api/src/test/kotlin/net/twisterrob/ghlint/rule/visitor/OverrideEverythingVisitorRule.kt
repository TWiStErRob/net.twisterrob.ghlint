package net.twisterrob.ghlint.rule.visitor

import net.twisterrob.ghlint.model.Action
import net.twisterrob.ghlint.model.ActionStep
import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.InvalidContent
import net.twisterrob.ghlint.model.Job
import net.twisterrob.ghlint.model.Step
import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import org.mockito.kotlin.verify

class OverrideEverythingVisitorRule : VisitorRule, WorkflowVisitor, ActionVisitor, InvalidContentVisitor {

	override val issues: List<Issue>
		get() = error("not implemented")

	override fun check(file: File): List<Finding> {
		verify(this).check(file)
		return super.check(file)
	}

	override fun visitWorkflowFile(reporting: Reporting, file: File) {
		verify(this).visitWorkflowFile(reporting, file)
		super.visitWorkflowFile(reporting, file)
	}

	override fun visitWorkflow(reporting: Reporting, workflow: Workflow) {
		verify(this).visitWorkflow(reporting, workflow)
		super.visitWorkflow(reporting, workflow)
	}

	override fun visitJob(reporting: Reporting, job: Job) {
		verify(this).visitJob(reporting, job)
		super.visitJob(reporting, job)
	}

	override fun visitNormalJob(reporting: Reporting, job: Job.NormalJob) {
		verify(this).visitNormalJob(reporting, job)
		super.visitNormalJob(reporting, job)
	}

	override fun visitReusableWorkflowCallJob(reporting: Reporting, job: Job.ReusableWorkflowCallJob) {
		verify(this).visitReusableWorkflowCallJob(reporting, job)
		super.visitReusableWorkflowCallJob(reporting, job)
	}

	override fun visitStep(reporting: Reporting, step: Step) {
		verify(this).visitStep(reporting, step)
		super.visitStep(reporting, step)
	}

	override fun visitUsesStep(reporting: Reporting, step: Step.Uses) {
		verify(this).visitUsesStep(reporting, step)
		super.visitUsesStep(reporting, step)
	}

	override fun visitRunStep(reporting: Reporting, step: Step.Run) {
		verify(this).visitRunStep(reporting, step)
		super.visitRunStep(reporting, step)
	}

	override fun visitActionFile(reporting: Reporting, file: File) {
		verify(this).visitActionFile(reporting, file)
		super.visitActionFile(reporting, file)
	}

	override fun visitAction(reporting: Reporting, action: Action) {
		verify(this).visitAction(reporting, action)
		super.visitAction(reporting, action)
	}

	override fun visitInput(reporting: Reporting, input: Action.ActionInput) {
		verify(this).visitInput(reporting, input)
		super.visitInput(reporting, input)
	}

	override fun visitOutput(reporting: Reporting, output: Action.ActionOutput) {
		verify(this).visitOutput(reporting, output)
		super.visitOutput(reporting, output)
	}

	override fun visitRuns(reporting: Reporting, runs: Action.Runs) {
		verify(this).visitRuns(reporting, runs)
		super.visitRuns(reporting, runs)
	}

	override fun visitCompositeRuns(reporting: Reporting, runs: Action.Runs.CompositeRuns) {
		verify(this).visitCompositeRuns(reporting, runs)
		super.visitCompositeRuns(reporting, runs)
	}

	override fun visitActionStep(reporting: Reporting, step: ActionStep) {
		verify(this).visitActionStep(reporting, step)
		super.visitActionStep(reporting, step)
	}

	override fun visitActionUsesStep(reporting: Reporting, step: ActionStep.Uses) {
		verify(this).visitActionUsesStep(reporting, step)
		super.visitActionUsesStep(reporting, step)
	}

	override fun visitActionRunStep(reporting: Reporting, step: ActionStep.Run) {
		verify(this).visitActionRunStep(reporting, step)
		super.visitActionRunStep(reporting, step)
	}

	override fun visitJavascriptRuns(reporting: Reporting, runs: Action.Runs.JavascriptRuns) {
		verify(this).visitJavascriptRuns(reporting, runs)
		super.visitJavascriptRuns(reporting, runs)
	}

	override fun visitDockerRuns(reporting: Reporting, runs: Action.Runs.DockerRuns) {
		verify(this).visitDockerRuns(reporting, runs)
		super.visitDockerRuns(reporting, runs)
	}

	override fun visitBranding(reporting: Reporting, branding: Action.Branding) {
		verify(this).visitBranding(reporting, branding)
		super.visitBranding(reporting, branding)
	}

	override fun visitInvalidContentFile(reporting: Reporting, file: File) {
		verify(this).visitInvalidContentFile(reporting, file)
		super.visitInvalidContentFile(reporting, file)
	}

	override fun visitInvalidContent(reporting: Reporting, content: InvalidContent) {
		verify(this).visitInvalidContent(reporting, content)
		super.visitInvalidContent(reporting, content)
	}
}
