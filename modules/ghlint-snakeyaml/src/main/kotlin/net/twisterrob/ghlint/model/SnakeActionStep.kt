package net.twisterrob.ghlint.model

import net.twisterrob.ghlint.yaml.getOptional
import net.twisterrob.ghlint.yaml.getOptionalText
import net.twisterrob.ghlint.yaml.getRequiredText
import net.twisterrob.ghlint.yaml.map
import net.twisterrob.ghlint.yaml.toTextMap
import org.snakeyaml.engine.v2.nodes.MappingNode
import org.snakeyaml.engine.v2.nodes.Node

public sealed class SnakeActionStep(
	override val factory: SnakeComponentFactory,
) : ActionStep.BaseStep, HasSnakeNode<MappingNode>, SnakeElement {

	override val name: String?
		get() = node.getOptionalText("name")

	override val id: String?
		get() = node.getOptionalText("id")

	override val `if`: String?
		get() = node.getOptionalText("if")

	override val env: Env?
		get() = node.getOptional("env")?.let { factory.createEnv(it) }

	public class SnakeActionStepRun internal constructor(
		override val factory: SnakeComponentFactory,
		override val parent: Action.Runs.CompositeRuns,
		override val index: Step.Index,
		override val node: MappingNode,
		override val target: Node,
	) : ActionStep.Run, SnakeActionStep(factory) {

		@Suppress("detekt.MemberNameEqualsClassName")
		override val run: String
			get() = node.getRequiredText("run")

		override val shell: String
			get() = node.getRequiredText("shell")

		override val workingDirectory: String?
			get() = node.getOptionalText("working-directory")
	}

	public class SnakeActionStepUses internal constructor(
		override val factory: SnakeComponentFactory,
		override val parent: Action.Runs.CompositeRuns,
		override val index: Step.Index,
		override val node: MappingNode,
		override val target: Node,
	) : ActionStep.Uses, SnakeActionStep(factory) {

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
