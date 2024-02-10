package net.twisterrob.ghlint.model

import io.kotest.matchers.maps.shouldHaveSize
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.CleanupMode
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.writeText

@Suppress("detekt.TrimMultilineRawString") // See load().
class WorkflowTest {

	@TempDir(cleanup = CleanupMode.ALWAYS)
	lateinit var tempDir: Path

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
		val file = tempDir.resolve("workflow.yml").apply { writeText(yaml.trimIndent()) }
		return Workflow.from(File(FileName(file.toRealPath().toString())))
	}
}
