package net.twisterrob.ghlint.model

import net.twisterrob.ghlint.yaml.array
import net.twisterrob.ghlint.yaml.getOptional
import net.twisterrob.ghlint.yaml.getOptionalText
import net.twisterrob.ghlint.yaml.getRequired
import net.twisterrob.ghlint.yaml.getRequiredText
import net.twisterrob.ghlint.yaml.map
import net.twisterrob.ghlint.yaml.toTextArray
import net.twisterrob.ghlint.yaml.toTextMap
import org.snakeyaml.engine.v2.nodes.MappingNode
import org.snakeyaml.engine.v2.nodes.Node

public sealed class SnakeRuns : HasSnakeNode<MappingNode> {

	public class SnakeJavascriptRuns(
		override val parent: Action,
		override val node: MappingNode,
		override val target: Node,
	) : Action.Runs.JavascriptRuns, SnakeRuns() {

		override val using: String
			get() = node.getRequiredText("using")
		override val main: String
			get() = node.getRequiredText("main")
		override val pre: String?
			get() = node.getOptionalText("pre")
		override val preIf: String?
			get() = node.getOptionalText("pre-if")
		override val post: String?
			get() = node.getOptionalText("post")
		override val postIf: String?
			get() = node.getOptionalText("post-if")
	}

	public class SnakeCompositeRuns(
		private val factory: SnakeComponentFactory,
		override val parent: Action,
		override val node: MappingNode,
		override val target: Node,
	) : Action.Runs.CompositeRuns, SnakeRuns() {

		override val steps: List<ActionStep> by lazy {
			node.getRequired("steps").array.mapIndexed { index, node ->
				factory.createActionStep(
					parent = this,
					index = index,
					node = node,
				)
			}
		}
	}

	public class SnakeDockerRuns(
		override val parent: Action,
		override val node: MappingNode,
		override val target: Node,
	) : Action.Runs.DockerRuns, SnakeRuns() {

		override val using: String
			get() = node.getRequiredText("using")

		override val image: String
			get() = node.getRequiredText("image")

		override val entrypoint: String?
			get() = node.getOptionalText("entrypoint")
		override val preEntrypoint: String?
			get() = node.getOptionalText("pre-entrypoint")
		override val postEntrypoint: String?
			get() = node.getOptionalText("post-entrypoint")

		override val args: List<String>?
			get() = node.getOptional("args")?.run { array.toTextArray() }

		override val env: Map<String, String>?
			get() = node.getOptional("env")?.run { map.toTextMap() }
	}
}
