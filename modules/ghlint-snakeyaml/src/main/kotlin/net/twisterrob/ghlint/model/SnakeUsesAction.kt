package net.twisterrob.ghlint.model

import net.twisterrob.ghlint.analysis.ActionResolver

public class SnakeUsesAction internal constructor(
	public override val raw: String,
	public override val versionComment: String?,
	private val actionResolver: ActionResolver,
) : Step.UsesAction {

	@Suppress("detekt.PropertyUsedBeforeDeclaration")
	override val action: Action
		get() = actionResolver.resolveAction(owner = owner, repo = repository, path = path, ref = ref)

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
