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

public val Issue.descriptionWithExamples: String
	get() = buildString {
		append(description)
		append("\n")
		renderExamples("Compliant", compliant)
		renderExamples("Non-compliant", compliant)
	}

private fun StringBuilder.renderExamples(type: String, examples: List<Example>) {
	if (examples.isNotEmpty()) {
		append("\n## ${type} ${if (examples.size > 1) "examples" else "example"}\n")
		examples.forEachIndexed { index, example ->
			if (examples.size != 1) {
				append("\n### ${type} example #${index + 1}\n")
			}
			append("```yaml\n")
			append(example.content)
			append("\n```\n")
			append(example.explanation)
			append("\n")
		}
	}
}
