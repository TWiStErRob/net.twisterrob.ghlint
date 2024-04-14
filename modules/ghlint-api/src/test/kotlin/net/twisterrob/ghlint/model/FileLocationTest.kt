package net.twisterrob.ghlint.model

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import java.nio.file.Path

class FileLocationTest {

	@Nested
	inner class `name Test` {

		@CsvSource(
			"a/b/c, c",
			"./a/b/c, c",
			"../a/b/c, c",
			"a/b/c/, c",
			"a/b/c.d, c.d",
			"a/b/c d.e, c d.e",
			"a b/c d.e, c d.e",
		)
		@ParameterizedTest
		fun test(path: String, name: String) {
			val subject = FileLocation(path)

			subject.name shouldBe name
		}
	}

	@Nested
	inner class `inferType Test` {

		@ValueSource(
			strings = [
				"workflow.yml",
				"workflow.yaml",
				".github/workflows/action.yaml",
				".github/workflows/action.yml",
			]
		)
		@ParameterizedTest fun workflows(fileName: String, @TempDir temp: Path) {
			val subject = fileAt(temp.resolve(fileName))

			val result = subject.inferType()

			result shouldBe FileType.WORKFLOW
		}

		@ValueSource(
			strings = [
				"action.yml",
				"action.yaml",
				".github/workflows/my-action/action.yml",
				".github/workflows/my-action/action.yaml",
				".github/actions/my-action/action.yml",
				".github/actions/my-action/action.yaml",
				"actions/my-action/action.yml",
				"actions/my-action/action.yaml",
				"actions/subfolder/my-action/action.yml",
				"actions/subfolder/my-action/action.yaml",
				"ACTION.YML",
				"ACTION.YAML",
				"Action.Yaml",
			]
		)
		@ParameterizedTest fun actions(fileName: String, @TempDir temp: Path) {
			val subject = fileAt(temp.resolve(fileName))

			val result = subject.inferType()

			result shouldBe FileType.ACTION
		}

		@ValueSource(
			strings = [
				"README.md",
				"github/workflows/README.md",
			]
		)
		@ParameterizedTest fun unknowns(fileName: String, @TempDir temp: Path) {
			val subject = fileAt(temp.resolve(fileName))

			val result = subject.inferType()

			result shouldBe FileType.UNKNOWN
		}

		private fun fileAt(path: Path): FileLocation =
			FileLocation(path.toString())
	}
}
