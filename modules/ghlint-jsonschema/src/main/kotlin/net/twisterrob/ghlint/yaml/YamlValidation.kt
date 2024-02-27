package net.twisterrob.ghlint.yaml

import dev.harrel.jsonschema.Error
import dev.harrel.jsonschema.SchemaResolver
import dev.harrel.jsonschema.Validator
import dev.harrel.jsonschema.ValidatorFactory
import net.twisterrob.ghlint.model.Yaml
import java.net.URI
import java.net.URL

internal object YamlValidation {

	private const val WORKFLOW_SCHEMA_URL =
		"https://raw.githubusercontent.com/SchemaStore/schemastore/master/src/schemas/json/github-workflow.json"

	private val resolver = object : SchemaResolver {
		private val cache: MutableMap<String, SchemaResolver.Result> = mutableMapOf()

		override fun resolve(uri: String): SchemaResolver.Result =
			cache.getOrPut(uri) {
				SchemaResolver.Result.fromString(URL(uri).readText())
			}
	}

	fun validate(yaml: Yaml): Validator.Result {
		val validator = ValidatorFactory()
			.withDisabledSchemaValidation(true)
			.withJsonNodeFactory(SnakeYamlJsonNode.Factory(SnakeYaml::load))
			.withSchemaResolver(resolver)
			.createValidator()
		return validator.validate(URI.create(WORKFLOW_SCHEMA_URL), yaml.raw)
	}
}

internal fun Error.toDisplayString(): String =
	"${this.instanceLocation}: ${this.error}\nvalidated by ${this.evaluationPath} (${this.schemaLocation})"
