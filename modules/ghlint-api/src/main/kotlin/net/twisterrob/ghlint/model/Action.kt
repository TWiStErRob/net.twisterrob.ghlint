package net.twisterrob.ghlint.model

/**
 * https://docs.github.com/en/actions/learn-github-actions/understanding-github-actions#actions
 * https://docs.github.com/en/actions/creating-actions/metadata-syntax-for-github-actions
 */
public interface Action : Component, Content {

	public val name: String
	public val description: String
	public val author: String?
	public val branding: Branding?
	public val inputs: Map<String, ActionInput>?
	public val outputs: Map<String, ActionOutput>?
	public val runs: Runs

	public interface ActionInput {

		public val parent: Action
		public val id: String
		public val description: String

		@Suppress("detekt.BooleanPropertyNaming") // Keep as schema.
		public val required: Boolean
		public val default: String?
		public val deprecationMessage: String?
	}

	public interface ActionOutput {

		public val parent: Action
		public val id: String
		public val description: String
		public val value: String? // Model as sealed?, only valid for composite.
	}

	public sealed interface Runs {

		public val parent: Action

		public interface CompositeRuns : Runs, Step.Parent {

			public override val steps: List<ActionStep>
		}

		public interface JavascriptRuns : Runs {

			public val using: String
			public val main: String
			public val pre: String?
			public val preIf: String?
			public val post: String?
			public val postIf: String?
		}

		public interface DockerRuns : Runs {

			public val using: String
			public val image: String
			public val entrypoint: String?
			public val preEntrypoint: String?
			public val postEntrypoint: String?
			public val args: List<String>?
			public val env: Map<String, String>?
		}
	}

	public interface Branding {

		public val parent: Action

		public val icon: String?
		public val color: String?
	}

	public companion object
}

public val Action.id: String?
	get() = if (parent.location.path.startsWith("github://")) {
		parent.location.path.removePrefix("github://").substringBeforeLast('/')
	} else {
		null
	}
