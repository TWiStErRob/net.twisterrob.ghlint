package net.twisterrob.ghlint.rule

import org.intellij.lang.annotations.Language

/**
 * Describes an issue found by a rule.
 *
 * For a template to create an issue, see [ExampleRule].
 */
@Suppress("detekt.UnnecessaryAnnotationUseSiteTarget") // TODEL https://github.com/detekt/detekt/issues/8212
public class Issue(
	public val id: String,
	@param:Language("markdown")
	public val title: String,
	@param:Language("markdown")
	public val description: String,
	public val compliant: List<Example>,
	public val nonCompliant: List<Example>,
)

@Suppress("detekt.UnnecessaryAnnotationUseSiteTarget") // TODEL https://github.com/detekt/detekt/issues/8212
public class Example(
	public val path: String = "example.yml",
	@param:Language("yaml")
	public val content: String,
	@param:Language("markdown")
	public val explanation: String,
)
