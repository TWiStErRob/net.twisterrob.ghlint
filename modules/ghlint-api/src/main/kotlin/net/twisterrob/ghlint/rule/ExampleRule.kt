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
			id = "IssueName",
			title = "Object is verb subject.",
			description = """
				Persuasive sentence about why this is a problem.
				More details about the problem.
			    More details about the fix.
			    List benefits.
			""".trimIndent(),
			compliant = listOf(
				Example(
					explanation = "Describe what to focus on succinctly.",
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
					explanation = "Describe what to focus on succinctly.",
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
