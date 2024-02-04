package net.twisterrob.ghlint.model

import net.twisterrob.ghlint.yaml.getOptionalText
import org.snakeyaml.engine.v2.nodes.MappingNode
import org.snakeyaml.engine.v2.nodes.Node

internal fun Workflow.Companion.from(node: Node): Workflow =
	Workflow(node as MappingNode)

internal fun Job.Companion.from(node: Node): Job =
	Job(node as MappingNode)

internal fun Step.Companion.from(node: MappingNode): Step =
	when {
		node.getOptionalText("uses") != null -> Step.Uses(node)
		node.getOptionalText("run") != null -> Step.Run(node)
		else -> error("Unknown step type: $node")
	}
