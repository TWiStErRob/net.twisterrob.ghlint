package net.twisterrob.ghlint.rules

import io.kotest.matchers.should
import net.twisterrob.ghlint.rules.wip.QuoteGithubOutputRule
import net.twisterrob.ghlint.testing.beEmpty
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.test
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class QuoteGithubOutputRuleTest {

	@TestFactory fun metadata() = test(QuoteGithubOutputRule::class)

	@Test fun `passes when no env defined`() {
		val result = check<QuoteGithubOutputRule>(
			"""
				jobs:
			""".trimIndent()
		)

		result should beEmpty()
	}
}
