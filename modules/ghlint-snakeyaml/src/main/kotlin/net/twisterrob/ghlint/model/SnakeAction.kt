package net.twisterrob.ghlint.model

import net.twisterrob.ghlint.yaml.getOptional
import net.twisterrob.ghlint.yaml.getOptionalText
import net.twisterrob.ghlint.yaml.getRequired
import net.twisterrob.ghlint.yaml.getRequiredText
import net.twisterrob.ghlint.yaml.map
import org.snakeyaml.engine.v2.nodes.MappingNode
import org.snakeyaml.engine.v2.nodes.Node

public class SnakeAction internal constructor(
	override val factory: SnakeComponentFactory,
	override val parent: File,
	override val node: MappingNode,
	override val target: Node,
) : Action, HasSnakeNode<MappingNode>, SnakeElement {

	override val name: String
		get() = node.getRequiredText("name")

	override val author: String?
		get() = node.getOptionalText("author")

	override val branding: Action.Branding?
		get() = node.getOptional("branding")
			?.let { factory.createBranding(this, it) }

	override val description: String
		get() = node.getRequiredText("description")

	override val inputs: Map<String, Action.ActionInput>?
		get() = node.getOptional("inputs")
			?.run {
				map
					.map { (key, node) ->
						factory.createActionInput(
							action = this@SnakeAction,
							key = key,
							node = node,
						)
					}
					.associateBy { it.id }
			}

	override val outputs: Map<String, Action.ActionOutput>?
		get() = node.getOptional("outputs")
			?.run {
				map
					.map { (key, node) ->
						factory.createActionOutput(
							action = this@SnakeAction,
							key = key,
							node = node,
						)
					}
					.associateBy { it.id }
			}

	override val runs: Action.Runs
		get() = factory.createRuns(
			action = this,
			node = node.getRequired("runs")
		)
}
