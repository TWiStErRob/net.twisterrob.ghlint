package net.twisterrob.ghlint.model

public sealed interface Env {

	public interface Explicit : Env, Map<String, String>

	public interface Dynamic : Env {
		public val value: String
	}
}
