package net.twisterrob.ghlint.model

/**
 * https://docs.github.com/en/actions/learn-github-actions/understanding-github-actions#workflows
 * https://docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions
 */
public interface Workflow : Component, Content {

	public val name: String?
	public val env: Env?
	public val jobs: Map<String, Job>
	public val permissions: Permissions?
	public val defaults: Defaults?

	public companion object
}

public val Workflow.id: String
	get() {
		val name = parent.location.name
		return when {
			name.endsWith(".yaml") -> name.removeSuffix(".yaml")
			name.endsWith(".yml") -> name.removeSuffix(".yml")
			else -> name
		}
	}
