package net.twisterrob.ghlint.yaml

import dev.harrel.jsonschema.Error
import dev.harrel.jsonschema.SchemaResolver
import dev.harrel.jsonschema.ValidatorFactory
import net.twisterrob.ghlint.model.FileLocation
import net.twisterrob.ghlint.model.RawFile
import org.snakeyaml.engine.v2.nodes.Node
import java.net.URI
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.readText

public class YamlValidation(
	workflowSchema: URI = URI("https://json.schemastore.org/github-workflow.json"),
	actionSchema: URI = URI("https://json.schemastore.org/github-action.json"),
) {
	private val schemas = mapOf(
		YamlValidationType.WORKFLOW to Schema(workflowSchema),
		YamlValidationType.ACTION to Schema(actionSchema),
	)

	public fun validate(node: Node, type: YamlValidationType): List<YamlValidationProblem> {
		val schema = schemas.getValue(type)
		val validator = ValidatorFactory()
			.withDisabledSchemaValidation(true)
			.withJsonNodeFactory(SnakeYamlJsonNode.Factory {
				SnakeYaml.loadRaw(RawFile(FileLocation("unknown"), it))
			})
			.withSchemaResolver(schema)
			.createValidator()
		val validationProblems = validator
			.validate(schema.uri, node)
			.errors
			.map { YamlValidationProblem(it.error, it.instanceLocation) }
		val duplicationProblems = detectDuplicateKeys(node)
		return validationProblems + duplicationProblems
	}

	private class Schema(val uri: URI) : SchemaResolver {
		private val cache = ConcurrentHashMap<String, SchemaResolver.Result>()

		override fun resolve(uri: String): SchemaResolver.Result = cache.computeIfAbsent(uri) {
			val requested = URI(uri)
			val content = when {
				requested.scheme == "embedded" ->
					checkNotNull(YamlValidation::class.java.getResourceAsStream("schemas${requested.path}")) {
						"Embedded schema not found: $requested"
					}.bufferedReader().use { it.readText() }
				requested.scheme == "file" -> Path.of(requested).readText()
				this.uri.scheme in setOf("http", "https") -> {
					val connection = requested.toURL().openConnection().apply {
						connectTimeout = 10_000
						readTimeout = 30_000
					}
					connection.getInputStream().bufferedReader().use { it.readText() }
				}
				else -> error("Cannot resolve schema $uri: network access is disabled for this schema source.")
			}
			SchemaResolver.Result.fromString(content)
		}
	}

	public companion object {
		/** Shared latest-schema validator for callers that do not select schema sources. */
		public val default: YamlValidation = YamlValidation()

		public fun validate(node: Node, type: YamlValidationType): List<YamlValidationProblem> =
			default.validate(node, type)
	}
}

internal fun Error.toDisplayString(): String =
	"${this.instanceLocation}: ${this.error}\nvalidated by ${this.evaluationPath} (${this.schemaLocation})"

internal fun YamlValidationProblem.toDisplayString(): String =
	"${this.instanceLocation}: ${this.error}"
