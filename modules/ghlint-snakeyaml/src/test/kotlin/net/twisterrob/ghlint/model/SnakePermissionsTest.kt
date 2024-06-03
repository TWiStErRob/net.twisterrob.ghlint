package net.twisterrob.ghlint.model

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import net.twisterrob.ghlint.testing.load
import net.twisterrob.ghlint.testing.loadUnsafe
import org.junit.jupiter.api.Test

class SnakePermissionsTest {

	private fun File.asJob(): Job.NormalJob = (content as Workflow).jobs.values.single() as Job.NormalJob

	@Test fun `job has permissions with correct values`() {
		val job = load(
			"""
				on: push
				jobs:
				  test:
				    permissions:
				      contents: read
				      issues: write
				    runs-on: ubuntu-latest
				    steps:
				      - uses: actions/checkout@v4
			""".trimIndent()
		).asJob()

		job.permissions?.contents shouldBe Access.READ
		job.permissions?.issues shouldBe Access.WRITE
	}

	@Test fun `workflow has permissions with correct values`() {
		val workflow = load(
			"""
				on: push
				permissions:
				  pull-requests: write
				  id-token: read
				jobs:
				  test:
				    runs-on: ubuntu-latest
				    steps:
				      - uses: actions/checkout@v4
			""".trimIndent()
		).content as Workflow

		workflow.permissions?.pullRequests shouldBe Access.WRITE
		workflow.permissions?.idToken shouldBe Access.READ
	}

	@Test fun `job with no permissions is null`() {
		val job = load(
			"""
				on: push
				jobs:
				  test:
				    runs-on: ubuntu-latest
				    steps:
				      - uses: actions/checkout@v4
			""".trimIndent()
		).asJob()

		job.permissions shouldBe null
	}

	@Test fun `job with one permission set, remaining are access NONE`() {
		val job = load(
			"""
				on: push
				jobs:
				  test:
				    permissions:
				      repository-projects: read
				    runs-on: ubuntu-latest
				    steps:
				      - uses: actions/checkout@v4
			""".trimIndent()
		).asJob()

		job.permissions?.repositoryProjects shouldBe Access.READ

		Permission.entries.forEach { permission ->
			if (permission != Permission.REPOSITORY_PROJECTS) {
				job.permissions?.get(permission) shouldBe Access.NONE
			}
		}
	}

	@Test fun `asMap produces map representing exactly what is in the yaml`() {
		val job = load(
			"""
				on: push
				jobs:
				  test:
				    permissions:
				      contents: read
				      issues: write
				    runs-on: ubuntu-latest
				    steps:
				      - uses: actions/checkout@v4
			""".trimIndent()
		).asJob()

		val map = job.permissions?.map
		map?.size shouldBe 2
		map?.get("contents") shouldBe "read"
		map?.get("issues") shouldBe "write"
	}

	@Test fun `job has a new permission not modelled by GHLint`() {
		val job = loadUnsafe(
			"""
				on: push
				jobs:
				  test:
				    permissions:
				      some-new-permission: read
				    runs-on: ubuntu-latest
				    steps:
				      - uses: actions/checkout@v4
			""".trimIndent()
		).asJob()

		job.permissions shouldNotBe null

		val map = job.permissions?.map
		map?.size shouldBe 1
		map?.get("some-new-permission") shouldBe "read"
	}

	@Test fun `job has a new access level not modelled by GHLint`() {
		val job = loadUnsafe(
			"""
				on: push
				jobs:
				  test:
				    permissions:
				      contents: admin
				    runs-on: ubuntu-latest
				    steps:
				      - uses: actions/checkout@v4
			""".trimIndent()
		).asJob()

		job.permissions shouldNotBe null

		val map = job.permissions?.map
		map?.size shouldBe 1
		map?.get("contents") shouldBe "admin"
	}
}
