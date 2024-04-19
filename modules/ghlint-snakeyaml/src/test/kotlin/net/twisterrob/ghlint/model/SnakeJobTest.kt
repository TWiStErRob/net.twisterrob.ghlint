package net.twisterrob.ghlint.model

import io.kotest.matchers.collections.containExactly
import io.kotest.matchers.collections.haveSize
import io.kotest.matchers.nulls.beNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SnakeJobTest {

	@Test fun `normal job has steps`() {
		val job = load(
			"""
				jobs:
				  test:
				    runs-on: ubuntu-latest
				    steps:
				      - uses: actions/checkout@v4
				      - uses: actions/checkout@v4
			""".trimIndent()
		)

		job.shouldBeInstanceOf<Job.NormalJob>()
		job.steps should haveSize(2)
	}

	@Test fun `reusable workflow call has uses`() {
		val job = load(
			"""
				jobs:
				  test:
				    uses: reusable/workflow.yml
			""".trimIndent()
		)

		job.shouldBeInstanceOf<Job.ReusableWorkflowCallJob>()
		job.uses shouldBe "reusable/workflow.yml"
	}

	@Nested
	inner class `needs property` {

		@Test fun `reusable workflow call has no dependencies`() {
			val job = load(
				"""
					jobs:
					  test:
					    uses: reusable/workflow.yml
				""".trimIndent()
			)

			job.shouldBeInstanceOf<Job.ReusableWorkflowCallJob>()
			job.needs should beNull()
		}

		@Test fun `reusable workflow call has direct dependency`() {
			val job = load(
				"""
					jobs:
					  test:
					    needs: other
					    uses: reusable/workflow.yml
				""".trimIndent()
			)

			job.shouldBeInstanceOf<Job.ReusableWorkflowCallJob>()
			job.needs should containExactly("other")
		}

		@Test fun `reusable workflow call has multiple dependencies`() {
			val job = load(
				"""
					jobs:
					  test:
					    needs: [test1, test2]
					    uses: reusable/workflow.yml
				""".trimIndent()
			)

			job.shouldBeInstanceOf<Job.ReusableWorkflowCallJob>()
			job.needs should containExactly("test1", "test2")
		}

		@Test fun `reusable workflow call has multiple dependencies (expanded syntax)`() {
			val job = load(
				"""
					jobs:
					  test:
					    needs:
					      - test1
					      - test2
					    uses: reusable/workflow.yml
				""".trimIndent()
			)

			job.shouldBeInstanceOf<Job.ReusableWorkflowCallJob>()
			job.needs should containExactly("test1", "test2")
		}

		@Test fun `normal job has no dependencies`() {
			val job = load(
				"""
					jobs:
					  test:
					    runs-on: ubuntu-latest
					    steps:
					      - uses: actions/checkout@v4
				""".trimIndent()
			)

			job.shouldBeInstanceOf<Job.NormalJob>()
			job.needs should beNull()
		}

		@Test fun `normal job has direct dependency`() {
			val job = load(
				"""
					jobs:
					  test:
					    needs: other
					    runs-on: ubuntu-latest
					    steps:
					      - uses: actions/checkout@v4
				""".trimIndent()
			)

			job.shouldBeInstanceOf<Job.NormalJob>()
			job.needs should containExactly("other")
		}

		@Test fun `normal job has multiple dependencies`() {
			val job = load(
				"""
					jobs:
					  test:
					    needs: [test1, test2]
					    runs-on: ubuntu-latest
					    steps:
					      - uses: actions/checkout@v4
				""".trimIndent()
			)

			job.shouldBeInstanceOf<Job.NormalJob>()
			job.needs should containExactly("test1", "test2")
		}

		@Test fun `normal job has multiple dependencies (expanded syntax)`() {
			val job = load(
				"""
					jobs:
					  test:
					    needs:
					      - test1
					      - test2
					    runs-on: ubuntu-latest
					    steps:
					      - uses: actions/checkout@v4
				""".trimIndent()
			)

			job.shouldBeInstanceOf<Job.NormalJob>()
			job.needs should containExactly("test1", "test2")
		}
	}

	private fun load(@Language("yaml") yaml: String): Job {
		val yamlFile = RawFile(FileLocation("test.yml"), yaml)
		return (SnakeComponentFactory().createFile(yamlFile).content as SnakeWorkflow).jobs.values.single()
	}
}
