package net.twisterrob.ghlint.model

/**
 * Represents a step in a GitHub Actions [Workflow].
 *
 * References:
 *  * [General documentation](https://docs.github.com/en/actions/learn-github-actions/understanding-github-actions#jobs)
 *  * [`jobs.jobs_id.steps.*`](https://docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions#jobsjob_idsteps)
 */
public sealed interface WorkflowStep : Step, Component {

	public val parent: Job.NormalJob

	public companion object;

	public interface BaseStep : WorkflowStep, Step.BaseStep {

		public companion object
	}

	public interface Run : BaseStep, Step.Run {

		public companion object
	}

	public interface Uses : BaseStep, Step.Uses {

		public companion object
	}
}
