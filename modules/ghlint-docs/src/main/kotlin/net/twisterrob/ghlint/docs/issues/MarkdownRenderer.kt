package net.twisterrob.ghlint.docs.issues

import net.twisterrob.ghlint.analysis.Analyzer
import net.twisterrob.ghlint.model.FileLocation
import net.twisterrob.ghlint.model.RawFile
import net.twisterrob.ghlint.model.wholeFile
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.rule.Example
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Rule
import net.twisterrob.ghlint.ruleset.RuleSet
import net.twisterrob.ghlint.yaml.SnakeYaml
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
			append(renderIssueDescription(rule, issue))
		}

	private fun renderIssueDescription(rule: Rule, issue: Issue): String =
		buildString {
			appendLine(issue.description)
			renderExamples(rule = rule, issue = issue, examples = issue.compliant, type = "Compliant")
			renderExamples(rule = rule, issue = issue, examples = issue.nonCompliant, type = "Non-compliant")
		}
}

private fun StringBuilder.renderExamples(rule: Rule, issue: Issue, examples: List<Example>, type: String) {
	if (examples.isNotEmpty()) {
		appendLine() // Add a line between description and example heading.
		appendLine("## ${type} ${if (examples.size > 1) "examples" else "example"}")
		examples.forEachIndexed { index, example ->
			if (examples.size != 1) {
				appendLine()
				appendLine("### ${type} example #${index + 1}")
			}
			appendLine(example.explanation)
			appendLine() // Add a line between explanation and example.
			appendLine(buildString {
				append("```yaml\n")
				append(example.content)
				append("\n```")
			}.prependIndent("> "))
			renderFindings(rule.calculateFindings(issue, example))
		}
	}
}

private fun Rule.calculateFindings(issue: Issue, example: Example): List<Finding> {
	val exampleFile = SnakeYaml.load(RawFile(FileLocation("example.yml"), example.content))
	val exampleRuleSet = object : RuleSet {
		override val id: String = "example"
		override val name: String = "Example"
		override fun createRules(): List<Rule> = listOf(this@calculateFindings)
	}
	val findings =
		try {
			Analyzer().analyze(listOf(exampleFile), listOf<RuleSet>(exampleRuleSet))
		} catch (@Suppress("detekt.TooGenericExceptionCaught") e: Exception) {
			// TooGenericExceptionCaught: Catch all exceptions to prevent the whole process from failing.
			listOf(
				Finding(
					rule = this,
					issue = issue,
					location = exampleFile.wholeFile,
					message = @Suppress("detekt.StringShouldBeRawString") "\n```\n${e.message ?: "null"}\n```\n"
				)
			)
		}
	return findings.filter { it.issue == issue || it.issue.id == "RuleErrored" }
}

// REPORT False negative detekt.NestedBlockDepth: invert guard if, and inline this function.
private fun StringBuilder.renderFindings(findings: List<Finding>) {
	if (findings.isEmpty()) return
	appendLine(">") // Follow the code block's quote.
	findings.forEach { finding ->
		val bullet = "**Line ${finding.location.start.line.number}**: ${finding.message}"
		append("> - ")
		appendLine(bullet.lineSequence().first())
		bullet.lineSequence().drop(1).forEach { line ->
			append(">    ") // mkdocs requires 4 indentation of bullets.
			if (line.isNotEmpty()) {
				appendLine(line)
			} else {
				// This prevents separating the bullet from the rest of the content.
				appendLine("<br/>")
			}
		}
	}
}
