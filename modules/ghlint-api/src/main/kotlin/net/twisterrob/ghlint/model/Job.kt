package net.twisterrob.ghlint.model

/**
 * https://docs.github.com/en/actions/learn-github-actions/understanding-github-actions#jobs
 */
public sealed interface Job : Component {

	public val parent: Workflow
	public val id: String
	public val name: String?
	public val env: Env?
	public val permissions: Set<Permission>?
	public val needs: List<String>?

	@Suppress("detekt.VariableNaming")
	public val `if`: String?

	public companion object;

	// TODO find a way to remove this from the API.
	public interface BaseJob : Job

	public interface NormalJob : BaseJob, Step.Parent {

		public override val steps: List<WorkflowStep>
		public val defaults: Defaults?
		public val timeoutMinutes: String?

		public companion object
	}

	public interface ReusableWorkflowCallJob : BaseJob {

		public val uses: String
		public val with: Map<String, String>?
		public val secrets: Secrets?

		public companion object
	}

	public sealed interface Secrets {

		public interface Inherit : Secrets
		public interface Explicit : Secrets, Map<String, String>
	}
}
