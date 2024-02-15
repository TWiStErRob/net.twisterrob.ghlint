package net.twisterrob.ghlint.rules

import io.kotest.matchers.should
import net.twisterrob.ghlint.rules.wip.CreatePullRequestRule
import net.twisterrob.ghlint.testing.beEmpty
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.test
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class CreatePullRequestRuleTest {

	@TestFactory fun metadata() = test(CreatePullRequestRule::class)

	@Test fun `passes when no env defined`() {
		val result = check<CreatePullRequestRule>(
			"""
				jobs:
			""".trimIndent()
		)

		result should beEmpty()
	}
}
