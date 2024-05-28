package net.twisterrob.ghlint.model

import net.twisterrob.ghlint.testing.load
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class IsCheckoutKtTest {

	@MethodSource("getCheckoutActions")
	@ParameterizedTest fun `standard checkout actions in workflow`(action: String) {
		val file = load(
			"""
				on: push
				jobs:
				  test:
				    runs-on: ubuntu-latest
				    steps:
				      - uses: ${action}
			""".trimIndent(),
		)

		assertTrue(file.firstStep.isCheckout)
	}

	@MethodSource("getNonCheckoutActions")
	@ParameterizedTest fun `non-checkout actions in workflow`(action: String) {
		val file = load(
			"""
				on: push
				jobs:
				  test:
				    runs-on: ubuntu-latest
				    steps:
				      - uses: ${action}
			""".trimIndent(),
		)

		assertFalse(file.firstStep.isCheckout)
	}

	@Test fun `runs step in workflow`() {
		val file = load(
			"""
				on: push
				jobs:
				  test:
				    runs-on: ubuntu-latest
				    steps:
				    - run: git checkout
			""".trimIndent(),
		)

		assertFalse(file.firstStep.isCheckout)
	}

	@MethodSource("getCheckoutActions")
	@ParameterizedTest fun `standard checkout actions in action`(action: String) {
		val file = load(
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

		assertTrue(file.firstStep.isCheckout)
	}

	@MethodSource("getNonCheckoutActions")
	@ParameterizedTest fun `non-checkout actions in action`(action: String) {
		val file = load(
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

		assertFalse(file.firstStep.isCheckout)
	}

	@Test fun `runs step in action`() {
		val file = load(
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

		assertFalse(file.firstStep.isCheckout)
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
