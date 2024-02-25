package net.twisterrob.ghlint.reporting.sarif

import net.twisterrob.ghlint.rule.Example
import net.twisterrob.ghlint.rule.Issue

internal fun Issue.helpMarkdown(): String =
	descriptionWithExamples + helpLink

private val Issue.helpLink: String
	get() = "\n---\nSee also the [online documentation](https://ghlint.twisterrob.net/issues/default/${id}/)."

private val Issue.descriptionWithExamples: String
	get() = buildString {
		append(description)
		append("\n")
		renderExamples("Compliant", compliant)
		renderExamples("Non-compliant", nonCompliant)
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
