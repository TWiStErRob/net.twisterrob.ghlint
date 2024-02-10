package net.twisterrob.ghlint.yaml

import dev.harrel.jsonschema.Error
import dev.harrel.jsonschema.Validator
import dev.harrel.jsonschema.ValidatorFactory
import org.intellij.lang.annotations.Language
import java.net.URL

public object YamlValidation {

	public fun validate(@Language("yaml") yaml: String): Validator.Result {
		val url =
			"https://raw.githubusercontent.com/SchemaStore/schemastore/master/src/schemas/json/github-workflow.json"
		val factory = SnakeYamlJsonNode.Factory(Yaml::load)
		val validator = ValidatorFactory()
			.withDisabledSchemaValidation(true)
			.withJsonNodeFactory(factory)
			.createValidator()
		val schema = validator.registerSchema(URL(url).readText())
		return validator.validate(schema, factory.create(yaml))
	}
}

internal fun Error.toDisplayString(): String =
	"${this.instanceLocation}: ${this.error}\nvalidated by ${this.evaluationPath} (${this.schemaLocation})"
