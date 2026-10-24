package net.twisterrob.ghlint.yaml

import dev.harrel.jsonschema.Error
import dev.harrel.jsonschema.SchemaResolver
import dev.harrel.jsonschema.ValidatorFactory
import net.twisterrob.ghlint.model.FileLocation
import net.twisterrob.ghlint.model.RawFile
import org.snakeyaml.engine.v2.nodes.Node
import java.net.URI
import java.util.concurrent.ConcurrentHashMap

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
			check(requested.isLocalSchema() || this.uri.scheme in setOf("http", "https")) {
				"Cannot resolve schema $uri: network access is disabled for this schema source."
			}
			val connection = requested.toURL().openConnection().apply {
				useCaches = false
				connectTimeout = 10_000
				readTimeout = 30_000
			}
			val content = connection.getInputStream().bufferedReader().use { it.readText() }
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

private fun URI.isLocalSchema(): Boolean = when (scheme) {
	"file" -> true
	"jar" -> URI(schemeSpecificPart.substringBefore("!/")).isLocalSchema()
	else -> false
}

internal fun Error.toDisplayString(): String =
	"${this.instanceLocation}: ${this.error}\nvalidated by ${this.evaluationPath} (${this.schemaLocation})"

internal fun YamlValidationProblem.toDisplayString(): String =
	"${this.instanceLocation}: ${this.error}"
