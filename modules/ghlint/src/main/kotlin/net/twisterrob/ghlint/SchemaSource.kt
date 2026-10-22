package net.twisterrob.ghlint

import net.twisterrob.ghlint.yaml.YamlValidation
import java.net.URI
import java.nio.file.Path

/** Selects the schema used to validate one kind of GitHub Actions file. */
public sealed interface SchemaSource {
	/** Download the latest schema, keeping it in memory for this invocation. */
	public data object Latest : SchemaSource

	/** Use the schema bundled with GH-Lint without accessing the network. */
	public data object Embedded : SchemaSource

	/** Use a local schema and resolve its file references without accessing the network. */
	public data class LocalFile(public val path: Path) : SchemaSource
}

internal fun SchemaSource.toUri(name: String): URI = when (this) {
	SchemaSource.Latest -> URI("https://json.schemastore.org/$name")
	SchemaSource.Embedded -> checkNotNull(YamlValidation::class.java.getResource("schemas/$name")) {
		"Embedded schema not found: $name"
	}.toURI()
	is SchemaSource.LocalFile -> path.toAbsolutePath().normalize().toUri()
}
