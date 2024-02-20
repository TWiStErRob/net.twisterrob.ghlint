package net.twisterrob.ghlint.docs

import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Rule
import net.twisterrob.ghlint.rule.descriptionWithExamples
import net.twisterrob.ghlint.ruleset.RuleSet
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

internal class Generator(
	private val target: Path,
) {

	fun generate(vararg ruleSets: RuleSet) {
		ruleSets.forEach { ruleSet ->
			val ruleSetFolder = target.resolve(ruleSet.id).apply { createDirectories() }
			val ruleSetFile = ruleSetFolder.resolve("index.md")
			ruleSetFile.writeText(
				generateDocs(
					ruleSet = ruleSet
				)
			)
			ruleSet.createRules().forEach { rule ->
				rule.issues.forEach { issue ->
					val issueFile = ruleSetFolder.resolve(issue.fileName)
					issueFile.writeText(
						generateDocs(
							ruleSet = ruleSet,
							rule = rule,
							issue = issue,
							relatedIssues = rule.issues - issue
						)
					)
				}
			}
		}
	}

	private fun generateDocs(ruleSet: RuleSet): String =
		buildString {
			appendLine(
				"""
					# Rule set "${ruleSet.name}" (`${ruleSet.id}`)
					
				""".trimIndent()
			)
			ruleSet.createRules().sortedBy { it.id }.forEach { rule ->
				appendLine(" - `${rule.id}`")
				rule.issues.sortedBy { it.id }.forEach { issue ->
					appendLine("    - [`${issue.id}`](${issue.fileName}): ${issue.title}")
				}
			}
		}

	private fun generateDocs(ruleSet: RuleSet, rule: Rule, issue: Issue, relatedIssues: List<Issue>): String =
		buildString {
			val relatedIssuesText = relatedIssues.joinToString(separator = ", ") { "[`${it.id}`](${it.fileName})" }
			val related = if (relatedIssuesText.isNotEmpty()) " along with ${relatedIssuesText}" else ""
			appendLine(
				"""
					# `${issue.id}`
					${issue.title}
					
					_Defined by `${rule.id}` in the "[${ruleSet.name}](../)" ruleset${related}._
					
					## Description
				""".trimIndent()
			)
			appendLine(issue.descriptionWithExamples)
		}
}

private val Rule.id: String
	get() = this::class.simpleName ?: error("Invalid rule")

private val Issue.fileName: String
	get() = this.id + ".md"
