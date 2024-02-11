package net.twisterrob.ghlint.model

/**
 * https://docs.github.com/en/actions/learn-github-actions/understanding-github-actions#jobs
 */
public sealed interface Job : Component {

	public val parent: Workflow
	public val id: String
	public val name: String?
	public val env: Map<String, String>?
	public val permissions: Map<String, String>?

	@Suppress("detekt.VariableNaming")
	public val `if`: String?

	public companion object;

	// TODO find a way to remove this from the API.
	public interface BaseJob : Job

	public interface NormalJob : BaseJob {

		public val steps: List<Step>
		public val defaults: Defaults?
		public val timeoutMinutes: Int?

		public companion object
	}

	public interface ReusableWorkflowCallJob : BaseJob {

		public val uses: String

		public companion object
	}
}
