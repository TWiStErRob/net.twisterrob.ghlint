package net.twisterrob.ghlint.model

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.instanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import net.twisterrob.ghlint.testing.load
import net.twisterrob.ghlint.testing.loadUnsafe
import org.junit.jupiter.api.Test

class SnakeFileTest {

	@Test fun `syntax error`() {
		val file = loadUnsafe("x: *")

		file.content.parent shouldBe file

		file.content shouldBe instanceOf<InvalidContent>()
		file.content shouldBe instanceOf<SnakeSyntaxErrorContent>()
	}

	@Test fun `valid workflow`() {
		val file = load(
			"""
				on: push
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
				on: push
				jobs:
				  test:
				    uses: reusable/workflow.yml
			""".trimIndent()
		)

		file.content shouldBeSameInstanceAs file.content
	}

	@Test fun `content is not re-created on error`() {
		val file = loadUnsafe("<invalid yaml/>")

		file.content shouldBeSameInstanceAs file.content
	}
}
