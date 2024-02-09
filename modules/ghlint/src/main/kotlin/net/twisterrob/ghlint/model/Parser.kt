package net.twisterrob.ghlint.model

import net.twisterrob.ghlint.yaml.getOptional
import net.twisterrob.ghlint.yaml.getOptionalText
import org.snakeyaml.engine.v2.nodes.MappingNode
import org.snakeyaml.engine.v2.nodes.Node

internal fun Workflow.Companion.from(file: File, node: Node): Workflow =
	Workflow(file, node as MappingNode)

internal fun Job.Companion.from(workflow: Workflow, key: String, node: MappingNode): Job =
	when {
		node.getOptionalText("uses") != null -> Job.ReusableWorkflowCallJob(workflow, key, node)
		node.getOptional("steps") != null -> Job.NormalJob(workflow, key, node)
		else -> error("Unknown job: $node")
	}

internal fun Step.Companion.from(job: Job.NormalJob, node: MappingNode): Step =
	when {
		node.getOptionalText("uses") != null -> Step.Uses(job, node)
		node.getOptionalText("run") != null -> Step.Run(job, node)
		else -> error("Unknown step type: $node")
	}

internal fun Job.NormalJob.Defaults.Companion.from(node: MappingNode): Job.NormalJob.Defaults =
	Job.NormalJob.Defaults(node)
