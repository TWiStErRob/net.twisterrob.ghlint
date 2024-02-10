package net.twisterrob.ghlint.model

public sealed interface Job : Model {

	public val parent: Workflow
	public val id: String
	public val name: String?
	public val env: Map<String, String>?
	public val permissions: Map<String, String>?

	@Suppress("detekt.VariableNaming")
	public val `if`: String?

	public companion object;

	public interface BaseJob : Job

	public interface NormalJob : Job {

		public val steps: List<Step>
		public val defaults: Defaults?
		public val timeoutMinutes: Int?

		public interface Defaults {

			public val run: Run?

			public interface Run {

				public val shell: String?

				public companion object
			}

			public companion object
		}

		public companion object
	}

	public interface ReusableWorkflowCallJob : Job {

		public val uses: String

		public companion object
	}
}
