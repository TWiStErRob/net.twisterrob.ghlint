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

	public interface BaseStep : Step

	public interface Run : Step {

		@Suppress("detekt.MemberNameEqualsClassName")
		public val run: String
		public val shell: String?
		public val env: Map<String, String>?

		public companion object
	}

	public interface Uses : Step {

		@Suppress("detekt.MemberNameEqualsClassName")
		public val uses: String
		public val with: Map<String, String>?

		public companion object
	}
}
