package net.twisterrob.ghlint.rule

import org.intellij.lang.annotations.Language

public class Issue(
	public val id: String,
	@Language("markdown")
	public val title: String,
	@Language("markdown")
	public val description: String = "", // STOPSHIP
	public val compliant: List<Example> = emptyList(), // STOPSHIP
	public val nonCompliant: List<Example> = emptyList(), // STOPSHIP
)

public class Example(
	@Language("yaml")
	public val content: String,
	@Language("markdown")
	public val explanation: String,
)
