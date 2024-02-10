package net.twisterrob.ghlint.model

/**
 * https://docs.github.com/en/actions/learn-github-actions/understanding-github-actions#workflows
 */
public interface Workflow : Component {

	public val parent: File
	public val name: String?
	public val env: Map<String, String>?
	public val jobs: Map<String, Job>
	public val permissions: Map<String, String>?

	public companion object
}

public val Workflow.id: String
	get() = parent.location.name
