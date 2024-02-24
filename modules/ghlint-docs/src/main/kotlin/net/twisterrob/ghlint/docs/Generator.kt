package net.twisterrob.ghlint.docs

import net.twisterrob.ghlint.ruleset.RuleSet
import kotlin.io.path.writeText

internal class Generator(
	private val locator: FileLocator,
	private val renderer: MarkdownRenderer,
) {

	fun generate(vararg ruleSets: RuleSet) {
		ruleSets.forEach { ruleSet ->
			locator.ruleSetFile(ruleSet).writeText(
				renderer.renderRuleSet(
					ruleSet = ruleSet
				)
			)
			ruleSet.createRules().forEach { rule ->
				rule.issues.forEach { issue ->
					locator.issueFile(ruleSet, issue).writeText(
						renderer.renderIssue(
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
}
