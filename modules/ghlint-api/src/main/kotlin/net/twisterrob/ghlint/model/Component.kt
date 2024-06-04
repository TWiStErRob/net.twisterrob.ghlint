package net.twisterrob.ghlint.model

/**
 * https://docs.github.com/en/actions/learn-github-actions/understanding-github-actions#the-components-of-github-actions
 */
public sealed interface Component : Element {

	public companion object
}

public val Component.file: File
	get() = when (this) {
		is Workflow -> parent
		is Job -> parent.parent
		is WorkflowStep -> parent.parent.parent
		is Action -> parent
		is ActionStep -> parent.parent.parent
	}
