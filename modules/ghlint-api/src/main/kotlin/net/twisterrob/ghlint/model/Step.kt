package net.twisterrob.ghlint.model

/**
 * Represents a step in a GitHub Actions Workflow or Action.
 *
 * Common step properties for
 *  * [Workflow `jobs.jobs_id.steps.*`](https://docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions#jobsjob_idsteps)
 *  * [Action `runs.steps.*`](https://docs.github.com/en/actions/creating-actions/metadata-syntax-for-github-actions#runssteps)
 */
public sealed interface Step {

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
	public interface BaseStep : Step {

		public companion object
	}

	public interface Run : BaseStep {

		@Suppress("detekt.MemberNameEqualsClassName")
		public val run: String
		public val shell: String?
		public val workingDirectory: String?

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
	public interface UsesAction { // TODO docker? local?

		public val uses: String
		public val versionComment: String?

		public val action: String
		public val owner: String
		public val repository: String
		public val path: String?
		public val ref: String
	}
}
