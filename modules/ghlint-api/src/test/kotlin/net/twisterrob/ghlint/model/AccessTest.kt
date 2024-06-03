package net.twisterrob.ghlint.model

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import org.junit.jupiter.api.Test

class AccessTest {

	@Test
	fun `enum values match expected strings`() {
		Access.NONE.value shouldBe "none"
		Access.READ.value shouldBe "read"
		Access.WRITE.value shouldBe "write"
	}

	@Test
	fun `fromString returns correct enum`() {
		Access.fromString("none") shouldBe Access.NONE
		Access.fromString("read") shouldBe Access.READ
		Access.fromString("write") shouldBe Access.WRITE
	}

	@Test
	fun `fromString throws for unknown value`() {
		val exception = shouldThrow<IllegalArgumentException> {
			Access.fromString("admin")
		}
		exception shouldHaveMessage "Unknown access value: admin"
	}

	@Test
	fun `fromString throws for wrong cased value`() {
		val exception = shouldThrow<IllegalArgumentException> {
			Access.fromString("Read")
		}
		exception shouldHaveMessage "Unknown access value: Read"
	}

	@Test
	fun `fromString throws for enum name`() {
		val exception = shouldThrow<IllegalArgumentException> {
			Access.fromString(Access.WRITE.name)
		}
		exception shouldHaveMessage "Unknown access value: WRITE"
	}
}
