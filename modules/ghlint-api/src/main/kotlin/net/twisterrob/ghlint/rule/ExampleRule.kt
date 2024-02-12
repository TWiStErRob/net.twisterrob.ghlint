package net.twisterrob.ghlint.rule

import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.results.Finding

private class ExampleRule : Rule {

	override val issues: List<Issue> = listOf(IssueName)

	override fun check(workflow: Workflow): List<Finding> {
		TODO("Implement, or extend VisitorRule and override a function.")
	}

	companion object {

		val IssueName = Issue(
			id = "TODO IssueName",
			title = "TODO Object is verb subject.",
			description = """
				TODO Persuasive sentence about why this is a problem.
				TODO More details about the problem.
				TODO More details about the fix.
				TODO List benefits.
			""".trimIndent(),
			compliant = listOf(
				Example(
					explanation = "TODO Describe what to focus on succinctly.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Example"
					""".trimIndent()
				)
			),
			nonCompliant = listOf(
				Example(
					explanation = "TODO Describe what to focus on succinctly.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Example"
					""".trimIndent()
				)
			)
		)
	}
}
