package net.twisterrob.ghlint.docs

import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Rule
import net.twisterrob.ghlint.ruleset.RuleSet
import java.nio.file.Path
import kotlin.io.path.createParentDirectories
import kotlin.io.path.writeText

internal class Generator(
	private val target: Path,
) {

	fun generate(vararg ruleSets: RuleSet) {
		ruleSets.forEach { ruleSet ->
			ruleSet.createRules().forEach { rule ->
				rule.issues.forEach { issue ->
					generate(ruleSet, rule, issue)
				}
			}
		}
	}

	private fun generate(ruleSet: RuleSet, rule: Rule, issue: Issue) {
		val docs = generateDocs(ruleSet, rule, issue)
		val file = target.resolve(ruleSet.id).resolve("${issue.id}.md")
		file.createParentDirectories()
		file.writeText(docs)
	}

	private fun generateDocs(ruleSet: RuleSet, rule: Rule, issue: Issue): String =
		buildString {
			appendLine(
				"""
					# `${issue.id}`
					${issue.title}
					
					_Defined by `${rule.id}` in ${ruleSet.name} ruleset._
					
				""".trimIndent()
			)
			appendLine(issue.description)
		}
}

private val Rule.id: String
	get() = this::class.simpleName ?: error("Invalid rule")
