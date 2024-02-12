package net.twisterrob.ghlint.rules

import io.kotest.matchers.should
import net.twisterrob.ghlint.testing.beEmpty
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.haveFinding
import org.junit.jupiter.api.Test

class RedundantShellRuleTest {

	@Test fun `reports when both job and workflow have the same default shell`() {
		val result = check<RedundantShellRule>(
			"""
				defaults:
				  run:
				    shell: bash
				jobs:
				  example:
				    runs-on: ubuntu-latest
				    defaults:
				      run:
				        shell: bash
				    steps:
				      - run: echo "Example"
			""".trimIndent()
		)

		result should haveFinding(
			"RedundantDefaultShell",
			"Both Job[example] and Workflow[test.yml] has bash shell as default, one of them can be removed."
		)
	}

	@Test fun `passes when both job and workflow have different default shell`() {
		val result = check<RedundantShellRule>(
			"""
				defaults:
				  run:
				    shell: sh
				jobs:
				  example:
				    runs-on: ubuntu-latest
				    defaults:
				      run:
				        shell: bash
				    steps:
				      - run: echo "Example"
			""".trimIndent()
		)

		result should beEmpty()
	}

	@Test fun `reports when step has the same shell as the default in job`() {
		val result = check<RedundantShellRule>(
			"""
				jobs:
				  example:
				    runs-on: ubuntu-latest
				    defaults:
				      run:
				        shell: bash
				    steps:
				      - run: echo "Example"
				        shell: bash
			""".trimIndent()
		)

		result should haveFinding(
			"RedundantShell",
			"Both Step[#0] in Job[example] and Job[example] has bash shell, the step's shell can be removed."
		)
	}

	@Test fun `reports when step has the same shell as the default in workflow`() {
		val result = check<RedundantShellRule>(
			"""
				defaults:
				  run:
				    shell: bash
				jobs:
				  example:
				    runs-on: ubuntu-latest
				    steps:
				      - run: echo "Example"
				        shell: bash
			""".trimIndent()
		)

		result should haveFinding(
			"RedundantShell",
			"Both Step[#0] in Job[example] and Workflow[test.yml] has bash shell, the step's shell can be removed."
		)
	}

	@Test fun `reports when step has the same shell as the default in workflow and job`() {
		val result = check<RedundantShellRule>(
			"""
				defaults:
				  run:
				    shell: bash
				jobs:
				  example:
				    runs-on: ubuntu-latest
				    defaults:
				      run:
				        shell: bash
				    steps:
				      - run: echo "Example"
				        shell: bash
			""".trimIndent()
		)

		result should haveFinding(
			"RedundantShell",
			"Both Step[#0] in Job[example] and Job[example] has bash shell, the step's shell can be removed."
		)
		result should haveFinding(
			"RedundantDefaultShell",
			"Both Job[example] and Workflow[test.yml] has bash shell as default, one of them can be removed."
		)
	}

	@Test fun `passes when job and step have different shell`() {
		val result = check<RedundantShellRule>(
			"""
				jobs:
				  example:
				    runs-on: ubuntu-latest
				    defaults:
				      run:
				        shell: sh
				    steps:
				      - run: echo "Example"
				        shell: bash
			""".trimIndent()
		)

		result should beEmpty()
	}

	@Test fun `passes when workflow and step have different shell`() {
		val result = check<RedundantShellRule>(
			"""
				defaults:
				  run:
				    shell: sh
				jobs:
				  example:
				    runs-on: ubuntu-latest
				    steps:
				      - run: echo "Example"
				        shell: bash
			""".trimIndent()
		)

		result should beEmpty()
	}

	@Test fun `passes when workflow, job and step have different shell`() {
		val result = check<RedundantShellRule>(
			"""
				defaults:
				  run:
				    shell: sh
				jobs:
				  example:
				    runs-on: ubuntu-latest
				    defaults:
				      run:
				        shell: bash
				    steps:
				      - run: echo "Example"
				        shell: sh
			""".trimIndent()
		)

		result should beEmpty()
	}
}
