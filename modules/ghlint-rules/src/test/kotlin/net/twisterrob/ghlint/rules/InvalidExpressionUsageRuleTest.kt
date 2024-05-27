package net.twisterrob.ghlint.rules

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
import net.twisterrob.ghlint.testing.check
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class InvalidExpressionUsageRuleTest {

	@TestFactory fun metadata() = test(InvalidExpressionUsageRule::class)

	@Test fun `passes when no expression in uses field`() {
		val results = check<InvalidExpressionUsageRule>(
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
		val results = check<InvalidExpressionUsageRule>(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				    - uses: actions/checkout@${'$'}{{ github.sha }}
			""".trimIndent(),
		)

		results shouldHave singleFinding(
			"InvalidExpressionUsage",
			"Step[actions/checkout@${'$'}{{ github.sha }}] in Job[test] contains a GitHub expression in the `uses` field."
		)
	}

	@Test fun `passes when expression not in uses field for local action`() {
		val results = check<InvalidExpressionUsageRule>(
			"""
				name: "Test"
				description: "Test"
				inputs:
				  test:
				    description: "Test"
				runs:
				  using: composite
				  steps:
				    - name: "Test"
				      uses: actions/checkout@v4
			""".trimIndent(),
			fileName = "action.yml"
		)

		results shouldHave noFindings()
	}

	@Test fun `reports when expression in uses field for local action`() {
		val results = check<InvalidExpressionUsageRule>(
			"""
				name: "Test"
				description: "Test"
				inputs:
				  test:
				    description: "Test"
				runs:
				  using: composite
				  steps:
				    - name: "Test"
				      uses: actions/checkout@${'$'}{{ github.sha }}
			""".trimIndent(),
			fileName = "action.yml"
		)

		val message = """
			Step["Test"] in Action["Test"] contains a GitHub expression in the `uses` field.
		""".trimIndent()

		results shouldHave singleFinding(
			"InvalidExpressionUsage",
			message
		)
	}
}
