package net.twisterrob.ghlint.rules

import io.kotest.matchers.should
import net.twisterrob.ghlint.rules.wip.QuoteGithubEnvRule
import net.twisterrob.ghlint.testing.beEmpty
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.test
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class QuoteGithubEnvRuleTest {

	@TestFactory fun metadata() = test(QuoteGithubEnvRule::class)

	@Test fun `passes when no env defined`() {
		val result = check<QuoteGithubEnvRule>(
			"""
				jobs:
			""".trimIndent()
		)

		result should beEmpty()
	}
}
