package net.twisterrob.ghlint.model

import io.kotest.matchers.maps.shouldHaveSize
import net.twisterrob.ghlint.yaml.SnakeYaml
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

@Suppress("detekt.TrimMultilineRawString") // See load().
class SnakeWorkflowTest {

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

	private fun load(@Language("yaml") yaml: String): Workflow {
		val yamlFile = RawFile(FileLocation("test.yml"), yaml.trimIndent())
		return SnakeComponentFactory().createWorkflow(yamlFile, SnakeYaml.loadRaw(yamlFile))
	}
}
