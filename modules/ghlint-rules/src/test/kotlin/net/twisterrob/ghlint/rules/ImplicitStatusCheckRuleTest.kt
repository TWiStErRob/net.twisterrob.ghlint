package net.twisterrob.ghlint.rules

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.testing.action
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.invoke
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
import net.twisterrob.ghlint.testing.workflow
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class ImplicitStatusCheckRuleTest {

	@TestFactory fun metadata() = test(ImplicitStatusCheckRule::class)

	@Nested
	inner class NeverUseAlwaysStepTest {

		@Test fun `passes when always is not in the condition`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - run: echo "Test"
					        if: success() || failure()
				""".trimIndent(),
			)

			val results = check<ImplicitStatusCheckRule>(file)

			results shouldHave noFindings()
		}

		@Test fun `passes when always is explicitly expressed`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - run: echo "Test"
					        if: success() || failure() || cancelled()
				""".trimIndent(),
			)

			val results = check<ImplicitStatusCheckRule>(file)

			results shouldHave noFindings()
		}

		@Test fun `fails when always is used`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - run: echo "Test"
					        if: always()
				""".trimIndent(),
			)

			val results = check<ImplicitStatusCheckRule>(file)

			results shouldHave singleFinding(
				issue = "NeverUseAlways",
				message = """Step[#0] in Job[test] uses the always() condition.""",
				location = file("-", 2),
			)
		}

		@Test fun `fails when always is used as part of a condition`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - run: echo "Test"
					        if: github.context.value && (always() || failure())
				""".trimIndent(),
			)

			val results = check<ImplicitStatusCheckRule>(file)

			results shouldHave singleFinding(
				issue = "NeverUseAlways",
				message = """Step[#0] in Job[test] uses the always() condition.""",
				location = file("-", 2),
			)
		}
	}

	@Nested
	inner class NeverUseAlwaysActionStepTest {

		@Test fun `passes when always is not in the condition`() {
			val file = action(
				"""
					name: Test
					description: Test
					runs:
					  using: composite
					  steps:
					    - run: echo "Test"
					      shell: bash
					      if: success() || failure()
				""".trimIndent(),
			)

			val results = check<ImplicitStatusCheckRule>(file)

			results shouldHave noFindings()
		}

		@Test fun `passes when always is explicitly expressed`() {
			val file = action(
				"""
					name: Test
					description: Test
					runs:
					  using: composite
					  steps:
					    - run: echo "Test"
					      shell: bash
					      if: success() || failure() || cancelled()
				""".trimIndent(),
			)

			val results = check<ImplicitStatusCheckRule>(file)

			results shouldHave noFindings()
		}

		@Test fun `fails when always is used`() {
			val file = action(
				"""
					name: Test
					description: Test
					runs:
					  using: composite
					  steps:
					    - run: echo "Test"
					      shell: bash
					      if: always()
				""".trimIndent(),
			)

			val results = check<ImplicitStatusCheckRule>(file)

			results shouldHave singleFinding(
				issue = "NeverUseAlways",
				message = """Step[#0] in Action["Test"] uses the always() condition.""",
				location = file("-"),
			)
		}

		@Test fun `fails when always is used as part of a condition`() {
			val file = action(
				"""
					name: Test
					description: Test
					runs:
					  using: composite
					  steps:
					    - run: echo "Test"
					      shell: bash
					      if: github.context.value && (always() || failure())
				""".trimIndent(),
			)

			val results = check<ImplicitStatusCheckRule>(file)

			results shouldHave singleFinding(
				issue = "NeverUseAlways",
				message = """Step[#0] in Action["Test"] uses the always() condition.""",
				location = file("-"),
			)
		}
	}

	@Nested
	inner class NeverUseAlwaysJobTest {

		@Test fun `passes when always is not in the condition`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    if: success() || failure()
					    steps:
					      - run: echo "Test"
				""".trimIndent(),
			)

			val results = check<ImplicitStatusCheckRule>(file)

			results shouldHave noFindings()
		}

		@Test fun `passes when always is explicitly expressed`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    if: success() || failure() || cancelled()
					    steps:
					      - run: echo "Test"
				""".trimIndent(),
			)

			val results = check<ImplicitStatusCheckRule>(file)

			results shouldHave noFindings()
		}

		@Test fun `fails when always is used`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    if: always()
					    steps:
					      - run: echo "Test"
				""".trimIndent(),
			)

			val results = check<ImplicitStatusCheckRule>(file)

			results shouldHave singleFinding(
				issue = "NeverUseAlways",
				message = "Job[test] uses the always() condition.",
				location = file("test"),
			)
		}

		@Test fun `fails when always is used as part of a condition`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    if: github.context.value && (always() || failure())
					    steps:
					      - run: echo "Test"
				""".trimIndent(),
			)

			val results = check<ImplicitStatusCheckRule>(file)

			results shouldHave singleFinding(
				issue = "NeverUseAlways",
				message = "Job[test] uses the always() condition.",
				location = file("test"),
			)
		}
	}

	@Nested
	inner class NegativeStatusCheckTest {

		@ParameterizedTest
		@ValueSource(strings = ["success", "failure", "cancelled", "always"])
		fun `fails when negative status check condition is used`(function: String) {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - run: echo "Test"
					        if: ${'$'}{{ ! ${function}() }}
				""".trimIndent(),
			)

			val results = check<ImplicitStatusCheckRule>(file)

			results.filterNot { it.issue.id == "NeverUseAlways" } shouldHave singleFinding(
				issue = "NegativeStatusCheck",
				message = """Step[#0] in Job[test] uses a negative condition.""",
				location = file("-", 2),
			)
		}

		@ParameterizedTest
		@ValueSource(strings = ["success", "failure", "cancelled", "always"])
		fun `fails when negative status check condition is used in action step`(function: String) {
			val file = action(
				"""
					name: Test
					description: Test
					runs:
					  using: composite
					  steps:
					    - run: echo "Test"
					      shell: bash
					      if: ${'$'}{{ ! ${function}() }}
				""".trimIndent(),
			)

			val results = check<ImplicitStatusCheckRule>(file)

			results.filterNot { it.issue.id == "NeverUseAlways" } shouldHave singleFinding(
				issue = "NegativeStatusCheck",
				message = """Step[#0] in Action["Test"] uses a negative condition.""",
				location = file("-"),
			)
		}
	}
}
