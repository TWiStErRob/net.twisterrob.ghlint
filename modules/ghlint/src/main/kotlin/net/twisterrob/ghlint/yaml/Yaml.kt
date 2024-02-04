package net.twisterrob.ghlint.yaml

import org.snakeyaml.engine.v2.api.Dump
import org.snakeyaml.engine.v2.api.DumpSettings
import org.snakeyaml.engine.v2.api.LoadSettings
import org.snakeyaml.engine.v2.api.StreamDataWriter
import org.snakeyaml.engine.v2.composer.Composer
import org.snakeyaml.engine.v2.nodes.Node
import org.snakeyaml.engine.v2.parser.ParserImpl
import org.snakeyaml.engine.v2.scanner.StreamReader
import java.io.StringWriter

internal object Yaml {

	internal fun load(yaml: String): Node {
		val settings = LoadSettings.builder()
			.setParseComments(true)
			.build()
		return Composer(settings, ParserImpl(settings, StreamReader(settings, yaml))).singleNode.get()
	}

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
