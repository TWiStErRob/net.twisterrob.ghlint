package net.twisterrob.ghlint.model

import io.kotest.matchers.maps.shouldHaveSize
import net.twisterrob.ghlint.yaml.Yaml
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

class WorkflowTest {

	@Test fun `no jobs`() {
		val workflow = load(
			"""
				jobs:
			"""
		)

		workflow.jobs shouldHaveSize 0
	}

	@Test fun `has jobs`() {
		val workflow = load(
			"""
				jobs:
				  job1:
				    steps:
				  job2:
				    steps:
			"""
		)

		workflow.jobs shouldHaveSize 2
	}

	private fun load(@Language("yaml") yaml: String): Workflow =
		Workflow.from(Yaml.load(yaml.trimIndent()))
}
