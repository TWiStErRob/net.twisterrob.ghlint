package net.twisterrob.ghlint.rule

import net.twisterrob.ghlint.model.Action
import net.twisterrob.ghlint.model.File
import javax.annotation.OverridingMethodsMustInvokeSuper

public interface ActionVisitor {

	@OverridingMethodsMustInvokeSuper
	public fun visitFile(reporting: Reporting, file: File) {
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
			TODO("visitStep(reporting, $step)")
		}
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
