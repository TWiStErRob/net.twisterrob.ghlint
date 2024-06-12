package net.twisterrob.ghlint.rules

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.testing.aFinding
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.exactFindings
import net.twisterrob.ghlint.testing.invoke
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
import net.twisterrob.ghlint.testing.workflow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class RedundantShellRuleTest {

	@TestFactory fun metadata() = test(RedundantShellRule::class)

	@Test fun `reports when both job and workflow have the same default shell`() {
		val file = workflow(
			"""
				on: push
				defaults:
				  run:
				    shell: bash
				jobs:
				  test:
				    runs-on: test
				    defaults:
				      run:
				        shell: bash
				    steps:
				      - run: echo "Test"
			""".trimIndent()
		)

		val results = check<RedundantShellRule>(file)

		results shouldHave singleFinding(
			issue = "RedundantDefaultShell",
			message = "Both Job[test] and Workflow[test] has `bash` shell as default, one of them can be removed.",
			location = file("test", 1),
		)
	}

	@Test fun `passes when both job and workflow have different default shell`() {
		val file = workflow(
			"""
				on: push
				defaults:
				  run:
				    shell: sh
				jobs:
				  test:
				    runs-on: test
				    defaults:
				      run:
				        shell: bash
				    steps:
				      - run: echo "Test"
			""".trimIndent(),
		)

		val results = check<RedundantShellRule>(file)

		results shouldHave noFindings()
	}

	@Test fun `reports when step has the same shell as the default in job`() {
		val file = workflow(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    defaults:
				      run:
				        shell: bash
				    steps:
				      - run: echo "Test"
				        shell: bash
			""".trimIndent(),
		)

		val results = check<RedundantShellRule>(file)

		results shouldHave singleFinding(
			issue = "RedundantShell",
			message = "Both Job[test] and Step[#0] in Job[test] has `bash` shell, the step's shell can be removed.",
		)
	}

	@Test fun `reports when step has the same shell as the default in workflow`() {
		val file = workflow(
			"""
				on: push
				defaults:
				  run:
				    shell: bash
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - run: echo "Test"
				        shell: bash
			""".trimIndent(),
		)

		val results = check<RedundantShellRule>(file)

		results shouldHave singleFinding(
			issue = "RedundantShell",
			message = "Both Workflow[test] and Step[#0] in Job[test] has `bash` shell, the step's shell can be removed.",
		)
	}

	@Test fun `reports when step has the same shell as the default in workflow and job`() {
		val file = workflow(
			"""
				on: push
				defaults:
				  run:
				    shell: bash
				jobs:
				  test:
				    runs-on: test
				    defaults:
				      run:
				        shell: bash
				    steps:
				      - run: echo "Test"
				        shell: bash
			""".trimIndent(),
		)

		val results = check<RedundantShellRule>(file)

		results shouldHave exactFindings(
			aFinding(
				"RedundantShell",
				"Both Job[test] and Step[#0] in Job[test] has `bash` shell, the step's shell can be removed."
			),
			aFinding(
				"RedundantDefaultShell",
				"Both Job[test] and Workflow[test] has `bash` shell as default, one of them can be removed."
			),
		)
	}

	@Test fun `passes when job and step have different shell`() {
		val file = workflow(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    defaults:
				      run:
				        shell: sh
				    steps:
				      - run: echo "Test"
				        shell: bash
			""".trimIndent(),
		)

		val results = check<RedundantShellRule>(file)

		results shouldHave noFindings()
	}

	@Test fun `passes when workflow and step have different shell`() {
		val file = workflow(
			"""
				on: push
				defaults:
				  run:
				    shell: sh
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - run: echo "Test"
				        shell: bash
			""".trimIndent(),
		)

		val results = check<RedundantShellRule>(file)

		results shouldHave noFindings()
	}

	@Test fun `passes when workflow, job and step have different shell`() {
		val file = workflow(
			"""
				on: push
				defaults:
				  run:
				    shell: sh
				jobs:
				  test:
				    runs-on: test
				    defaults:
				      run:
				        shell: bash
				    steps:
				      - run: echo "Test"
				        shell: sh
			""".trimIndent(),
		)

		val results = check<RedundantShellRule>(file)

		results shouldHave noFindings()
	}
}
