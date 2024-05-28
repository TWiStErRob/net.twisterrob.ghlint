package net.twisterrob.ghlint.model

import io.kotest.matchers.shouldBe
import net.twisterrob.ghlint.testing.load
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class IsCheckoutKtTest {

	private fun loadFirstStep(@Language("yaml") yaml: String, fileName: String = "test.yml"): Step {
		val file = load(yaml, fileName)
		return when (val content = file.content) {
			is Workflow -> (content.jobs.values.single() as Job.NormalJob).steps.single()
			is Action -> (content.runs as Action.Runs.CompositeRuns).steps.single()
			is InvalidContent -> error("Invalid content: ${content.error}")
		}
	}

	@MethodSource("getCheckoutActions")
	@ParameterizedTest fun `standard checkout actions in workflow`(action: String) {
		val step = loadFirstStep(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - uses: ${action}
			""".trimIndent(),
		)

		step.isCheckout shouldBe true
	}

	@MethodSource("getNonCheckoutActions")
	@ParameterizedTest fun `non-checkout actions in workflow`(action: String) {
		val step = loadFirstStep(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - uses: ${action}
			""".trimIndent(),
		)

		step.isCheckout shouldBe false
	}

	@Test fun `runs step in workflow`() {
		val step = loadFirstStep(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - run: git checkout
			""".trimIndent(),
		)

		step.isCheckout shouldBe false
	}

	@MethodSource("getCheckoutActions")
	@ParameterizedTest fun `standard checkout actions in action`(action: String) {
		val step = loadFirstStep(
			"""
				name: Test
				description: Test
				runs:
				  using: composite
				  steps:
				    - uses: ${action}
			""".trimIndent(),
			fileName = "action.yml",
		)

		step.isCheckout shouldBe true
	}

	@MethodSource("getNonCheckoutActions")
	@ParameterizedTest fun `non-checkout actions in action`(action: String) {
		val step = loadFirstStep(
			"""
				name: Test
				description: Test
				runs:
				  using: composite
				  steps:
				    - uses: ${action}
			""".trimIndent(),
			fileName = "action.yml",
		)

		step.isCheckout shouldBe false
	}

	@Test fun `runs step in action`() {
		val step = loadFirstStep(
			"""
				name: Test
				description: Test
				runs:
				  using: composite
				  steps:
				    - run: git checkout
				      shell: bash
			""".trimIndent(),
			fileName = "action.yml",
		)

		step.isCheckout shouldBe false
	}

	companion object {
		@JvmStatic
		val checkoutActions = arrayOf(
			"actions/checkout@v2",
			"actions/checkout@v4",
			"actions/checkout@v2.3.4",
			"actions/checkout@cafebabecafebabecafebabe",
		)

		@JvmStatic
		val nonCheckoutActions = arrayOf(
			"./actions/checkout",
			"fork/checkout@v4",
			"actions/download-artifact@v4",
			"some/other/actions/checkout@cafebabecafebabecafebabe",
		)
	}
}
