package net.twisterrob.ghlint.model

import net.twisterrob.ghlint.model.Action.ActionInput
import net.twisterrob.ghlint.model.SnakeJob.SnakeNormalJob
import net.twisterrob.ghlint.model.SnakeJob.SnakeReusableWorkflowCallJob
import net.twisterrob.ghlint.yaml.getDash
import net.twisterrob.ghlint.yaml.getOptional
import net.twisterrob.ghlint.yaml.getOptionalText
import net.twisterrob.ghlint.yaml.getRequiredKey
import net.twisterrob.ghlint.yaml.text
import org.snakeyaml.engine.v2.api.LoadSettings
import org.snakeyaml.engine.v2.common.ScalarStyle
import org.snakeyaml.engine.v2.composer.Composer
import org.snakeyaml.engine.v2.exceptions.YamlEngineException
import org.snakeyaml.engine.v2.nodes.MappingNode
import org.snakeyaml.engine.v2.nodes.Node
import org.snakeyaml.engine.v2.nodes.ScalarNode
import org.snakeyaml.engine.v2.nodes.Tag
import org.snakeyaml.engine.v2.parser.ParserImpl
import org.snakeyaml.engine.v2.scanner.StreamReader
import org.snakeyaml.engine.v2.schema.JsonSchema
import kotlin.jvm.optionals.getOrElse

public class SnakeComponentFactory {

	public fun loadYaml(file: RawFile): Node {
		val settings = LoadSettings.builder()
			.setParseComments(true)
			.setSchema(JsonSchema())
			.build()
		val node = try {
			Composer(settings, ParserImpl(settings, StreamReader(settings, file.content))).singleNode
				.getOrElse { ScalarNode(Tag.NULL, "", ScalarStyle.PLAIN) }
		} catch (ex: YamlEngineException) {
			val message = "Failed to parse YAML: ${ex.message ?: ex}\n" +
					"Full input (${file.location.path}):\n" +
					file.content
			throw IllegalArgumentException(message, ex)
		}
		return node
	}

	public fun createWorkflow(file: RawFile, node: Node): Workflow {
		node as MappingNode
		return SnakeWorkflow(
			factory = this,
			node = node,
			target = node.getRequiredKey("jobs")
		).apply { parent = File(file.location, this) }
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

	public fun createAction(file: RawFile, node: Node): Action {
		node as MappingNode
		return SnakeAction(
			factory = this,
			node = node,
			target = node.getRequiredKey("name"),
		).apply { parent = File(file.location, this) }
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
			uses = uses,
			versionComment = versionComment,
		)
}
