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
		fun `strips extension`(fileName: String) {
			val wf = mock<Workflow>()
			whenever(wf.parent).thenReturn(Yaml.from(FileLocation(fileName), ""))

			wf.id shouldBe "file-name"
		}

		@ParameterizedTest
		@CsvSource(
			"file-name.yml.yaml, file-name.yml",
			"file-name.yml.yml, file-name.yml",
			"file-name.yaml.yml, file-name.yaml",
			"file-name.yaml.yaml, file-name.yaml",
		)
		fun `keeps duplicate extension`(fileName: String, expectedId: String) {
			val wf = mock<Workflow>()
			whenever(wf.parent).thenReturn(Yaml.from(FileLocation(fileName), ""))

			wf.id shouldBe expectedId
		}
	}
}
