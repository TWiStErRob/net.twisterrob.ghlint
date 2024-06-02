package net.twisterrob.ghlint.model

/**
 * https://docs.github.com/en/actions/learn-github-actions/understanding-github-actions#workflows
 */
public interface Workflow : Component, Content {

	public val name: String?
	public val env: Env?
	public val jobs: Map<String, Job>
	public val permissions: Set<Permission>?
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
