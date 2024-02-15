package net.twisterrob.ghlint.rules

import io.kotest.matchers.should
import net.twisterrob.ghlint.rules.wip.UseGhTokenWithGhCliRule
import net.twisterrob.ghlint.testing.beEmpty
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.test
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class UseGhTokenWithGhCliRuleTest {

	@TestFactory fun metadata() = test(UseGhTokenWithGhCliRule::class)

	@Test fun `passes when no env defined`() {
		val result = check<UseGhTokenWithGhCliRule>(
			"""
				jobs:
			""".trimIndent()
		)

		result should beEmpty()
	}
}
