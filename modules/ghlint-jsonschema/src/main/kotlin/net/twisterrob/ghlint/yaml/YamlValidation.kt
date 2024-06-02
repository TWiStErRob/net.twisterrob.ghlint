package net.twisterrob.ghlint.yaml

import dev.harrel.jsonschema.Error
import dev.harrel.jsonschema.SchemaResolver
import dev.harrel.jsonschema.ValidatorFactory
import net.twisterrob.ghlint.model.FileLocation
import net.twisterrob.ghlint.model.RawFile
import org.snakeyaml.engine.v2.nodes.Node
import java.net.URI
import java.net.URL

public object YamlValidation {

	/**
	 * See [source code](https://github.com/SchemaStore/schemastore/blob/master/src/schemas/json/github-workflow.json).
	 */
	private const val WORKFLOW_SCHEMA_URL = "https://json.schemastore.org/github-workflow.json"

	/**
	 * See [source code](https://github.com/SchemaStore/schemastore/blob/master/src/schemas/json/github-action.json).
	 */
	private const val ACTION_SCHEMA_URL = "https://json.schemastore.org/github-action.json"

	private val resolver = object : SchemaResolver {
		private val cache: MutableMap<String, SchemaResolver.Result> = mutableMapOf()

		override fun resolve(uri: String): SchemaResolver.Result =
			cache.getOrPut(uri) {
				SchemaResolver.Result.fromString(URL(uri).readText())
			}
	}

	public fun validate(node: Node, type: YamlValidationType): List<YamlValidationProblem> {
		val validator = ValidatorFactory()
			.withDisabledSchemaValidation(true)
			.withJsonNodeFactory(SnakeYamlJsonNode.Factory {
				SnakeYaml.loadRaw(RawFile(FileLocation("unknown"), it))
			})
			.withSchemaResolver(resolver)
			.createValidator()
		val uri = URI.create(
			when (type) {
				YamlValidationType.WORKFLOW -> WORKFLOW_SCHEMA_URL
				YamlValidationType.ACTION -> ACTION_SCHEMA_URL
			}
		)!!
		val validationProblems = validator
			.validate(uri, node)
			.errors
			.map { YamlValidationProblem(it.error, it.instanceLocation) }
		val duplicationProblems = detectDuplicateKeys(node)
		return validationProblems + duplicationProblems
	}
}

internal fun Error.toDisplayString(): String =
	"${this.instanceLocation}: ${this.error}\nvalidated by ${this.evaluationPath} (${this.schemaLocation})"

internal fun YamlValidationProblem.toDisplayString(): String =
	"${this.instanceLocation}: ${this.error}"
