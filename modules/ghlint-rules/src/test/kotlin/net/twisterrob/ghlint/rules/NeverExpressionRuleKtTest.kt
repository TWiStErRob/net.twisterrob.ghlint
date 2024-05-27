package net.twisterrob.ghlint.rules

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class NeverExpressionRuleKtTest {

	@TestFactory fun metadata() = test(NeverExpressionRule::class)
	@Test fun `passes when no expression in uses field`() {
		val results = net.twisterrob.ghlint.testing.check<NeverExpressionRule>(
            """
             on: push
             jobs:
               test:
                 runs-on: test
                 steps:
                 - uses: actions/checkout@v4
            """.trimIndent(),
		)

		results shouldHave noFindings()
	}

	@Test fun `reports when expression in uses field`() {
		val uses = "actions/checkout@\${{ github.sha }}"
		val results = net.twisterrob.ghlint.testing.check<NeverExpressionRule>(
            """
             on: push
             jobs:
               test:
                 runs-on: test
                 steps:
                 - uses: actions/checkout@\${uses}
            """.trimIndent(),
		)

		results shouldHave singleFinding(
            "NeverExpression",
            "Step[actions/checkout@\$uses] in Job[test] uses a GitHub expression in the uses field."
        )
	}

	@Test fun `containsExpression should return true for expression`() {
		val uses = "owner/repo@\${{ github.sha }}"
		val isGitHubExpression = uses.containsGitHubExpression()
		assertTrue(isGitHubExpression)
	}

	@Test fun `containsExpression should return false for no expression`() {
		val uses = "owner/repo@main"
		val isGitHubExpression = uses.containsGitHubExpression()
		assertFalse(isGitHubExpression)
	}
}
