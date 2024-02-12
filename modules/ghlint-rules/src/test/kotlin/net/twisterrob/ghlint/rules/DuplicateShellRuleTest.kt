package net.twisterrob.ghlint.rules

import io.kotest.matchers.should
import net.twisterrob.ghlint.testing.beEmpty
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.haveFinding
import org.junit.jupiter.api.Test

class DuplicateShellRuleTest {

	@Test fun `reports when 2 steps have an explicit shell`() {
		val result = check<DuplicateShellRule>(
			"""
				jobs:
				  example:
				    steps:
				      - run: echo "Example"
				        shell: bash
				      - run: echo "Example"
				        shell: bash
			""".trimIndent()
		)

		result should haveFinding(
			"DuplicateShellOnSteps",
			"Job[example] has 2 steps defining bash shell, set default shell on job."
		)
	}

	@Test fun `reports when 3 steps have an explicit shell`() {
		val result = check<DuplicateShellRule>(
			"""
				jobs:
				  example:
				    steps:
				      - run: echo "Example"
				        shell: bash
				      - run: echo "Example"
				        shell: bash
				      - run: echo "Example"
				        shell: bash
			""".trimIndent()
		)

		result should haveFinding(
			"DuplicateShellOnSteps",
			"Job[example] has 3 steps defining bash shell, set default shell on job."
		)
	}

	@Test fun `reports when multiple steps have an explicit shell intermingled with other steps`() {
		val result = check<DuplicateShellRule>(
			"""
				jobs:
				  example:
				    steps:
				      - uses: actions/checkout@v4
				      - run: echo "Example"
				        shell: bash
				      - uses: actions/setup-java@v4
				      - run: echo "Example"
				        shell: bash
				      - uses: actions/upload-artifact@v4
			""".trimIndent()
		)

		result should haveFinding(
			"DuplicateShellOnSteps",
			"Job[example] has 2 steps defining bash shell, set default shell on job."
		)
	}

	@Test fun `passes when there are no run steps`() {
		val result = check<DuplicateShellRule>(
			"""
				jobs:
				  example:
				    steps:
				      - uses: actions/checkout@v4
				      - uses: actions/setup-java@v4
				      - uses: actions/upload-artifact@v4
			""".trimIndent()
		)

		result should beEmpty()
	}

	@Test fun `passes when job has default shell`() {
		val result = check<DuplicateShellRule>(
			"""
				jobs:
				  example:
				    defaults:
				      run:
				        shell: bash
				    steps:
				      - run: echo "Example"
				      - run: echo "Example"
			""".trimIndent()
		)

		result should beEmpty()
	}

	@Test fun `passes when steps have different shells`() {
		val result = check<DuplicateShellRule>(
			"""
				jobs:
				  example:
				    steps:
				      - run: echo "Example"
				        shell: bash
				      - run: echo "Example"
				        shell: sh
			""".trimIndent()
		)

		result should beEmpty()
	}

	@Test fun `passes when steps override default shell`() {
		val result = check<DuplicateShellRule>(
			"""
				defaults:
				  run:
				    shell: bash
				jobs:
				  example:
				    steps:
				      - run: echo "Example"
				        shell: sh
				      - run: echo "Example"
				        shell: sh
			""".trimIndent()
		)

		result should beEmpty()
	}
}
