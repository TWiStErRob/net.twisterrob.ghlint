package net.twisterrob.ghlint.cli

import net.twisterrob.ghlint.rule.Example
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rules.DefaultRuleSet

internal class RuleHelpService {

	private val issueMap: Map<String, Issue> by lazy {
		DefaultRuleSet()
			.createRules()
			.flatMap { rule -> rule.issues }
			.associateBy { issue -> issue.id }
	}

	fun getRuleHelp(ruleId: String): String? {
		val issue = issueMap[ruleId] ?: return null
		return formatIssueHelp(issue)
	}

	private fun formatIssueHelp(issue: Issue): String = buildString {
		appendLine("# ${issue.id}")
		appendLine()
		appendLine(issue.title)
		appendLine()

		appendLine("## Description")
		appendLine(issue.description)
		appendLine()

		if (issue.compliant.isNotEmpty()) {
			appendLine("## Compliant examples")
			issue.compliant.forEachIndexed { index, example ->
				appendLine()
				formatExample(example, index + 1)
			}
			appendLine()
		}

		if (issue.nonCompliant.isNotEmpty()) {
			appendLine("## Non-compliant examples")
			issue.nonCompliant.forEachIndexed { index, example ->
				appendLine()
				formatExample(example, index + 1)
			}
		}
	}.trim()

	private fun StringBuilder.formatExample(example: Example, index: Int) {
		appendLine("### Example $index")
		if (example.explanation.isNotBlank()) {
			appendLine(example.explanation)
			appendLine()
		}
		appendLine("```yaml")
		appendLine("# ${example.path}")
		appendLine(example.content)
		appendLine("```")
	}
}
