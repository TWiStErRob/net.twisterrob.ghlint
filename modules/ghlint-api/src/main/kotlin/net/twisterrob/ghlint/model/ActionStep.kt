package net.twisterrob.ghlint.model

/**
 * https://docs.github.com/en/actions/learn-github-actions/understanding-github-actions#actions
 */
public sealed interface ActionStep : Component {

	public val parent: Action.Runs.CompositeRuns
	public val index: Index
	public val name: String?
	public val id: String?

	@Suppress("detekt.VariableNaming")
	public val `if`: String?
	public val env: Map<String, String>?

	public companion object;

	@JvmInline
	public value class Index(public val value: Int)

	// TODO find a way to remove this from the API.
	public interface BaseStep : ActionStep

	public interface Run : BaseStep {

		@Suppress("detekt.MemberNameEqualsClassName")
		public val run: String
		public val shell: String
		public val workingDirectory: String?

		public companion object
	}

	public interface Uses : BaseStep {

		@Suppress("detekt.MemberNameEqualsClassName")
		public val uses: Step.UsesAction
		public val with: Map<String, String>?

		public companion object
	}
}
