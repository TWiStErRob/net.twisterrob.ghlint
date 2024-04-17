package net.twisterrob.ghlint.model

/**
 * https://docs.github.com/en/actions/learn-github-actions/understanding-github-actions#actions
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
