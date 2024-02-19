package net.twisterrob.ghlint.model

public class SnakeUsesAction internal constructor(
	public override val raw: String,
	public override val versionComment: String?,
) : Step.UsesAction {

	override val action: String
		get() = raw.substringBefore('@')

	override val ref: String
		get() = raw.substringAfter('@')

	override val owner: String
		get() = action.substringBefore('/')

	override val repository: String
		get() = action.removePrefix(owner).removePrefix("/").substringBefore('/')

	override val path: String?
		get() = action.removePrefix("${owner}/${repository}")
			.removePrefix("/")
			.takeIf { it.isNotEmpty() }
}
