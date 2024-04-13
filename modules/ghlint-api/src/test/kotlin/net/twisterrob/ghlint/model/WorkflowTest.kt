package net.twisterrob.ghlint.model

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

class WorkflowTest {

	@Nested
	inner class IdTest {

		@ParameterizedTest
		@ValueSource(
			strings = [
				"file-name",
				"file-name.yml",
				"file-name.yaml",
			]
		)
		fun `strips extension`(path: String) {
			val workflow: Workflow = createWorkflow(path)

			workflow.id shouldBe "file-name"
		}

		@ParameterizedTest
		@CsvSource(
			"file-name.yml.yaml, file-name.yml",
			"file-name.yml.yml, file-name.yml",
			"file-name.yaml.yml, file-name.yaml",
			"file-name.yaml.yaml, file-name.yaml",
		)
		fun `keeps duplicate extension`(path: String, expectedId: String) {
			val workflow: Workflow = createWorkflow(path)

			workflow.id shouldBe expectedId
		}

		@ParameterizedTest
		@CsvSource(
			"folder/file-name.yaml, file-name",
			".github/workflows/file-name, file-name",
			".github/workflows/file-name.yml, file-name",
			".github/workflows/file-name.yaml.yml, file-name.yaml",
			"/other/folder/file-name.yaml, file-name",
			"../super/folder/file-name.yml, file-name",
		)
		fun `path doesn't play in ID`(path: String, expectedId: String) {
			val workflow: Workflow = createWorkflow(path)

			workflow.id shouldBe expectedId
		}

		private fun createWorkflow(path: String): Workflow {
			val workflow: Workflow = mock()
			val content: Content = mock<InvalidContent>()
			whenever(workflow.parent).thenReturn(File(FileLocation(path), content))
			return workflow
		}
	}
}
