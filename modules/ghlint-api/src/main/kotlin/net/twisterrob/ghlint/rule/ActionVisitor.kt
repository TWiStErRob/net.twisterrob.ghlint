package net.twisterrob.ghlint.rule

import net.twisterrob.ghlint.model.Action
import net.twisterrob.ghlint.model.ActionStep
import net.twisterrob.ghlint.model.File
import javax.annotation.OverridingMethodsMustInvokeSuper

@Suppress("detekt.TooManyFunctions", "detekt.ComplexInterface")
public interface ActionVisitor {

	@OverridingMethodsMustInvokeSuper
	public fun visitActionFile(reporting: Reporting, file: File) {
		visitAction(reporting, file.content as Action)
	}

	@OverridingMethodsMustInvokeSuper
	public fun visitAction(reporting: Reporting, action: Action) {
		visitRuns(reporting, action.runs)
		action.inputs.orEmpty().values.forEach { input ->
			visitInput(reporting, input)
		}
		action.outputs.orEmpty().values.forEach { output ->
			visitOutput(reporting, output)
		}
		action.branding?.let { branding ->
			visitBranding(reporting, branding)
		}
	}

	@OverridingMethodsMustInvokeSuper
	public fun visitInput(reporting: Reporting, input: Action.ActionInput) {
		// No children.
	}

	@OverridingMethodsMustInvokeSuper
	public fun visitOutput(reporting: Reporting, output: Action.ActionOutput) {
		// No children.
	}

	@OverridingMethodsMustInvokeSuper
	public fun visitRuns(reporting: Reporting, runs: Action.Runs) {
		when (runs) {
			is Action.Runs.CompositeRuns -> visitCompositeRuns(reporting, runs)
			is Action.Runs.JavascriptRuns -> visitJavascriptRuns(reporting, runs)
			is Action.Runs.DockerRuns -> visitDockerRuns(reporting, runs)
		}
	}

	@OverridingMethodsMustInvokeSuper
	public fun visitCompositeRuns(reporting: Reporting, runs: Action.Runs.CompositeRuns) {
		runs.steps.forEach { step ->
			visitStep(reporting, step)
		}
	}

	@OverridingMethodsMustInvokeSuper
	public fun visitStep(reporting: Reporting, step: ActionStep) {
		when (step) {
			is ActionStep.Run -> visitRunStep(reporting, step)
			is ActionStep.Uses -> visitUsesStep(reporting, step)
			is ActionStep.BaseStep -> error("Unknown step type: ${step}")
		}
	}

	@OverridingMethodsMustInvokeSuper
	public fun visitUsesStep(reporting: Reporting, step: ActionStep.Uses) {
		// No children.
	}

	@OverridingMethodsMustInvokeSuper
	public fun visitRunStep(reporting: Reporting, step: ActionStep.Run) {
		// No children.
	}

	@OverridingMethodsMustInvokeSuper
	public fun visitJavascriptRuns(reporting: Reporting, runs: Action.Runs.JavascriptRuns) {
		// No children.
	}

	@OverridingMethodsMustInvokeSuper
	public fun visitDockerRuns(reporting: Reporting, runs: Action.Runs.DockerRuns) {
		// No children.
	}

	@OverridingMethodsMustInvokeSuper
	public fun visitBranding(reporting: Reporting, branding: Action.Branding) {
		// No children.
	}
}
