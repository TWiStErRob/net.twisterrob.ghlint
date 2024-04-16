package net.twisterrob.ghlint.rule

import org.intellij.lang.annotations.Language

/**
 * Describes an issue found by a rule.
 *
 * For a template to create an issue, see [ExampleRule].
 */
public class Issue(
	public val id: String,
	@Language("markdown")
	public val title: String,
	@Language("markdown")
	public val description: String,
	public val compliant: List<Example>,
	public val nonCompliant: List<Example>,
)

public class Example(
	public val path: String = "example.yml",
	@Language("yaml")
	public val content: String,
	@Language("markdown")
	public val explanation: String,
)
