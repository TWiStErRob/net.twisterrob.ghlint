package net.twisterrob.ghlint.model

import io.kotest.matchers.nulls.beNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import net.twisterrob.ghlint.testing.load
import net.twisterrob.ghlint.testing.workflow
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DefaultShellKtTest {

	@Nested
	inner class WorkflowTest {

		private fun loadWorkflow(@Language("yaml") yaml: String): Workflow {
			val file = load(workflow(yaml))
			return file.content as Workflow
		}

		@Test fun `no shell defined`() {
			val workflow = loadWorkflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - uses: some/action@v1
				""".trimIndent(),
			)

			workflow.defaultShell should beNull()
		}

		@Test fun `shell defined on step`() {
			val workflow = loadWorkflow(
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

			workflow.defaultShell should beNull()
		}

		@Test fun `default shell defined on job`() {
			val workflow = loadWorkflow(
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

			workflow.defaultShell should beNull()
		}

		@Test fun `default shell defined on workflow`() {
			val workflow = loadWorkflow(
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

			workflow.defaultShell shouldBe "bash"
		}

		@Test fun `default shell defined on both workflow and job`() {
			val workflow = loadWorkflow(
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

			workflow.defaultShell shouldBe "powershell"
		}
	}

	@Nested
	inner class JobTest {

		private fun loadJob(@Language("yaml") yaml: String): Job.NormalJob {
			val file = load(workflow(yaml))
			return (file.content as Workflow).jobs.values.single() as Job.NormalJob
		}

		@Test fun `no shell defined`() {
			val job = loadJob(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - uses: some/action@v1
				""".trimIndent(),
			)

			job.defaultShell should beNull()
		}

		@Test fun `shell defined on step`() {
			val job = loadJob(
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

			job.defaultShell should beNull()
		}

		@Test fun `default shell defined on job`() {
			val job = loadJob(
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

			job.defaultShell shouldBe "bash"
		}

		@Test fun `default shell defined on workflow`() {
			val job = loadJob(
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

			job.defaultShell should beNull()
		}

		@Test fun `default shell defined on both workflow and job`() {
			val job = loadJob(
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

			job.defaultShell shouldBe "bash"
		}
	}
}
