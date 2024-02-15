package net.twisterrob.ghlint.rules

import io.kotest.matchers.should
import net.twisterrob.ghlint.rules.wip.AlwaysDoubleCurlyIfRule
import net.twisterrob.ghlint.testing.beEmpty
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.test
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class AlwaysDoubleCurlyIfRuleTest {

	@TestFactory fun metadata() = test(AlwaysDoubleCurlyIfRule::class)

	@Test fun `passes when no env defined`() {
		val result = check<AlwaysDoubleCurlyIfRule>(
			"""
				jobs:
			""".trimIndent()
		)

		result should beEmpty()
	}
}
