package net.twisterrob.ghlint.model

/**
 * https://docs.github.com/en/actions/learn-github-actions/understanding-github-actions#workflows
 */
public interface Action : Component {

	public val parent: File
	public val name: String
	public val description: String
	public val inputs: Map<String, ActionInput>
	public val runs: Runs

	public interface ActionInput {

		public val parent: Action
		public val id: String
		public val description: String
		public val required: Boolean
		public val default: String?
	}

	public interface Runs {

		public val parent: Action
		public val using: String
	}

	public companion object
}

public val Action.id: String?
	get() = if (parent.location.path.startsWith("github://")) {
		parent.location.path.removePrefix("github://").substringBeforeLast('/')
	} else {
		null
	}
