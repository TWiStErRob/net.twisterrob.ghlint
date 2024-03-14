package net.twisterrob.ghlint.model

import net.twisterrob.ghlint.results.Location
import net.twisterrob.ghlint.yaml.getOptional
import net.twisterrob.ghlint.yaml.getOptionalText
import net.twisterrob.ghlint.yaml.getRequiredText
import net.twisterrob.ghlint.yaml.map
import net.twisterrob.ghlint.yaml.toTextMap
import org.snakeyaml.engine.v2.nodes.MappingNode
import org.snakeyaml.engine.v2.nodes.Node

public sealed class SnakeStep protected constructor(
	override val node: MappingNode,
) : Step.BaseStep, HasSnakeNode, HasSnakeTarget {

	override val location: Location
		get() = super.location

	override val name: String?
		get() = node.getOptionalText("name")

	override val id: String?
		get() = node.getOptionalText("id")

	override val `if`: String?
		get() = node.getOptionalText("if")

	public class SnakeRun internal constructor(
		override val parent: Job.NormalJob,
		override val index: Step.Index,
		override val node: MappingNode,
		override val target: Node,
	) : Step.Run, SnakeStep(node) {

		@Suppress("detekt.MemberNameEqualsClassName")
		override val run: String
			get() = node.getRequiredText("run")

		override val shell: String?
			get() = node.getOptionalText("shell")

		override val env: Map<String, String>?
			get() = node.getOptional("env")?.run { map.toTextMap() }
	}

	public class SnakeUses internal constructor(
		private val factory: SnakeComponentFactory,
		override val parent: Job.NormalJob,
		override val index: Step.Index,
		override val node: MappingNode,
		override val target: Node,
	) : Step.Uses, SnakeStep(node) {

		@Suppress("detekt.MemberNameEqualsClassName")
		override val uses: Step.UsesAction
			get() = factory.createUsesAction(
				uses = node.getRequiredText("uses"),
				versionComment = node.inLineComments?.singleOrNull()?.value,
			)

		override val with: Map<String, String>?
			get() = node.getOptional("with")?.run { map.toTextMap() }
	}
}
