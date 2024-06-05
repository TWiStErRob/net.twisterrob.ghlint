package net.twisterrob.ghlint.analysis

import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.wholeFile
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.rule.Example
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Rule

internal class SafeRule(
	private val unsafeRule: Rule,
) : Rule {

	override val issues: List<Issue>
		get() = unsafeRule.issues + RuleErrored

	override fun check(file: File): List<Finding> {
		try {
			return unsafeRule.check(file)
		} catch (@Suppress("detekt.TooGenericExceptionCaught") ex: Throwable) {
			// detekt.TooGenericExceptionCaught: Can't know what's wrong, so we can't handle it more specifically.
			val errorFinding = Finding(
				rule = this,
				issue = RuleErrored,
				location = file.wholeFile,
				message = @Suppress("detekt.StringShouldBeRawString") // Cannot be, because we don't control stackTraceToString.
				// This stackTraceToString ends with \n and it might contain ```, so using ```` to wrap it.
				"${unsafeRule} errored while checking ${file.location.path}:\n````\n${ex.stackTraceToString()}````",
			)
			return listOf(errorFinding)
		}
	}

	override fun toString(): String = "SafeRule(${unsafeRule})"

	internal companion object {

		internal val RuleErrored = Issue(
			id = "RuleErrored",
			title = "A rule failed to check a workflow and threw an exception.",
			description = """
				An error occurred while checking the workflow.
				It came from another rule, so it's not possible to provide more details than the error stack trace.
			""".trimIndent(),
			compliant = listOf(
				Example(
					explanation = "Minimal valid workflow.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Example"
					""".trimIndent(),
				),
			),
			nonCompliant = listOf(
				Example(
					explanation = "Fake example to satisfy the validation framework.",
					// The name is used in documentation generation, see ProblematicRule.
					content = """
						name: "Invalid"
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Example"
					""".trimIndent(),
				),
			),
		)
	}
}
