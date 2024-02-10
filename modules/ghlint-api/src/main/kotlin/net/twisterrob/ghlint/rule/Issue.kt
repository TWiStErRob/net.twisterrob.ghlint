package net.twisterrob.ghlint.rule

import org.intellij.lang.annotations.Language

public class Issue(
	public val id: String,
	public val description: String,
	public val reasoning: String = "", // STOPSHIP
	public val compliant: List<Example> = emptyList(), // STOPSHIP
	public val nonCompliant: List<Example> = emptyList(), // STOPSHIP
)

public class Example(
	@Language("yaml")
	public val content: String,
	public val explanation: String? = null,
)
