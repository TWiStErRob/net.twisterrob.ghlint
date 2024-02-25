package net.twisterrob.ghlint.docs

import net.twisterrob.ghlint.rule.Example
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Rule
import net.twisterrob.ghlint.ruleset.RuleSet
import kotlin.io.path.relativeTo

internal class MarkdownRenderer(
	private val locator: FileLocator,
) {

	fun renderRuleSet(ruleSet: RuleSet): String =
		buildString {
			appendLine(
				"""
					# Rule set "${ruleSet.name}" (`${ruleSet.id}`)
					
				""".trimIndent()
			)
			val rules = ruleSet.createRules()
			if (rules.isEmpty()) {
				appendLine("No rules.")
			}
			rules.sortedBy { it.id }.forEach { rule ->
				appendLine(" - `${rule.id}`")
				rule.issues.sortedBy { it.id }.forEach { issue ->
					val thisFolder = locator.ruleSetFile(ruleSet).parent
					val issueRelativePath = locator.issueFile(ruleSet, issue).relativeTo(thisFolder)
					appendLine("    - [`${issue.id}`](${issueRelativePath}): ${issue.title}")
				}
			}
		}

	fun renderIssue(ruleSet: RuleSet, rule: Rule, issue: Issue, relatedIssues: List<Issue>): String =
		buildString {
			val thisFolder = locator.issueFile(ruleSet, issue).parent
			val relatedIssuesText = relatedIssues.joinToString(separator = ", ") { relatedIssue ->
				val relatedIssueRelativePath = locator.issueFile(ruleSet, relatedIssue).relativeTo(thisFolder)
				"[`${relatedIssue.id}`]($relatedIssueRelativePath)"
			}
			val related = if (relatedIssuesText.isNotEmpty()) " along with ${relatedIssuesText}" else ""
			val ruleSetRelativePath = locator.ruleSetFile(ruleSet).relativeTo(thisFolder)
			appendLine(
				"""
					# `${issue.id}`
					${issue.title}
					
					_Defined by `${rule.id}` in the "[${ruleSet.name}](${ruleSetRelativePath})" ruleset${related}._
					
					## Description
				""".trimIndent()
			)
			append(issue.descriptionWithExamples)
		}
}

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
