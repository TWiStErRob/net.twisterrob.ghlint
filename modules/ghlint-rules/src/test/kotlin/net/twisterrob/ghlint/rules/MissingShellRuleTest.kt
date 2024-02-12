package net.twisterrob.ghlint.rules

import io.kotest.matchers.should
import net.twisterrob.ghlint.testing.beEmpty
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.haveFinding
import org.junit.jupiter.api.Test

class MissingShellRuleTest {

	@Test fun `reports when step is missing a shell`() {
		val result = check<MissingShellRule>(
			"""
				jobs:
				  example:
				    steps:
				      - run: echo "Example"
			""".trimIndent()
		)

		result should haveFinding(
			"MissingShell",
			"Step[#0] in Job[example] is missing a shell, specify `bash` for better error handling."
		)
	}

	@Test fun `passes when shell is declared on step`() {
		val result = check<MissingShellRule>(
			"""
				jobs:
				  example:
				    steps:
				      - run: echo "Example"
				        shell: bash
			""".trimIndent()
		)

		result should beEmpty()
	}

	@Test fun `passes when shell is declared on the job`() {
		val result = check<MissingShellRule>(
			"""
				jobs:
				  example:
				    defaults:
				      run:
				        shell: bash
				    steps:
				      - run: echo "Example"
				        shell: bash
			""".trimIndent()
		)

		result should beEmpty()
	}

	@Test fun `passes when shell is declared on the workflow`() {
		val result = check<MissingShellRule>(
			"""
				defaults:
				  run:
				    shell: bash
				jobs:
				  example:
				    steps:
				      - run: echo "Example"
			""".trimIndent()
		)

		result should beEmpty()
	}

	@Test fun `reports when step is declared on another job`() {
		val result = check<MissingShellRule>(
			"""
				jobs:
				  other:
				    defaults:
				      run:
				        shell: bash
				    steps:
				      - run: echo "Example"
				  example:
				    steps:
				      - run: echo "Example"
			""".trimIndent()
		)

		result should haveFinding(
			"MissingShell",
			"Step[#0] in Job[example] is missing a shell, specify `bash` for better error handling."
		)
	}
}
