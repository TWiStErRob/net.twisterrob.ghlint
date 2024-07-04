package net.twisterrob.ghlint.rules

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.testing.action
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.invoke
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
import net.twisterrob.ghlint.testing.workflow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class InvalidExpressionUsageRuleTest {

	@TestFactory fun metadata() = test(InvalidExpressionUsageRule::class)

	@Test fun `passes when no expression in uses field`() {
		val file = workflow(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - uses: actions/checkout@v4
			""".trimIndent(),
		)

		val results = check<InvalidExpressionUsageRule>(file)

		results shouldHave noFindings()
	}

	@Test fun `reports when expression in uses field`() {
		val file = workflow(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - uses: actions/checkout@${'$'}{{ github.ref }}
			""".trimIndent(),
		)

		val results = check<InvalidExpressionUsageRule>(file)

		results shouldHave singleFinding(
			issue = "InvalidExpressionUsage",
			message = """
				Step[actions/checkout@${'$'}{{ github.ref }}] in Job[test] contains a GitHub expression in the `uses` field.
			""".trimIndent(),
			location = file("-", 2),
		)
	}

	@Test fun `passes when expression not in uses field for action`() {
		val file = action(
			"""
				name: "Test"
				description: "Test"
				runs:
				  using: composite
				  steps:
				    - name: "Test"
				      uses: actions/checkout@v4
			""".trimIndent(),
		)

		val results = check<InvalidExpressionUsageRule>(file)

		results shouldHave noFindings()
	}

	@Test fun `reports when expression in uses field for action`() {
		val file = action(
			"""
				name: "Test"
				description: "Test"
				runs:
				  using: composite
				  steps:
				    - name: "Test"
				      uses: actions/checkout@${'$'}{{ github.sha }}
			""".trimIndent(),
		)

		val results = check<InvalidExpressionUsageRule>(file)

		results shouldHave singleFinding(
			issue = "InvalidExpressionUsage",
			message = """Step["Test"] in Action["Test"] contains a GitHub expression in the `uses` field.""",
			location = file("-"),
		)
	}

	@Test fun `passes when no expression in uses field for local action`() {
		val file = workflow(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - uses: ./actions/local
			""".trimIndent(),
		)

		val results = check<InvalidExpressionUsageRule>(file)

		results shouldHave noFindings()
	}

	@Test fun `reports when expression in uses field for workflow call job`() {
		val file = workflow(
			"""
				on: push
				jobs:
				  test:
				    uses: org/repo/.github/workflows/reusable.yml@${'$'}{{ github.ref_name }}
			""".trimIndent(),
		)

		val results = check<InvalidExpressionUsageRule>(file)

		results shouldHave singleFinding(
			issue = "InvalidExpressionUsage",
			message = """Job[test] contains a GitHub expression in the `uses` field.""",
			location = file("test"),
		)
	}
}
