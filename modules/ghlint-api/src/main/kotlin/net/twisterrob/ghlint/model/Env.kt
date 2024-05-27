package net.twisterrob.ghlint.model

public sealed interface Env {

	public fun asMap(): Map<String, String>
	public fun asText(): String?

	public interface Explicit : Env, Map<String, String> {
		override fun asMap(): Map<String, String> = this
		override fun asText(): String? = null
	}

	public interface Dynamic : Env {
		public val value: String
		override fun asMap(): Map<String, String> = emptyMap()
		override fun asText(): String? = value
	}
}

public fun Env?.asMap(): Map<String, String> =
	this?.asMap().orEmpty()
