package net.twisterrob.ghlint.cli

import net.twisterrob.ghlint.rule.Example
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Rule
import net.twisterrob.ghlint.rules.DefaultRuleSet

internal class RuleHelpService {

	private val allRules: List<Rule> by lazy {
		DefaultRuleSet().createRules()
	}

	fun getRuleHelp(ruleId: String): String? {
		val issue = findIssueById(ruleId) ?: return null
		return formatIssueHelp(issue)
	}

	private fun findIssueById(ruleId: String): Issue? {
		for (rule in allRules) {
			rule.issues.forEach { issue ->
				if (issue.id == ruleId) {
					return issue
				}
			}
		}
		return null
	}

	private fun formatIssueHelp(issue: Issue): String {
		val sb = StringBuilder()

		sb.appendLine("# ${issue.id}")
		sb.appendLine()
		sb.appendLine(issue.title)
		sb.appendLine()

		sb.appendLine("## Description")
		sb.appendLine(issue.description)
		sb.appendLine()

		if (issue.compliant.isNotEmpty()) {
			sb.appendLine("## Compliant examples")
			issue.compliant.forEachIndexed { index, example ->
				sb.appendLine()
				formatExample(sb, example, index + 1)
			}
			sb.appendLine()
		}

		if (issue.nonCompliant.isNotEmpty()) {
			sb.appendLine("## Non-compliant examples")
			issue.nonCompliant.forEachIndexed { index, example ->
				sb.appendLine()
				formatExample(sb, example, index + 1)
			}
		}

		return sb.toString().trim()
	}

	private fun formatExample(sb: StringBuilder, example: Example, index: Int) {
		sb.appendLine("### Example $index")
		if (example.explanation.isNotBlank()) {
			sb.appendLine(example.explanation)
			sb.appendLine()
		}
		sb.appendLine("```yaml")
		sb.appendLine("# ${example.path}")
		sb.appendLine(example.content)
		sb.appendLine("```")
	}
}
