package net.twisterrob.ghlint.model

import io.kotest.matchers.nulls.beNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import net.twisterrob.ghlint.testing.load
import net.twisterrob.ghlint.testing.workflow
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class EffectiveShellKtTest {

	@Nested
	inner class JobTest {

		private fun loadSingleJob(@Language("yaml") yaml: String): Job.NormalJob {
			val file = load(workflow(yaml))
			return (file.content as Workflow).jobs.values.single() as Job.NormalJob
		}

		@Test fun `no shell defined`() {
			val job = loadSingleJob(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - uses: some/action@v1
				""".trimIndent(),
			)

			job.effectiveShell should beNull()
		}

		@Test fun `shell defined on step`() {
			val job = loadSingleJob(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - run: echo Hello
					        shell: bash
				""".trimIndent(),
			)

			job.effectiveShell should beNull()
		}

		@Test fun `default shell defined on job`() {
			val job = loadSingleJob(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    defaults:
					      run:
					        shell: bash
					    steps:
					      - uses: some/action@v1
				""".trimIndent(),
			)

			job.effectiveShell shouldBe "bash"
		}

		@Test fun `default shell defined on workflow`() {
			val job = loadSingleJob(
				"""
					on: push
					defaults:
					  run:
					    shell: bash
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - uses: some/action@v1
				""".trimIndent(),
			)

			job.effectiveShell shouldBe "bash"
		}

		@Test fun `default shell defined on both workflow and job`() {
			val job = loadSingleJob(
				"""
					on: push
					defaults:
					  run:
					    shell: powershell
					jobs:
					  test:
					    runs-on: test
					    defaults:
					      run:
					        shell: bash
					    steps:
					      - uses: some/action@v1
				""".trimIndent(),
			)

			job.effectiveShell shouldBe "bash"
		}
	}

	@Nested
	inner class StepTest {

		private fun loadRunStep(@Language("yaml") yaml: String): WorkflowStep.Run {
			val file = load(workflow(yaml))
			return ((file.content as Workflow).jobs.values.single() as Job.NormalJob).steps.single() as WorkflowStep.Run
		}

		@Test fun `no shell defined`() {
			val step = loadRunStep(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - run: echo Hello
				""".trimIndent(),
			)

			step.effectiveShell should beNull()
		}

		@Test fun `shell defined on step`() {
			val step = loadRunStep(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - run: echo Hello
					        shell: bash
				""".trimIndent(),
			)

			step.effectiveShell shouldBe "bash"
		}

		@Test fun `default shell defined on job`() {
			val step = loadRunStep(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    defaults:
					      run:
					        shell: bash
					    steps:
					      - run: echo Hello
				""".trimIndent(),
			)

			step.effectiveShell shouldBe "bash"
		}

		@Test fun `default shell defined on job with step override`() {
			val step = loadRunStep(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    defaults:
					      run:
					        shell: powershell
					    steps:
					      - run: echo Hello
					        shell: bash
				""".trimIndent(),
			)

			step.effectiveShell shouldBe "bash"
		}

		@Test fun `default shell defined on workflow`() {
			val step = loadRunStep(
				"""
					on: push
					defaults:
					  run:
					    shell: bash
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - run: echo Hello
				""".trimIndent(),
			)

			step.effectiveShell shouldBe "bash"
		}

		@Test fun `default shell defined on workflow with step override`() {
			val step = loadRunStep(
				"""
					on: push
					defaults:
					  run:
					    shell: powershell
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - run: echo Hello
					        shell: bash
				""".trimIndent(),
			)

			step.effectiveShell shouldBe "bash"
		}

		@Test fun `default shell defined on both workflow and job`() {
			val step = loadRunStep(
				"""
					on: push
					defaults:
					  run:
					    shell: powershell
					jobs:
					  test:
					    runs-on: test
					    defaults:
					      run:
					        shell: bash
					    steps:
					      - run: echo Hello
				""".trimIndent(),
			)

			step.effectiveShell shouldBe "bash"
		}

		@Test fun `default shell defined on both workflow and job and step`() {
			val step = loadRunStep(
				"""
					on: push
					defaults:
					  run:
					    shell: powershell
					jobs:
					  test:
					    runs-on: test
					    defaults:
					      run:
					        shell: bash
					    steps:
					      - run: echo Hello
					        shell: zsh
				""".trimIndent(),
			)

			step.effectiveShell shouldBe "zsh"
		}
	}
}
