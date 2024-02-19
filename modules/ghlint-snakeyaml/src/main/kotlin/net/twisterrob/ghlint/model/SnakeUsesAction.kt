package net.twisterrob.ghlint.model

public class SnakeUsesAction internal constructor(
	private val factory: SnakeFactory,
	public override val raw: String,
	public override val versionComment: String?,
) : Step.UsesAction {

	@Suppress("detekt.PropertyUsedBeforeDeclaration")
	override val action: Action
		get() = factory.createUsedAction(owner = owner, repo = repository, path = path, ref = ref)

	override val actionName: String
		get() = raw.substringBefore('@')

	override val ref: String
		get() = raw.substringAfter('@')

	override val owner: String
		get() = ref.substringBefore('/')

	override val repository: String
		get() = ref.removePrefix(owner).removePrefix("/").substringBefore('/')

	override val path: String?
		get() = ref.removePrefix("${owner}/${repository}")
			.removePrefix("/")
			.takeIf { it.isNotEmpty() }
}
