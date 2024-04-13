package net.twisterrob.ghlint.yaml

import net.twisterrob.ghlint.analysis.Analyzer
import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.SnakeComponentFactory
import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.ruleset.RuleSet
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
import kotlin.jvm.optionals.getOrElse

public object SnakeYaml {

	private val factory = SnakeComponentFactory()

	public fun analyze(files: List<File>, ruleSets: List<RuleSet>): List<Finding> {
		val workflows = files.map(::loadWorkflow)
		return Analyzer().analyzeWorkflows(workflows, ruleSets)
	}

	public fun loadWorkflow(file: File): Workflow =
		factory.createWorkflow(file)

	public fun load(@Language("yaml") yaml: String): Node {
		val settings = LoadSettings.builder()
			.setParseComments(true)
			// Load the whole YAML into one buffer to prevent problems with getDash().
			.setBufferSize(yaml.length.coerceAtLeast(1))
			.setSchema(JsonSchema())
			.build()
		try {
			return Composer(settings, ParserImpl(settings, StreamReader(settings, yaml))).singleNode
				.getOrElse { ScalarNode(Tag.NULL, "", ScalarStyle.PLAIN) }
		} catch (ex: org.snakeyaml.engine.v2.exceptions.YamlEngineException) {
			throw IllegalArgumentException("Failed to parse YAML: ${ex.message ?: ex}\nFull input:\n${yaml}", ex)
		}
	}

	@Language("yaml")
	public fun save(node: Node): String {
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
