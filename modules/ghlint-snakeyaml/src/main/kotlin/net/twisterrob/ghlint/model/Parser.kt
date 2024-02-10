package net.twisterrob.ghlint.model

import net.twisterrob.ghlint.yaml.Yaml
import net.twisterrob.ghlint.yaml.getOptional
import net.twisterrob.ghlint.yaml.getOptionalText
import org.snakeyaml.engine.v2.nodes.MappingNode

public fun Workflow.Companion.from(file: File): Workflow =
	SnakeWorkflow(file, Yaml.load(file.readText()) as MappingNode)

private fun File.readText(): String =
	java.io.File(file.path).readText()

public fun Job.Companion.from(workflow: Workflow, key: String, node: MappingNode): Job =
	when {
		node.getOptionalText("uses") != null -> SnakeJob.SnakeReusableWorkflowCallJob(workflow, key, node)
		node.getOptional("steps") != null -> SnakeJob.SnakeNormalJob(workflow, key, node)
		else -> error("Unknown job: $node")
	}

public fun Step.Companion.from(job: Job.NormalJob, index: Int, node: MappingNode): Step =
	when {
		node.getOptionalText("uses") != null -> SnakeStep.SnakeUses(job, Step.Index(index), node)
		node.getOptionalText("run") != null -> SnakeStep.SnakeRun(job, Step.Index(index), node)
		else -> error("Unknown step type: $node")
	}

public fun Job.NormalJob.Defaults.Companion.from(node: MappingNode): Job.NormalJob.Defaults =
	SnakeJob.SnakeNormalJob.SnakeDefaults(node)

public fun Job.NormalJob.Defaults.Run.Companion.from(node: MappingNode): Job.NormalJob.Defaults.Run =
	SnakeJob.SnakeNormalJob.SnakeDefaults.SnakeRun(node)
