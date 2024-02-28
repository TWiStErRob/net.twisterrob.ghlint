package net.twisterrob.ghlint.model

import net.twisterrob.ghlint.results.ColumnNumber
import net.twisterrob.ghlint.results.LineNumber
import net.twisterrob.ghlint.results.Location
import net.twisterrob.ghlint.results.Position
import org.intellij.lang.annotations.Language

public class Yaml private constructor(
	public val raw: String,
) : Content {

	@Suppress("LateinitUsage") // Can't figure out a better way.
	public lateinit var parent: File
		private set

	override val location: Location by lazy {
		Location(
			parent.location,
			Position(LineNumber(0), ColumnNumber(0)),
			Position(LineNumber(0), ColumnNumber(0)),
		)
	}

	public companion object {

		public fun from(fileLocation: FileLocation, @Language("yaml") yaml: String): File {
			val content = Yaml(raw = yaml)
			val file = File(fileLocation, content)
			content.parent = file
			return file
		}
	}
}
