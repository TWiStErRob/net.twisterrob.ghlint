package net.twisterrob.ghlint.yaml

import net.twisterrob.ghlint.analysis.Analyzer
import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.RawFile
import net.twisterrob.ghlint.model.SnakeComponentFactory
import net.twisterrob.ghlint.model.name
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.ruleset.RuleSet
import org.intellij.lang.annotations.Language
import org.snakeyaml.engine.v2.api.Dump
import org.snakeyaml.engine.v2.api.DumpSettings
import org.snakeyaml.engine.v2.api.StreamDataWriter
import org.snakeyaml.engine.v2.nodes.MappingNode
import org.snakeyaml.engine.v2.nodes.Node
import java.io.StringWriter

public object SnakeYaml {

	private val factory = SnakeComponentFactory()

	public fun analyze(files: List<RawFile>, ruleSets: List<RuleSet>): List<Finding> {
		val workflows = files.map(this::load)
		return Analyzer().analyzeWorkflows(workflows, ruleSets)
	}

	public fun load(file: RawFile): File {
		val node = factory.loadYaml(file) as MappingNode

		@Suppress("UseIfInsteadOfWhen")
		val content = when {
			file.location.name == "action.yml" && !file.location.path.endsWith(".github/workflows/action.yml") ->
				factory.createAction(file, node)

			else ->
				factory.createWorkflow(file, node)
		}
		return File(file.location, content)
	}

	public fun loadRaw(yaml: RawFile): Node =
		factory.loadYaml(yaml)

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
