package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.Job
import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.rule.Example
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.report
import net.twisterrob.ghlint.rule.visitor.VisitorRule
import net.twisterrob.ghlint.rule.visitor.WorkflowVisitor

public class JobDependenciesRule : VisitorRule, WorkflowVisitor {

	override val issues: List<Issue> = listOf(MissingNeedsJob, JobDependencyCycle)

	override fun visitWorkflow(reporting: Reporting, workflow: Workflow) {
		super.visitWorkflow(reporting, workflow)
		val jobDependencies = buildJobGraph(workflow.jobs) { job, needed ->
			reporting.report(MissingNeedsJob, job) {
				"${it} references Job[${needed}], which does not exist."
			}
		}
		val cycle = Traversal(jobDependencies).findACycle()
		if (cycle.isNotEmpty()) {
			reporting.report(JobDependencyCycle, cycle.first()) { job ->
				"${job} forms a dependency cycle: [${cycle.joinToString { it.id }}]."
			}
		}
	}

	private companion object {

		val MissingNeedsJob = Issue(
			id = "MissingNeedsJob",
			title = "Needs references a missing job.",
			description = """
				Referencing a job that does not exist is an error.
				Not all dependencies can be satisfied and therefore the workflow cannot be started.
				Make sure all the jobs referenced in `needs:` exist in the workflow.
				
				GitHub may give an error similar to this:
				> The workflow is not valid. `.github/workflows/???.yml` (Line: ?, Col: ?):
				> Job '???' depends on unknown job '???'.
			""".trimIndent(),
			compliant = listOf(
				Example(
					explanation = "`example2` refers to `example1` as a dependency.",
					content = """
						name: "My Workflow"
						on: push
						jobs:
						  example1:
						    name: "My Job"
						    uses: reusable/workflow.yml
						  example2:
						    name: "My Job"
						    needs: example1
						    uses: reusable/workflow.yml
					""".trimIndent(),
				),
			),
			nonCompliant = listOf(
				Example(
					explanation = "`example2` refers to a non-existent `example3` as a dependency.",
					content = """
						name: "My Workflow"
						on: push
						jobs:
						  example1:
						    name: "My Job"
						    uses: reusable/workflow.yml
						  example2:
						    name: "My Job"
						    needs: example3
						    uses: reusable/workflow.yml
					""".trimIndent(),
				),
			),
		)

		val JobDependencyCycle = Issue(
			id = "JobDependencyCycle",
			title = "Cycle in job dependencies (needs).",
			description = """
				Having a cycle in the job dependency graph is an error.
				There must be a starting point in the workflow to start the workflow run.
				
				GitHub may give an error similar to this:
				> The workflow is not valid. `.github/workflows/???.yml` (Line: ?, Col: ?):
				> The workflow must contain at least one job with no dependencies.
			""".trimIndent(),
			compliant = listOf(
				Example(
					explanation = "Jobs don't have dependencies.",
					content = """
						name: "My Workflow"
						on: push
						jobs:
						  example:
						    name: "My Job"
						    uses: reusable/workflow.yml
					""".trimIndent(),
				),
				Example(
					explanation = "There's a job to start with (`example1`).",
					content = """
						name: "My Workflow"
						on: push
						jobs:
						  example1:
						    name: "My Job"
						    uses: reusable/workflow.yml
						  example2:
						    name: "My Job"
						    needs: example1
						    uses: reusable/workflow.yml
					""".trimIndent(),
				),
			),
			nonCompliant = listOf(
				Example(
					explanation = "Circular dependency between jobs.",
					content = """
						name: "My Workflow"
						on: push
						jobs:
						  example1:
						    name: "My Job 1 -> 2"
						    needs: example2
						    uses: reusable/workflow.yml
						  example2:
						    name: "My Job 2 -> 1"
						    needs: example1
						    uses: reusable/workflow.yml
					""".trimIndent(),
				),
			),
		)
	}
}

private fun buildJobGraph(jobs: Map<String, Job>, missingNeeds: (Job, String) -> Unit): Map<Job, Set<Job>> {
	val graph: MutableMap<Job, MutableSet<Job>> = mutableMapOf()
	jobs.values.forEach { job ->
		graph.getOrPut(job) { mutableSetOf() }
		job.needs.orEmpty().forEach { neededId ->
			val needed = jobs[neededId]
			if (needed != null) {
				graph.getOrPut(needed) { mutableSetOf() }.add(job)
			} else {
				missingNeeds(job, neededId)
			}
		}
	}
	return graph
}

private class Traversal<T>(
	private val graph: Map<T, Set<T>>,
) {

	private val visiting = mutableSetOf<T>()
	private val visited = mutableSetOf<T>()

	fun findACycle(): List<T> {
		graph.keys.forEach { job ->
			if (dfs(job)) {
				return visiting.toList()
			}
		}
		return emptyList()
	}

	@Suppress("detekt.ReturnCount") // Required to break the algorithm's flow.
	private fun dfs(node: T): Boolean {
		if (node in visited) {
			return false
		}
		if (node in visiting) {
			return true
		}
		visiting += node
		graph[node].orEmpty().forEach { neighbour ->
			if (dfs(neighbour)) {
				return true
			}
		}
		visiting -= node
		visited += node
		return false
	}
}
