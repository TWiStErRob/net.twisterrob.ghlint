package net.twisterrob.ghlint.model

import net.twisterrob.ghlint.model.Action.ActionInput
import net.twisterrob.ghlint.model.SnakeJob.SnakeNormalJob
import net.twisterrob.ghlint.model.SnakeJob.SnakeReusableWorkflowCallJob
import net.twisterrob.ghlint.yaml.SnakeErrorContent
import net.twisterrob.ghlint.yaml.SnakeUnknownContent
import net.twisterrob.ghlint.yaml.getDash
import net.twisterrob.ghlint.yaml.getOptional
import net.twisterrob.ghlint.yaml.getOptionalText
import net.twisterrob.ghlint.yaml.getRequiredKey
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
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.jvm.optionals.getOrElse

@Suppress("detekt.TooManyFunctions")
public class SnakeComponentFactory {

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
			val message = "Failed to parse YAML: ${ex.message ?: ex}\n" +
					"Full input (${file.location.path}):\n" +
					file.content
			throw IllegalArgumentException(message, ex)
		}
		return node
	}

	public fun createFile(file: RawFile): File =
		SnakeFile(file, this)

	// STOPSHIP separate? part of model?
	public enum class FileType {

		ACTION,
		WORKFLOW,
		UNKNOWN,
	}

	private fun inferType(file: File, @Suppress("UNUSED_PARAMETER") node: Node): FileType {
		val fileName = file.location.name.lowercase()
		return when {
			(fileName == "action.yml" || fileName == "action.yaml") && !file.location.isInGitHubWorkflows ->
				FileType.ACTION

			fileName.endsWith(".yml") || fileName.endsWith(".yaml") ->
				FileType.WORKFLOW

			else ->
				FileType.UNKNOWN
		}
	}

	internal fun createContent(file: File, node: Node): Content =
		when (val type = inferType(file, node)) {
			FileType.WORKFLOW ->
				createWorkflowSafe(file, node)

			FileType.ACTION ->
				createActionSafe(file, node)

			FileType.UNKNOWN ->
				SnakeUnknownContent(
					parent = file,
					node = node,
					inferredType = type,
					error = IllegalArgumentException("Unknown file type: ${file.location}")
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
					inferredType = FileType.WORKFLOW,
					error = IllegalArgumentException("Root node is not a mapping: ${node::class.java.simpleName}.")
				)
			}
		} catch (@Suppress("detekt.TooGenericExceptionCaught") ex: Exception) {
			SnakeErrorContent(
				parent = file,
				node = node,
				inferredType = FileType.WORKFLOW,
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
					inferredType = FileType.ACTION,
					error = IllegalArgumentException("Root node is not a mapping: ${node::class.java.simpleName}.")
				)
			}
		} catch (@Suppress("detekt.TooGenericExceptionCaught") ex: Exception) {
			SnakeErrorContent(
				parent = file,
				node = node,
				inferredType = FileType.ACTION,
				error = ex
			)
		}

	public fun createWorkflow(file: File, node: Node): Workflow {
		node as MappingNode
		return SnakeWorkflow(
			parent = file,
			factory = this,
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

	public fun createAction(file: File, node: Node): Action {
		node as MappingNode
		return SnakeAction(
			parent = file,
			factory = this,
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
			uses = uses,
			versionComment = versionComment,
		)

	public fun createSecrets(it: Node): Job.Secrets =
		if (it is MappingNode) {
			SnakeJob.SnakeSecretsExplicit(
				node = it,
				target = it,
				map = it.map.toTextMap()
			)
		} else if (it is ScalarNode && it.text == "inherit") {
			SnakeJob.SnakeSecretsInherit(
				node = it,
				target = it,
			)
		} else {
			error("Unsupported secrets: ${it}")
		}
}

private val FileLocation.isInGitHubWorkflows: Boolean
	get() =
		// !endsWith(".github/workflows/action.y[a]ml"), but in a way that supports running in the folder.
		Path.of(path).run { parent.parent.name == ".github" && parent.name == "workflows" }
