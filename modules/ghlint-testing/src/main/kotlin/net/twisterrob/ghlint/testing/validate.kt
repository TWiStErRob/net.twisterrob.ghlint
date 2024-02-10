package net.twisterrob.ghlint.testing

import io.kotest.assertions.withClue
import io.kotest.matchers.collections.atLeastSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldHave
import io.kotest.matchers.shouldNot
import net.twisterrob.ghlint.analysis.Validator
import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.FileLocation
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Rule
import org.intellij.lang.annotations.Language
import io.kotest.matchers.string.beEmpty as beEmptyString

public fun validate(
	@Language("yaml") yml: String,
	fileName: String = "test.yml",
): List<Finding> {
	val file = File(FileLocation(fileName), yml)
	return Validator().validateWorkflows(listOf(file))
}

public inline fun <reified T : Rule> validate(issue: Issue) {
	withClue("Issue ${issue.id} description") {
		issue.title shouldNot beEmptyString()
	}
	withClue("Issue ${issue.id} reasoning") {
		issue.description shouldNot beEmptyString()
	}
	withClue("Issue ${issue.id} compliant examples") {
		issue.compliant shouldHave atLeastSize(1)
		issue.compliant.forEachIndexed { index, example ->
			withClue("${issue.id} compliant example #${index + 1}:\n${example.content}") {
				validate(example.content) should beEmpty()
				check<T>(example.content) shouldNot haveFinding(issue.id)
			}
		}
	}
	withClue("Issue ${issue.id} non-compliant examples") {
		issue.nonCompliant shouldHave atLeastSize(1)
		issue.nonCompliant.forEachIndexed { index, example ->
			withClue("${issue.id} non-compliant example #${index + 1}:\n${example.content}") {
				validate(example.content) should beEmpty()
				check<T>(example.content) should haveFinding(issue.id)
			}
		}
	}
}
