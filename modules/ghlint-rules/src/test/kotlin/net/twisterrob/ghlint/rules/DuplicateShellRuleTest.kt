package net.twisterrob.ghlint.rules

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class DuplicateShellRuleTest {

	@TestFactory fun metadata() = test(DuplicateShellRule::class)

	@Test fun `reports when 2 steps have an explicit shell`() {
		val results = check<DuplicateShellRule>(
			"""
				jobs:
				  test:
				    steps:
				      - run: echo "Test"
				        shell: bash
				      - run: echo "Test"
				        shell: bash
			""".trimIndent()
		)

		results shouldHave singleFinding(
			"DuplicateShellOnSteps",
			"Job[test] has all (2) steps defining bash shell, set default shell on job."
		)
	}

	@Test fun `reports when 3 steps have an explicit shell`() {
		val results = check<DuplicateShellRule>(
			"""
				jobs:
				  test:
				    steps:
				      - run: echo "Test"
				        shell: bash
				      - run: echo "Test"
				        shell: bash
				      - run: echo "Test"
				        shell: bash
			""".trimIndent()
		)

		results shouldHave singleFinding(
			"DuplicateShellOnSteps",
			"Job[test] has all (3) steps defining bash shell, set default shell on job."
		)
	}

	@Test fun `reports when multiple steps have an explicit shell intermingled with other steps`() {
		val results = check<DuplicateShellRule>(
			"""
				jobs:
				  test:
				    steps:
				      - uses: actions/checkout@v4
				      - run: echo "Test"
				        shell: bash
				      - uses: actions/setup-java@v4
				      - run: echo "Test"
				        shell: bash
				      - uses: actions/upload-artifact@v4
			""".trimIndent()
		)

		results shouldHave singleFinding(
			"DuplicateShellOnSteps",
			"Job[test] has all (2) steps defining bash shell, set default shell on job."
		)
	}

	@Test fun `passes when there are no run steps`() {
		val results = check<DuplicateShellRule>(
			"""
				jobs:
				  test:
				    steps:
				      - uses: actions/checkout@v4
				      - uses: actions/setup-java@v4
				      - uses: actions/upload-artifact@v4
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `passes when job has default shell`() {
		val results = check<DuplicateShellRule>(
			"""
				jobs:
				  test:
				    defaults:
				      run:
				        shell: bash
				    steps:
				      - run: echo "Test"
				      - run: echo "Test"
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `passes when workflow has default shell`() {
		val results = check<DuplicateShellRule>(
			"""
				defaults:
				  run:
				    shell: bash
				jobs:
				  test:
				    steps:
				      - run: echo "Test"
				      - run: echo "Test"
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `passes when steps have different shells`() {
		val results = check<DuplicateShellRule>(
			"""
				jobs:
				  test:
				    steps:
				      - run: echo "Test"
				        shell: bash
				      - run: echo "Test"
				        shell: sh
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `passes when steps override default shell`() {
		val results = check<DuplicateShellRule>(
			"""
				defaults:
				  run:
				    shell: bash
				jobs:
				  test:
				    steps:
				      - run: echo "Test"
				        shell: sh
				      - run: echo "Test"
				        shell: powershell
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `reports when steps override default shell on job, but all the same`() {
		val results = check<DuplicateShellRule>(
			"""
				jobs:
				  test:
				    defaults:
				      run:
				        shell: bash
				    steps:
				      - run: echo "Test"
				        shell: sh
				      - run: echo "Test"
				        shell: sh
			""".trimIndent()
		)

		results shouldHave singleFinding(
			"DuplicateShellOnSteps",
			"All (2) steps in Job[test] override shell as `sh`, " +
					"change the default shell on the job from `bash` to `sh`, and remove shells from steps."
		)
	}

	@Test fun `reports when steps override default shell on workflow, but all the same`() {
		val results = check<DuplicateShellRule>(
			"""
				defaults:
				  run:
				    shell: bash
				jobs:
				  test:
				    steps:
				      - run: echo "Test"
				        shell: sh
				      - run: echo "Test"
				        shell: sh
			""".trimIndent()
		)

		results shouldHave singleFinding(
			"DuplicateShellOnSteps",
			"All (2) steps in Workflow[test] override shell as `sh`, " +
					"change the default shell on the workflow from `bash` to `sh`, and remove shells from steps."
		)
	}

	@Test fun `reports when steps override default shell on workflow, but all the same, multiple jobs`() {
		val results = check<DuplicateShellRule>(
			"""
				defaults:
				  run:
				    shell: bash
				jobs:
				  test1:
				    steps:
				      - run: echo "Test"
				        shell: sh
				      - run: echo "Test"
				        shell: sh
				  test2:
				    steps:
				      - run: echo "Test"
				        shell: sh
				      - run: echo "Test"
				        shell: sh
			""".trimIndent()
		)

		results shouldHave singleFinding(
			"DuplicateShellOnSteps",
			"All (4) steps in Workflow[test] override shell as `sh`, " +
					"change the default shell on the workflow from `bash` to `sh`, and remove shells from steps."
		)
	}

	@Test fun `passes when steps override default shell to different values`() {
		val results = check<DuplicateShellRule>(
			"""
				defaults:
				  run:
				    shell: bash
				jobs:
				  test1:
				    steps:
				      - run: echo "Test"
				        shell: sh
				      - run: echo "Test"
				        shell: sh
				  test2:
				    steps:
				      - run: echo "Test"
				        shell: zsh
				      - run: echo "Test"
				        shell: zsh
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `passes when a step overrides default shell to different values`() {
		val results = check<DuplicateShellRule>(
			"""
				defaults:
				  run:
				    shell: bash
				jobs:
				  test1:
				    steps:
				      - run: echo "Test"
				        shell: sh
				      - run: echo "Test"
				        shell: sh
				  test2:
				    steps:
				      - run: echo "Test"
				        shell: sh
				      - run: echo "Test"
				        shell: zsh
				      - run: echo "Test"
				        shell: sh
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `passes when a step missing shell - job`() {
		val results = check<DuplicateShellRule>(
			"""
				jobs:
				  test:
				    defaults:
				      run:
				        shell: bash
				    steps:
				      - run: echo "Test"
				        shell: sh
				      - run: echo "Test"
				      - run: echo "Test"
				        shell: sh
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `passes when a step missing shell - workflow`() {
		val results = check<DuplicateShellRule>(
			"""
				defaults:
				  run:
				    shell: bash
				jobs:
				  test1:
				    steps:
				      - run: echo "Test"
				        shell: sh
				      - run: echo "Test"
				        shell: sh
				  test2:
				    steps:
				      - run: echo "Test"
				        shell: sh
				      - run: echo "Test"
				      - run: echo "Test"
				        shell: sh
			""".trimIndent()
		)

		results shouldHave noFindings()
	}
}
