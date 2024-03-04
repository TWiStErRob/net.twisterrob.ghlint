package net.twisterrob.ghlint.testing

import io.kotest.assertions.withClue
import io.kotest.matchers.collections.atLeastSize
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.shouldHave
import io.kotest.matchers.shouldNot
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.string.shouldNotStartWith
import net.twisterrob.ghlint.analysis.JsonSchemaRuleSet
import net.twisterrob.ghlint.model.FileLocation
import net.twisterrob.ghlint.model.RawFile
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Rule
import net.twisterrob.ghlint.yaml.SnakeYaml
import org.intellij.lang.annotations.Language
import io.kotest.matchers.string.beEmpty as beEmptyString

public fun validate(
	@Language("yaml") yml: String,
	fileName: String = "test.yml",
): List<Finding> {
	val file = SnakeYaml.load(RawFile(FileLocation(fileName), yml))
	return JsonSchemaRuleSet().createRules().flatMap { it.check(file) }
}

public inline fun <reified T : Rule> validate(issue: Issue) {
	val rule = createRule<T>()
	validate(rule, issue)
}

public fun validate(rule: Rule, issue: Issue) {
	validateIssueTitle(issue)
	validateIssueDescription(issue)
	rule.validateCompliantExamples(issue)
	rule.validateNonCompliantExamples(issue)
}

internal fun validateIssueTitle(issue: Issue) {
	withClue("Issue ${issue.id} title") {
		issue.title shouldNot beEmptyString()
		issue.title shouldNotStartWith "TODO"
	}
}

internal fun validateIssueDescription(issue: Issue) {
	withClue("Issue ${issue.id} description") {
		issue.description shouldNot beEmptyString()
		// REPORT missing shouldNotMatch overload.
		withClue("contains TODO") {
			val todoRegex = Regex("""^TODO""", setOf(RegexOption.MULTILINE, RegexOption.IGNORE_CASE))
			issue.description shouldNotContain todoRegex
		}
	}
}

internal fun Rule.validateNonCompliantExamples(issue: Issue) {
	withClue("Issue ${issue.id} non-compliant examples") {
		issue.nonCompliant shouldHave atLeastSize(1)
		issue.nonCompliant.forEachIndexed { index, example ->
			withClue("${issue.id} non-compliant example #${index + 1}:\n${example.content}") {
				val findings = validate(example.content, "non-compliant.yml") +
						check(example.content, "non-compliant.yml")
				findings shouldHave onlyFindings(issue.id)
				example.explanation shouldNot beEmptyString()
				example.explanation shouldNotStartWith "TODO"
			}
		}
	}
}

internal fun Rule.validateCompliantExamples(issue: Issue) {
	withClue("Issue ${issue.id} compliant examples") {
		issue.compliant shouldHave atLeastSize(1)
		issue.compliant.forEachIndexed { index, example ->
			withClue("${issue.id} compliant example #${index + 1}:\n${example.content}") {
				validate(example.content, "compliant.yml") shouldHave noFindings()
				check(example.content, "compliant.yml") shouldHave noFindings()
				example.explanation shouldNot beEmptyString()
				example.explanation shouldNotStartWith "TODO"
			}
		}
	}
}

public fun validate(rule: Rule) {
	rule.issues shouldNot beEmpty()
	rule.issues.forEach { issue ->
		validate(rule, issue)
	}
}
