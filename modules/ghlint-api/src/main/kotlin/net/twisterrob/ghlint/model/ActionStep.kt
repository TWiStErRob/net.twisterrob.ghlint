package net.twisterrob.ghlint.model

/**
 * Represents a step in a GitHub Actions [Action].
 *
 * References:
 *  * [General documentation](https://docs.github.com/en/actions/learn-github-actions/understanding-github-actions#actions)
 *  * [`runs.steps.*`](https://docs.github.com/en/actions/creating-actions/metadata-syntax-for-github-actions#runssteps)
 */
public sealed interface ActionStep : Step, Component {

	public val parent: Action.Runs.CompositeRuns

	public companion object;

	// TODO find a way to remove this from the API.
	public interface BaseStep : ActionStep, Step.BaseStep {

		public companion object
	}

	public interface Run : BaseStep, Step.Run {

		public override val shell: String

		public companion object
	}

	public interface Uses : BaseStep, Step.Uses {

		public companion object
	}
}
