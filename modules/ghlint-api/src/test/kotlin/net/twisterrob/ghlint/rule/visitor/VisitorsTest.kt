package net.twisterrob.ghlint.rule.visitor

import io.kotest.matchers.collections.containDuplicates
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldNot
import net.twisterrob.ghlint.model.Action
import net.twisterrob.ghlint.model.ActionStep
import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.InvalidContent
import net.twisterrob.ghlint.model.Job
import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.model.WorkflowStep
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import org.junit.jupiter.api.Test
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.javaMethod

/**
 * Test for all the visitors at once.
 */
class VisitorsTest {

	@Test fun `no same-named methods should exist in all the visitor interfaces`() {
		overriddenFunctions shouldNot containDuplicates()
	}

	@Test fun `every Visitor method is overridden`() {
		val allMethods = allFunctions
		val overriddenMethods = overriddenFunctions
		overriddenMethods shouldContainExactlyInAnyOrder allMethods
	}
}

private val allFunctions: List<String>
	get() = OverrideEverythingVisitorRule::class
		.memberFunctions
		.filterNot(KFunction<*>::isObjectMethod)
		.map { it.name }
		.sorted()

private val overriddenFunctions: List<String>
	get() = OverrideEverythingVisitorRule::class
		.declaredFunctions
		.map { it.name }
		.sorted()

private fun KFunction<*>.isObjectMethod(): Boolean =
	this.javaMethod?.declaringClass == Object::class.java

@Suppress("RedundantOverride")
private class OverrideEverythingVisitorRule
	: VisitorRule, WorkflowVisitor, ActionVisitor, InvalidContentVisitor {

	override val issues: List<Issue> get() = error("not implemented")
	override fun check(file: File): List<Finding> = super.check(file)

	override fun visitWorkflowFile(reporting: Reporting, file: File) {
		super.visitWorkflowFile(reporting, file)
	}

	override fun visitWorkflow(reporting: Reporting, workflow: Workflow) {
		super.visitWorkflow(reporting, workflow)
	}

	override fun visitJob(reporting: Reporting, job: Job) {
		super.visitJob(reporting, job)
	}

	override fun visitNormalJob(reporting: Reporting, job: Job.NormalJob) {
		super.visitNormalJob(reporting, job)
	}

	override fun visitReusableWorkflowCallJob(reporting: Reporting, job: Job.ReusableWorkflowCallJob) {
		super.visitReusableWorkflowCallJob(reporting, job)
	}

	override fun visitWorkflowStep(reporting: Reporting, step: WorkflowStep) {
		super.visitWorkflowStep(reporting, step)
	}

	override fun visitUsesStep(reporting: Reporting, step: WorkflowStep.Uses) {
		super.visitUsesStep(reporting, step)
	}

	override fun visitRunStep(reporting: Reporting, step: WorkflowStep.Run) {
		super.visitRunStep(reporting, step)
	}

	override fun visitActionFile(reporting: Reporting, file: File) {
		super.visitActionFile(reporting, file)
	}

	override fun visitAction(reporting: Reporting, action: Action) {
		super.visitAction(reporting, action)
	}

	override fun visitInput(reporting: Reporting, input: Action.ActionInput) {
		super.visitInput(reporting, input)
	}

	override fun visitOutput(reporting: Reporting, output: Action.ActionOutput) {
		super.visitOutput(reporting, output)
	}

	override fun visitRuns(reporting: Reporting, runs: Action.Runs) {
		super.visitRuns(reporting, runs)
	}

	override fun visitCompositeRuns(reporting: Reporting, runs: Action.Runs.CompositeRuns) {
		super.visitCompositeRuns(reporting, runs)
	}

	override fun visitActionStep(reporting: Reporting, step: ActionStep) {
		super.visitActionStep(reporting, step)
	}

	override fun visitActionUsesStep(reporting: Reporting, step: ActionStep.Uses) {
		super.visitActionUsesStep(reporting, step)
	}

	override fun visitActionRunStep(reporting: Reporting, step: ActionStep.Run) {
		super.visitActionRunStep(reporting, step)
	}

	override fun visitJavascriptRuns(reporting: Reporting, runs: Action.Runs.JavascriptRuns) {
		super.visitJavascriptRuns(reporting, runs)
	}

	override fun visitDockerRuns(reporting: Reporting, runs: Action.Runs.DockerRuns) {
		super.visitDockerRuns(reporting, runs)
	}

	override fun visitBranding(reporting: Reporting, branding: Action.Branding) {
		super.visitBranding(reporting, branding)
	}

	override fun visitInvalidContentFile(reporting: Reporting, file: File) {
		super.visitInvalidContentFile(reporting, file)
	}

	override fun visitInvalidContent(reporting: Reporting, content: InvalidContent) {
		super.visitInvalidContent(reporting, content)
	}
}
