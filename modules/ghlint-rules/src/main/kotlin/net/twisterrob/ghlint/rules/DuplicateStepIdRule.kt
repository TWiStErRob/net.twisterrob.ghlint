package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.Job
import net.twisterrob.ghlint.rule.Example
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.VisitorRule
import net.twisterrob.ghlint.rule.report
import net.twisterrob.ghlint.rules.utils.editDistance

public class DuplicateStepIdRule : VisitorRule {

	override val issues: List<Issue> = listOf(DuplicateStepId, SimilarStepId)

	override fun visitNormalJob(reporting: Reporting, job: Job.NormalJob) {
		super.visitNormalJob(reporting, job)
		val ids = job.steps.mapNotNull { it.id }
		val similar: Sequence<Triple<String, String, Int>> = ids
			.combinations()
			.map { (id1, id2) -> if (id1 < id2) id1 to id2 else id2 to id1 }
			.map { Triple(it.first, it.second, editDistance(it.first, it.second)) }
			.filter { it.third <= MAX_SHELLS_ON_STEPS }

		similar.forEach { (id1, id2, distance) ->
			if (distance == 0) {
				reporting.report(DuplicateStepId, job) { "${it} has the `${id1}` step identifier multiple times." }
			} else {
				reporting.report(SimilarStepId, job) { "${it} has similar step identifiers: `${id1}` and `${id2}`." }
			}
		}
	}

	private companion object {

		private const val MAX_SHELLS_ON_STEPS = 3

		val DuplicateStepId = Issue(
			id = "DuplicateStepId",
			title = "Steps must have unique identifiers within a job.",
			description = """
				Multiple steps having the same identifier makes them unreferenceable.
				The `id:` defined on a step becomes a key in the `steps` context.
				If multiple steps have the same identifier, only one of them will be accessible.
				Remove the one that is not needed, or rename it to be unique.
				
				Relevant documentation:
				
				 * [`jobs.<job_id>.steps[*].id`](https://docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions#jobsjob_idstepsid)
				 * [`steps` context](https://docs.github.com/en/actions/learn-github-actions/contexts#steps-context)
			""".trimIndent(),
			compliant = listOf(
				Example(
					explanation = "Each step has a unique identifier or no identifier.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Example"
						        id: my-step-id
						      - run: echo "Example"
						      - run: echo "Example"
						        id: my-other-step-id
					""".trimIndent(),
				),
			),
			nonCompliant = listOf(
				Example(
					explanation = """
						Multiple steps have the same identifier.
						
						This is very likely a copy-paste mistake.
					""".trimIndent(),
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Example"
						        id: my-step-id
						      - run: echo "Example"
						        id: my-step-id
					""".trimIndent(),
				),
			),
		)

		val SimilarStepId = Issue(
			id = "SimilarStepId",
			title = "Steps should have distinguishable identifiers within a job.",
			description = """
				Multiple steps having very similar identifiers makes them hard to distinguish.
				
				It's hard to read and understand a workflow when steps have similar identifiers,
				this is especially important for people with dyslexia or ADHD.
				
				The `id:` defined on a step becomes a key in the `steps` context,
				so it's easy to mistakenly reference another unintended one.
			""".trimIndent(),
			compliant = listOf(
				Example(
					explanation = "Each step has a distinguishable identifier or no identifier.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Example"
						        id: my-step-id
						      - run: echo "Example"
						      - run: echo "Example"
						        id: my-other-step-id
					""".trimIndent(),
				),
			),
			nonCompliant = listOf(
				Example(
					explanation = "Identifier of the two steps are very similar.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Example"
						        id: my-step-id
						      - run: echo "Example"
						        id: wy-step-id
					""".trimIndent(),
				),
			),
		)
	}
}

private fun List<String>.combinations(): Sequence<Pair<String, String>> =
	sequence {
		for (index1 in 0 until this@combinations.size) {
			for (index2 in index1 + 1 until this@combinations.size) {
				yield(this@combinations[index1] to this@combinations[index2])
			}
		}
	}
