package net.twisterrob.ghlint.cli

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import net.twisterrob.ghlint.SchemaSource
import java.nio.file.Path
import kotlin.io.path.isReadable
import kotlin.io.path.isRegularFile

internal class SchemaOptions : OptionGroup("Schemas") {
	val workflow: SchemaSource by option("--workflow-schema", metavar = "SCHEMA")
		.convert { parseSchemaSource(it) }
		.default(SchemaSource.Latest, defaultForHelp = ":latest")
		.help("Workflow schema: :latest, :embedded (offline), or a local file (offline).")

	val action: SchemaSource by option("--action-schema", metavar = "SCHEMA")
		.convert { parseSchemaSource(it) }
		.default(SchemaSource.Latest, defaultForHelp = ":latest")
		.help("Action schema: :latest, :embedded (offline), or a local file (offline).")
}

private fun parseSchemaSource(value: String): SchemaSource = when (value) {
	":latest" -> SchemaSource.Latest
	":embedded" -> SchemaSource.Embedded
	else -> {
		val path = Path.of(value)
		require(path.isRegularFile() && path.isReadable()) {
			"Schema must be :latest, :embedded, or a readable file: $value"
		}
		SchemaSource.LocalFile(path)
	}
}
