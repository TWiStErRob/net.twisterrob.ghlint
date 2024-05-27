package net.twisterrob.ghlint.model

public sealed interface Env {

	public val map: Map<String, String>
	public val text: String?

	public interface Explicit : Env {
		override val text: String?
			get() = null
	}

	public interface Dynamic : Env {
		override val map: Map<String, String>
			get() = emptyMap()
	}
}

public val Env?.map: Map<String, String>
	get() = this?.map.orEmpty()
