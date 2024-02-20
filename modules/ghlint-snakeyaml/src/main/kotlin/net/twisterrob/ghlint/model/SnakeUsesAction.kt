package net.twisterrob.ghlint.model

public class SnakeUsesAction internal constructor(
	private val factory: SnakeComponentFactory,
	public override val uses: String,
	public override val versionComment: String?,
) : Step.UsesAction {

	@Suppress("detekt.PropertyUsedBeforeDeclaration")
	override val action: Action
		get() = factory.createUsedAction(owner = owner, repo = repository, path = path, ref = ref)

	override val actionName: String
		get() = uses.substringBefore('@')

	override val ref: String
		get() = uses.substringAfter('@')

	override val owner: String
		get() = actionName.substringBefore('/')

	override val repository: String
		get() = actionName.removePrefix(owner).removePrefix("/").substringBefore('/')

	override val path: String?
		get() = actionName.removePrefix("${owner}/${repository}")
			.removePrefix("/")
			.takeIf { it.isNotEmpty() }
}
