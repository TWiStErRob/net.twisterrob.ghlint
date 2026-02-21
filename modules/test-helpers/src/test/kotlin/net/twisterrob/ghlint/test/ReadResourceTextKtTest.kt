package net.twisterrob.ghlint.test

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import org.junit.jupiter.api.Test

/**
 * @see readResourceText
 */
class ReadResourceTextKtTest {

	@Test fun `reads relative resource`() {
		val content = ReadResourceTextKtTest::class.java
			.readResourceText("some/resource.txt")

		content shouldBe "test content\n"
	}

	@Test fun `reads absolute resource`() {
		val content = ReadResourceTextKtTest::class.java
			.readResourceText("/net/twisterrob/ghlint/test/some/resource.txt")

		content shouldBe "test content\n"
	}

	@Test fun `throws when resource not found`() {
		val failure = shouldThrow<IllegalStateException> {
			ReadResourceTextKtTest::class.java.readResourceText("non/existent.txt")
		}

		failure shouldHaveMessage "Cannot find resource: non/existent.txt"
	}
}
