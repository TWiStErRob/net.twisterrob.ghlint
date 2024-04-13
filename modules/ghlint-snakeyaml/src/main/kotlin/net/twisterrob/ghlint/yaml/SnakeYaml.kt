package net.twisterrob.ghlint.yaml

import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.RawFile
import net.twisterrob.ghlint.model.SnakeComponentFactory
import net.twisterrob.ghlint.model.name
import org.intellij.lang.annotations.Language
import org.snakeyaml.engine.v2.api.Dump
import org.snakeyaml.engine.v2.api.DumpSettings
import org.snakeyaml.engine.v2.api.StreamDataWriter
import org.snakeyaml.engine.v2.nodes.MappingNode
import org.snakeyaml.engine.v2.nodes.Node
import java.io.StringWriter

public object SnakeYaml {

	private val factory = SnakeComponentFactory()

	@Suppress("LiftReturnOrAssignment", "detekt.ReturnCount")
	public fun load(file: RawFile): File {
		val node = try {
			factory.loadYaml(file)
		} catch (@Suppress("detekt.TooGenericExceptionCaught") ex: Exception) {
			return ErrorInvalidContent.create(file, ex).parent
		}
		if (node !is MappingNode) {
			val error = IllegalArgumentException("Root node is not a mapping: ${node::class.java.simpleName}.")
			return ErrorInvalidContent.create(file, error).parent
		}
		try {
			return loadUnsafe(file, node)
		} catch (@Suppress("detekt.TooGenericExceptionCaught") ex: Exception) {
			return ErrorInvalidContent.create(file, ex).parent
		}
	}

	private fun loadUnsafe(file: RawFile, node: MappingNode): File {
		@Suppress("detekt.UseIfInsteadOfWhen")
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
