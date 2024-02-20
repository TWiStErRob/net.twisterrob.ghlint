package net.twisterrob.ghlint.model

import net.twisterrob.ghlint.analysis.SnakeActionResolver
import net.twisterrob.ghlint.model.Action.ActionInput
import net.twisterrob.ghlint.model.SnakeJob.SnakeNormalJob
import net.twisterrob.ghlint.model.SnakeJob.SnakeReusableWorkflowCallJob
import net.twisterrob.ghlint.yaml.Yaml
import net.twisterrob.ghlint.yaml.getDash
import net.twisterrob.ghlint.yaml.getOptional
import net.twisterrob.ghlint.yaml.getOptionalText
import net.twisterrob.ghlint.yaml.getRequiredKey
import net.twisterrob.ghlint.yaml.text
import org.snakeyaml.engine.v2.nodes.MappingNode
import org.snakeyaml.engine.v2.nodes.Node

public class SnakeComponentFactory {

	private val actionCache: MutableMap<String, Action> = mutableMapOf()

	public fun createWorkflow(file: File): Workflow {
		val node = Yaml.load(file.content) as MappingNode
		return SnakeWorkflow(
			factory = this,
			parent = file,
			node = node,
			target = node.getRequiredKey("jobs")
		)
	}

	internal fun createJob(workflow: Workflow, key: Node, node: Node): Job {
		node as MappingNode
		return when {
			node.getOptionalText("uses") != null ->
				SnakeReusableWorkflowCallJob(
					parent = workflow,
					id = key.text,
					node = node,
					target = key
				)

			node.getOptional("steps") != null ->
				SnakeNormalJob(
					factory = this,
					parent = workflow,
					id = key.text,
					node = node,
					target = key,
				)

			else ->
				error("Unknown job type: ${node}")
		}
	}

	internal fun createStep(parent: Job.NormalJob, index: Int, node: Node): Step {
		node as MappingNode
		return when {
			node.getOptionalText("uses") != null ->
				SnakeStep.SnakeUses(
					factory = this,
					parent = parent,
					index = Step.Index(index),
					node = node,
					target = node.getDash(),
				)

			node.getOptionalText("run") != null ->
				SnakeStep.SnakeRun(
					parent = parent,
					index = Step.Index(index),
					node = node,
					target = node.getDash(),
				)

			else ->
				error("Unknown step type: ${node}")
		}
	}

	internal fun createDefaults(node: Node): Defaults =
		SnakeDefaults(
			factory = this,
			node = node as MappingNode,
		)

	internal fun createDefaultsRun(node: Node): Defaults.Run =
		SnakeDefaults.SnakeRun(
			node = node as MappingNode,
		)

	public fun createAction(file: File): Action {
		val node = Yaml.load(file.content) as MappingNode
		return SnakeAction(
			factory = this,
			parent = file,
			node = node,
			target = node.getRequiredKey("name"),
		)
	}

	internal fun createActionInput(action: SnakeAction, key: Node, node: Node): ActionInput {
		node as MappingNode
		return SnakeActionInput(
			parent = action,
			id = key.text,
			node = node,
			target = key,
		)
	}

	internal fun createUsesAction(uses: String, versionComment: String?): Step.UsesAction =
		SnakeUsesAction(
			factory = this,
			uses = uses,
			versionComment = versionComment,
		)

	internal fun createUsedAction(owner: String, repo: String, path: String?, ref: String): Action =
		actionCache.getOrPut("$owner/$repo/${path ?: "null"}@$ref") {
			val file = SnakeActionResolver().resolveAction(owner = owner, repo = repo, path = path, ref = ref)
			this.createAction(file)
		}
}
