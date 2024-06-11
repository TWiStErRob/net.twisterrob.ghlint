package net.twisterrob.ghlint.model

import io.kotest.matchers.maps.beEmpty
import io.kotest.matchers.maps.haveSize
import io.kotest.matchers.should
import net.twisterrob.ghlint.testing.load
import net.twisterrob.ghlint.testing.loadUnsafe
import net.twisterrob.ghlint.testing.workflow
import org.junit.jupiter.api.Test

class SnakeWorkflowTest {

	private val File.theWorkflow: Workflow
		get() = this.content as Workflow

	@Test fun `zero jobs`() {
		val file = workflow(
			"""
				on: push
				jobs: {}
			""".trimIndent()
		)
		val workflow = loadUnsafe(file).theWorkflow

		workflow.jobs should beEmpty()
	}

	@Test fun `has jobs`() {
		val file = workflow(
			"""
				on: push
				jobs:
				  job1:
				    uses: reusable/workflow.yml
				  job2:
				    uses: reusable/workflow.yml
			""".trimIndent(),
		)
		val workflow = load(file).theWorkflow

		workflow.jobs should haveSize(2)
	}
}
