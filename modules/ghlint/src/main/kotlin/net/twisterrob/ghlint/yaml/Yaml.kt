package net.twisterrob.ghlint.yaml

import dev.harrel.jsonschema.Error
import dev.harrel.jsonschema.Validator
import dev.harrel.jsonschema.ValidatorFactory
import org.intellij.lang.annotations.Language
import org.snakeyaml.engine.v2.api.Dump
import org.snakeyaml.engine.v2.api.DumpSettings
import org.snakeyaml.engine.v2.api.LoadSettings
import org.snakeyaml.engine.v2.api.StreamDataWriter
import org.snakeyaml.engine.v2.common.ScalarStyle
import org.snakeyaml.engine.v2.composer.Composer
import org.snakeyaml.engine.v2.nodes.Node
import org.snakeyaml.engine.v2.nodes.ScalarNode
import org.snakeyaml.engine.v2.nodes.Tag
import org.snakeyaml.engine.v2.parser.ParserImpl
import org.snakeyaml.engine.v2.scanner.StreamReader
import org.snakeyaml.engine.v2.schema.JsonSchema
import java.io.StringWriter
import java.net.URL
import kotlin.jvm.optionals.getOrElse

internal object Yaml {

	internal fun validate(@Language("yaml") yaml: String): Validator.Result {
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

	internal fun load(@Language("yaml") yaml: String): Node {
		val settings = LoadSettings.builder()
			.setParseComments(true)
			.setSchema(JsonSchema())
			.build()
		return Composer(settings, ParserImpl(settings, StreamReader(settings, yaml))).singleNode
			.getOrElse { ScalarNode(Tag.NULL, "", ScalarStyle.PLAIN) }
	}

	@Language("yaml")
	internal fun save(node: Node): String {
		val settings = DumpSettings.builder()
			.setDumpComments(true)
			.build()
		return Dump(settings).dumpNodeToString(node)
	}
}

private fun Dump.dumpNodeToString(node: Node): String {
	class StreamToStringWriter : StringWriter(), StreamDataWriter {

		override fun flush() {
			super<StringWriter>.flush()
		}
	}

	val output = StreamToStringWriter()
	output.use { this@dumpNodeToString.dumpNode(node, it) }
	return output.toString()
}

internal fun Error.toDisplayString(): String =
	"${this.instanceLocation}: ${this.error}\nvalidated by ${this.evaluationPath} (${this.schemaLocation})"
