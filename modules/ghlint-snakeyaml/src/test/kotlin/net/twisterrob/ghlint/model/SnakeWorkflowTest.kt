package net.twisterrob.ghlint.model

import io.kotest.matchers.maps.beEmpty
import io.kotest.matchers.maps.haveSize
import io.kotest.matchers.should
import net.twisterrob.ghlint.testing.load
import net.twisterrob.ghlint.testing.loadUnsafe
import org.junit.jupiter.api.Test

class SnakeWorkflowTest {

	private val File.theWorkflow: Workflow
		get() = this.content as Workflow

	@Test fun `zero jobs`() {
		val workflow = loadUnsafe(
			"""
				on: push
				jobs: {}
			""".trimIndent()
		).theWorkflow

		workflow.jobs should beEmpty()
	}

	@Test fun `has jobs`() {
		val workflow = load(
			"""
				on: push
				jobs:
				  job1:
				    uses: reusable/workflow.yml
				  job2:
				    uses: reusable/workflow.yml
			""".trimIndent()
		).theWorkflow

		workflow.jobs should haveSize(2)
	}
}
