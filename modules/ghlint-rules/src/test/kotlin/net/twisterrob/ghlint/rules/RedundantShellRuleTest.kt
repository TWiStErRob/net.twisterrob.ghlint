package net.twisterrob.ghlint.rules

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.testing.aFinding
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.exactFindings
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class RedundantShellRuleTest {

	@TestFactory fun metadata() = test(RedundantShellRule::class)

	@Test fun `reports when both job and workflow have the same default shell`() {
		val results = check<RedundantShellRule>(
			"""
				defaults:
				  run:
				    shell: bash
				jobs:
				  test:
				    runs-on: ubuntu-latest
				    defaults:
				      run:
				        shell: bash
				    steps:
				      - run: echo "Test"
			""".trimIndent()
		)

		results shouldHave singleFinding(
			"RedundantDefaultShell",
			"Both Job[test] and Workflow[test] has bash shell as default, one of them can be removed."
		)
	}

	@Test fun `passes when both job and workflow have different default shell`() {
		val results = check<RedundantShellRule>(
			"""
				defaults:
				  run:
				    shell: sh
				jobs:
				  test:
				    runs-on: ubuntu-latest
				    defaults:
				      run:
				        shell: bash
				    steps:
				      - run: echo "Test"
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `reports when step has the same shell as the default in job`() {
		val results = check<RedundantShellRule>(
			"""
				jobs:
				  test:
				    runs-on: ubuntu-latest
				    defaults:
				      run:
				        shell: bash
				    steps:
				      - run: echo "Test"
				        shell: bash
			""".trimIndent()
		)

		results shouldHave singleFinding(
			"RedundantShell",
			"Both Step[#0] in Job[test] and Job[test] has bash shell, the step's shell can be removed."
		)
	}

	@Test fun `reports when step has the same shell as the default in workflow`() {
		val results = check<RedundantShellRule>(
			"""
				defaults:
				  run:
				    shell: bash
				jobs:
				  test:
				    runs-on: ubuntu-latest
				    steps:
				      - run: echo "Test"
				        shell: bash
			""".trimIndent()
		)

		results shouldHave singleFinding(
			"RedundantShell",
			"Both Step[#0] in Job[test] and Workflow[test] has bash shell, the step's shell can be removed."
		)
	}

	@Test fun `reports when step has the same shell as the default in workflow and job`() {
		val results = check<RedundantShellRule>(
			"""
				defaults:
				  run:
				    shell: bash
				jobs:
				  test:
				    runs-on: ubuntu-latest
				    defaults:
				      run:
				        shell: bash
				    steps:
				      - run: echo "Test"
				        shell: bash
			""".trimIndent()
		)

		results shouldHave exactFindings(
			aFinding(
				"RedundantShell",
				"Both Step[#0] in Job[test] and Job[test] has bash shell, the step's shell can be removed."
			),
			aFinding(
				"RedundantDefaultShell",
				"Both Job[test] and Workflow[test] has bash shell as default, one of them can be removed."
			),
		)
	}

	@Test fun `passes when job and step have different shell`() {
		val results = check<RedundantShellRule>(
			"""
				jobs:
				  test:
				    runs-on: ubuntu-latest
				    defaults:
				      run:
				        shell: sh
				    steps:
				      - run: echo "Test"
				        shell: bash
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `passes when workflow and step have different shell`() {
		val results = check<RedundantShellRule>(
			"""
				defaults:
				  run:
				    shell: sh
				jobs:
				  test:
				    runs-on: ubuntu-latest
				    steps:
				      - run: echo "Test"
				        shell: bash
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `passes when workflow, job and step have different shell`() {
		val results = check<RedundantShellRule>(
			"""
				defaults:
				  run:
				    shell: sh
				jobs:
				  test:
				    runs-on: ubuntu-latest
				    defaults:
				      run:
				        shell: bash
				    steps:
				      - run: echo "Test"
				        shell: sh
			""".trimIndent()
		)

		results shouldHave noFindings()
	}
}
