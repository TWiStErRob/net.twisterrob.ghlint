package net.twisterrob.ghlint.model

import io.kotest.matchers.maps.beEmpty
import io.kotest.matchers.maps.haveSize
import io.kotest.matchers.should
import net.twisterrob.ghlint.yaml.SnakeYaml
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

class SnakeWorkflowTest {

	@Test fun `no jobs`() {
		val workflow = load(
			"""
				jobs:
			""".trimIndent()
		)

		workflow.jobs should beEmpty()
	}

	@Test fun `has jobs`() {
		val workflow = load(
			"""
				jobs:
				  job1:
				    steps:
				  job2:
				    steps:
			""".trimIndent()
		)

		workflow.jobs should haveSize(2)
	}

	private fun load(@Language("yaml") yaml: String): Workflow {
		val yamlFile = RawFile(FileLocation("test.yml"), yaml)
		return SnakeComponentFactory().createWorkflow(yamlFile, SnakeYaml.loadRaw(yamlFile))
	}
}
