package net.twisterrob.ghlint.model

public class Finding(
	public val rule: Rule,
	public val issue: Issue,
	public val location: Location,
) {

	public companion object
}

public class Location(
	public val file: FileName,
	public val start: Position,
	public val end: Position,
) {

	public companion object
}

public class Position(
	public val line: LineNumber,
	public val column: ColumnNumber,
) {

	public companion object
}

@JvmInline
public value class LineNumber(
	public val number: Int,
) {

	public companion object
}

@JvmInline
public value class ColumnNumber(
	public val number: Int,
) {

	public companion object
}
