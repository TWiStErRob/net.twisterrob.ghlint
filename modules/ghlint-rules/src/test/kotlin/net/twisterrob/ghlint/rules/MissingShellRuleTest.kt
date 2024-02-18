package net.twisterrob.ghlint.rules

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class MissingShellRuleTest {

	@TestFactory fun metadata() = test(MissingShellRule::class)

	@Test fun `reports when step is missing a shell`() {
		val result = check<MissingShellRule>(
			"""
				jobs:
				  test:
				    steps:
				      - run: echo "Test"
			""".trimIndent()
		)

		result shouldHave singleFinding(
			"MissingShell",
			"Step[#0] in Job[test] is missing a shell, specify `bash` for better error handling."
		)
	}

	@Test fun `passes when shell is declared on step`() {
		val result = check<MissingShellRule>(
			"""
				jobs:
				  test:
				    steps:
				      - run: echo "Test"
				        shell: bash
			""".trimIndent()
		)

		result shouldHave noFindings()
	}

	@Test fun `passes when shell is declared on the job`() {
		val result = check<MissingShellRule>(
			"""
				jobs:
				  test:
				    defaults:
				      run:
				        shell: bash
				    steps:
				      - run: echo "Test"
				        shell: bash
			""".trimIndent()
		)

		result shouldHave noFindings()
	}

	@Test fun `passes when shell is declared on the workflow`() {
		val result = check<MissingShellRule>(
			"""
				defaults:
				  run:
				    shell: bash
				jobs:
				  test:
				    steps:
				      - run: echo "Test"
			""".trimIndent()
		)

		result shouldHave noFindings()
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
				      - run: echo "Test"
				  test:
				    steps:
				      - run: echo "Test"
			""".trimIndent()
		)

		result shouldHave singleFinding(
			"MissingShell",
			"Step[#0] in Job[test] is missing a shell, specify `bash` for better error handling."
		)
	}
}
