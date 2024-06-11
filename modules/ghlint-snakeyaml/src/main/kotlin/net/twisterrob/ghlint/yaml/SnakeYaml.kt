package net.twisterrob.ghlint.yaml

import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.RawFile
import net.twisterrob.ghlint.model.SnakeComponentFactory
import net.twisterrob.ghlint.model.SnakeFile
import org.intellij.lang.annotations.Language
import org.snakeyaml.engine.v2.api.Dump
import org.snakeyaml.engine.v2.api.DumpSettings
import org.snakeyaml.engine.v2.api.StreamDataWriter
import org.snakeyaml.engine.v2.nodes.Node
import java.io.StringWriter

public object SnakeYaml {

	public fun load(file: RawFile): File =
		SnakeComponentFactory(file).file

	public fun loadRaw(yaml: RawFile): Node =
		(SnakeComponentFactory(yaml).file as SnakeFile).node

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
