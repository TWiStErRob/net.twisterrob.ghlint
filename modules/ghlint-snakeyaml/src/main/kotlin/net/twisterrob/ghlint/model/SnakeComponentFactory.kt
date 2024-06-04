package net.twisterrob.ghlint.model

import net.twisterrob.ghlint.model.SnakeJob.SnakeNormalJob
import net.twisterrob.ghlint.model.SnakeJob.SnakeReusableWorkflowCallJob
import net.twisterrob.ghlint.yaml.getDash
import net.twisterrob.ghlint.yaml.getOptional
import net.twisterrob.ghlint.yaml.getOptionalText
import net.twisterrob.ghlint.yaml.getRequiredKey
import net.twisterrob.ghlint.yaml.getRequiredText
import net.twisterrob.ghlint.yaml.map
import net.twisterrob.ghlint.yaml.text
import net.twisterrob.ghlint.yaml.toTextMap
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

@Suppress("detekt.TooManyFunctions")
public class SnakeComponentFactory(
	file: RawFile,
) {

	public val file: File = createFile(file)

	public fun loadYaml(file: RawFile): Node {
		val settings = LoadSettings.builder()
			.setParseComments(true)
			// Load the whole YAML into one buffer to prevent problems with getDash().
			.setBufferSize(file.content.length.coerceAtLeast(1))
			.setSchema(JsonSchema())
			.build()
		val node = try {
			Composer(settings, ParserImpl(settings, StreamReader(settings, file.content))).singleNode
				.getOrElse { ScalarNode(Tag.NULL, "", ScalarStyle.PLAIN) }
		} catch (ex: YamlEngineException) {
			throw IllegalArgumentException("Failed to parse YAML: ${ex.message ?: ex}", ex)
		}
		return node
	}

	public fun createFile(file: RawFile): File =
		SnakeFile(
			factory = this,
			origin = file,
		)

	internal fun createContent(file: File, node: Node): Content =
		when (file.location.inferType()) {
			FileType.WORKFLOW ->
				createWorkflowSafe(file, node)

			FileType.ACTION ->
				createActionSafe(file, node)

			FileType.UNKNOWN ->
				SnakeUnknownContent(
					parent = file,
					node = node,
					error = IllegalArgumentException("Unknown file type of ${file.location.path}")
				)
		}

	private fun createWorkflowSafe(file: File, node: Node): Content =
		try {
			if (node is MappingNode) {
				createWorkflow(file, node)
			} else {
				SnakeErrorContent(
					parent = file,
					node = node,
					// Intentionally redacting info to prevent TMI.
					error = IllegalArgumentException("Root node is not a mapping: ${node::class.java.simpleName}.")
				)
			}
		} catch (@Suppress("detekt.TooGenericExceptionCaught") ex: Exception) {
			SnakeErrorContent(
				parent = file,
				node = node,
				error = ex
			)
		}

	private fun createActionSafe(file: File, node: Node): Content =
		try {
			if (node is MappingNode) {
				createAction(file, node)
			} else {
				SnakeErrorContent(
					parent = file,
					node = node,
					// Intentionally redacting info to prevent TMI.
					error = IllegalArgumentException("Root node is not a mapping: ${node::class.java.simpleName}.")
				)
			}
		} catch (@Suppress("detekt.TooGenericExceptionCaught") ex: Exception) {
			SnakeErrorContent(
				parent = file,
				node = node,
				error = ex
			)
		}

	private fun createWorkflow(file: File, node: Node): Workflow {
		node as MappingNode
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
					factory = this,
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

	internal fun createStep(parent: Job.NormalJob, index: Int, node: Node): WorkflowStep {
		node as MappingNode
		return when {
			node.getOptionalText("uses") != null ->
				SnakeWorkflowStep.SnakeWorkflowStepUses(
					factory = this,
					parent = parent,
					index = Step.Index(index),
					node = node,
					target = node.getDash(),
				)

			node.getOptionalText("run") != null ->
				SnakeWorkflowStep.SnakeWorkflowStepRun(
					factory = this,
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
			target = node,
		)

	internal fun createDefaultsRun(node: Node): Defaults.Run =
		SnakeDefaults.SnakeRun(
			node = node as MappingNode,
		)

	private fun createAction(file: File, node: Node): Action {
		node as MappingNode
		return SnakeAction(
			factory = this,
			parent = file,
			node = node,
			target = node.getRequiredKey("name"),
		)
	}

	internal fun createActionInput(action: Action, key: Node, node: Node): Action.ActionInput {
		node as MappingNode
		return SnakeActionInput(
			factory = this,
			parent = action,
			id = key.text,
			node = node,
			target = key,
		)
	}

	internal fun createActionOutput(action: Action, key: Node, node: Node): Action.ActionOutput {
		node as MappingNode
		return SnakeActionOutput(
			factory = this,
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

	internal fun createEnv(node: Node): Env =
		when (node) {
			is MappingNode -> {
				SnakeEnvExplicit(
					factory = this,
					node = node,
					target = node,
					map = node.map.toTextMap()
				)
			}

			is ScalarNode -> {
				SnakeEnvDynamic(
					factory = this,
					node = node,
					target = node,
					text = node.text
				)
			}

			else -> {
				error("Unsupported env: ${node}")
			}
		}

	internal fun createSecrets(node: Node): Job.Secrets =
		if (node is MappingNode) {
			SnakeJob.SnakeSecretsExplicit(
				factory = this,
				node = node,
				target = node,
				map = node.map.toTextMap()
			)
		} else if (node is ScalarNode && node.text == "inherit") {
			SnakeJob.SnakeSecretsInherit(
				factory = this,
				node = node,
				target = node,
			)
		} else {
			error("Unsupported secrets: ${node}")
		}

	internal fun createRuns(action: Action, node: Node): Action.Runs {
		node as MappingNode
		val using = node.getRequiredText("using")
		return when (using) {
			"docker" ->
				SnakeRuns.SnakeDockerRuns(
					factory = this,
					parent = action,
					node = node,
					target = node,
				)

			"composite" ->
				SnakeRuns.SnakeCompositeRuns(
					factory = this,
					parent = action,
					node = node,
					target = node,
				)

			else ->
				SnakeRuns.SnakeJavascriptRuns(
					factory = this,
					parent = action,
					node = node,
					target = node,
				)
		}
	}

	internal fun createActionStep(parent: Action.Runs.CompositeRuns, index: Int, node: Node): ActionStep {
		node as MappingNode
		return when {
			node.getOptionalText("uses") != null ->
				SnakeActionStep.SnakeActionStepUses(
					factory = this,
					parent = parent,
					index = Step.Index(index),
					node = node,
					target = node.getDash(),
				)

			node.getOptionalText("run") != null ->
				SnakeActionStep.SnakeActionStepRun(
					factory = this,
					parent = parent,
					index = Step.Index(index),
					node = node,
					target = node.getDash(),
				)

			else ->
				error("Unknown step type: ${node}")
		}
	}

	internal fun createBranding(action: Action, node: Node): Action.Branding {
		node as MappingNode
		return SnakeBranding(
			factory = this,
			parent = action,
			node = node,
			target = node,
		)
	}
}
