package net.twisterrob.ghlint.model

public sealed interface Env {

	public fun asMap(): Map<String, String> =
		if (this is Explicit) this else emptyMap()

	public fun asText(): String? =
		if (this is Dynamic) value else null

	public interface Explicit : Env, Map<String, String>

	public interface Dynamic : Env {
		public val value: String
	}
}
