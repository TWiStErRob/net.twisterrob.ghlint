package net.twisterrob.ghlint.model

/**
 * https://docs.github.com/en/actions/learn-github-actions/understanding-github-actions#actions
 */
public sealed interface Step : Component {

	public val parent: Job.NormalJob
	public val index: Index
	public val name: String?
	public val id: String?

	@Suppress("detekt.VariableNaming")
	public val `if`: String?

	public companion object;

	@JvmInline
	public value class Index(public val value: Int)

	// TODO find a way to remove this from the API.
	public interface BaseStep : Step

	public interface Run : BaseStep {

		@Suppress("detekt.MemberNameEqualsClassName")
		public val run: String
		public val shell: String?
		public val env: Map<String, String>?

		public companion object
	}

	public interface Uses : BaseStep {

		@Suppress("detekt.MemberNameEqualsClassName")
		public val uses: UsesAction
		public val with: Map<String, String>?

		public companion object
	}

	/**
	 * ```
	 * uses ::= <action>@<ref> # <versionComment>
	 * <action> ::= <owner>/<repository>(/<path>)?
	 * ```
	 */
	public interface UsesAction {

		public val raw: String
		public val versionComment: String?

		public val action: Action

		public val actionName: String
		public val owner: String
		public val repository: String
		public val path: String?
		public val ref: String
	}
}
