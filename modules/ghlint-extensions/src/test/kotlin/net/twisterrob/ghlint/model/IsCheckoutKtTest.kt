package net.twisterrob.ghlint.model

import io.kotest.matchers.shouldBe
import net.twisterrob.ghlint.testing.action
import net.twisterrob.ghlint.testing.load
import net.twisterrob.ghlint.testing.workflow
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class IsCheckoutKtTest {

	private val File.theStep: Step
		get() = when (val content = content) {
			is Workflow -> (content.jobs.values.single() as Job.NormalJob).steps.single()
			is Action -> (content.runs as Action.Runs.CompositeRuns).steps.single()
			is InvalidContent -> error("Invalid content: ${content.error}")
		}

	private fun loadActionStep(@Language("yaml") yaml: String): Step =
		load(action(yaml)).theStep

	private fun loadWorkflowStep(@Language("yaml") yaml: String): Step =
		load(workflow(yaml)).theStep

	@MethodSource("getCheckoutActions")
	@ParameterizedTest fun `standard checkout actions in workflow`(action: String) {
		val step = loadWorkflowStep(
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
		val step = loadWorkflowStep(
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
		val step = loadWorkflowStep(
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
		val step = loadActionStep(
			"""
				name: Test
				description: Test
				runs:
				  using: composite
				  steps:
				    - uses: ${action}
			""".trimIndent(),
		)

		step.isCheckout shouldBe true
	}

	@MethodSource("getNonCheckoutActions")
	@ParameterizedTest fun `non-checkout actions in action`(action: String) {
		val step = loadActionStep(
			"""
				name: Test
				description: Test
				runs:
				  using: composite
				  steps:
				    - uses: ${action}
			""".trimIndent(),
		)

		step.isCheckout shouldBe false
	}

	@Test fun `runs step in action`() {
		val step = loadActionStep(
			"""
				name: Test
				description: Test
				runs:
				  using: composite
				  steps:
				    - run: git checkout
				      shell: bash
			""".trimIndent(),
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
