package net.twisterrob.ghlint.model

import io.kotest.matchers.nulls.beNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import net.twisterrob.ghlint.testing.load
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

class EffectivePermissionsKtTest {

	private fun loadSingleJob(@Language("yaml") yaml: String): Job {
		val file = load(yaml)
		return (file.content as Workflow).jobs.values.single()
	}

	@Test fun `missing permissions`() {
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

		job.effectivePermissions should beNull()
	}

	@Test fun `missing permissions on reusable job`() {
		val job = loadSingleJob(
			"""
				on: push
				jobs:
				  test:
				    uses: reusable/workflow.yml
			""".trimIndent(),
		)

		job.effectivePermissions should beNull()
	}

	@Test fun `no permissions defined on job`() {
		val job = loadSingleJob(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    permissions: {}
				    steps:
				      - uses: some/action@v1
			""".trimIndent(),
		)

		job.effectivePermissions shouldNot beNull()
	}

	@Test fun `no permissions defined on reusable job`() {
		val job = loadSingleJob(
			"""
				on: push
				jobs:
				  test:
				    uses: reusable/workflow.yml
				    permissions: {}
			""".trimIndent(),
		)

		job.effectivePermissions shouldNot beNull()
	}

	@Test fun `no permissions defined on workflow`() {
		val job = loadSingleJob(
			"""
				on: push
				permissions: {}
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - uses: some/action@v1
			""".trimIndent(),
		)

		job.effectivePermissions shouldNot beNull()
	}

	@Test fun `some permissions defined on job`() {
		val job = loadSingleJob(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    permissions:
				      contents: read
				      pull-requests: write
				    steps:
				      - uses: some/action@v1
			""".trimIndent(),
		)

		job.effectivePermissions shouldNot beNull()
	}

	@Test fun `some permissions defined on reusable job`() {
		val job = loadSingleJob(
			"""
				on: push
				jobs:
				  test:
				    uses: reusable/workflow.yml
				    permissions:
				      contents: read
				      pull-requests: write
			""".trimIndent(),
		)

		job.effectivePermissions shouldNot beNull()
	}

	@Test fun `some permissions defined on workflow`() {
		val job = loadSingleJob(
			"""
				on: push
				permissions:
				  contents: read
				  pull-requests: write
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - uses: some/action@v1
			""".trimIndent(),
		)

		job.effectivePermissions shouldNot beNull()
	}

	@Test fun `some permissions defined on workflow and job`() {
		val job = loadSingleJob(
			"""
				on: push
				permissions:
				  contents: read
				  pull-requests: write
				jobs:
				  test:
				    runs-on: test
				    permissions:
				      statuses: read
				      checks: write
				    steps:
				      - uses: some/action@v1
			""".trimIndent(),
		)

		job.effectivePermissions?.map shouldBe job.permissions?.map
	}
}
