package net.twisterrob.ghlint.model

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.instanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import net.twisterrob.ghlint.yaml.SnakeSyntaxErrorContent
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

class SnakeFileTest {

	@Test fun `syntax error`() {
		val file = load("x: *")

		file.content.parent shouldBe file

		file.content shouldBe instanceOf<InvalidContent>()
		file.content shouldBe instanceOf<SnakeSyntaxErrorContent>()
	}

	@Test fun `valid workflow`() {
		val file = load(
			"""
				on:
				jobs:
				  test:
				    uses: reusable/workflow.yml
			""".trimIndent()
		)

		file.content shouldBe instanceOf<Workflow>()
	}

	@Test fun `content is not re-created`() {
		val file = load(
			"""
				on:
				jobs:
				  test:
				    uses: reusable/workflow.yml
			""".trimIndent()
		)

		file.content shouldBeSameInstanceAs file.content
	}

	@Test fun `content is not re-created on error`() {
		val file = load("<invalid yaml/>")

		file.content shouldBeSameInstanceAs file.content
	}

	private fun load(@Language("yaml") yaml: String): File {
		val yamlFile = RawFile(FileLocation("test.yml"), yaml)
		return SnakeComponentFactory().createFile(yamlFile)
	}
}
