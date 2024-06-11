package net.twisterrob.ghlint.model

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.instanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import net.twisterrob.ghlint.testing.file
import net.twisterrob.ghlint.testing.load
import net.twisterrob.ghlint.testing.loadUnsafe
import net.twisterrob.ghlint.testing.workflow
import net.twisterrob.ghlint.testing.yaml
import org.junit.jupiter.api.Test

class SnakeFileTest {

	@Test fun `syntax error`() {
		val file = yaml("x: *", "file.yml")
		val loaded = loadUnsafe(file)

		loaded.content.parent shouldBe loaded

		loaded.content shouldBe instanceOf<InvalidContent>()
		loaded.content shouldBe instanceOf<SnakeSyntaxErrorContent>()
	}

	@Test fun `valid workflow`() {
		val file = workflow(
			"""
				on: push
				jobs:
				  test:
				    uses: reusable/workflow.yml
			""".trimIndent(),
			fileName = "test.yml"
		)
		val loaded = load(file)

		loaded.content shouldBe instanceOf<Workflow>()
	}

	@Test fun `content is not re-created`() {
		val file = workflow(
			"""
				on: push
				jobs:
				  test:
				    uses: reusable/workflow.yml
			""".trimIndent(),
			fileName = "test.yml"
		)
		val loaded = load(file)

		loaded.content shouldBeSameInstanceAs loaded.content
	}

	@Test fun `content is not re-created on error`() {
		val file = file("<invalid yaml/>", "file.name")
		val loaded = loadUnsafe(file)

		loaded.content shouldBeSameInstanceAs loaded.content
	}
}
