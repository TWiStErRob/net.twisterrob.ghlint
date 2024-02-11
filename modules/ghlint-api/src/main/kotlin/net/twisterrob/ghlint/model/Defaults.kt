package net.twisterrob.ghlint.model

public interface Defaults {

	public val run: Run?

	public interface Run {

		public val shell: String?

		public companion object
	}

	public companion object
}
