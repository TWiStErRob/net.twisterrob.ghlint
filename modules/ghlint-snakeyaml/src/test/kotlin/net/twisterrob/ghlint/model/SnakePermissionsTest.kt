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
				      actions: write
				      attestations: read
				      checks: write
				      contents: read
				      deployments: none
				      id-token: read
				      issues: write
				      metadata: read
				      packages: write
				      pages: write
				      pull-requests: write
				      repository-projects: read
				      security-events: write
				      statuses: write
				    runs-on: ubuntu-latest
				    steps:
				      - uses: actions/checkout@v4
			""".trimIndent()
		).asJob()

		job.permissions?.actions shouldBe Access.WRITE
		job.permissions?.attestations shouldBe Access.READ
		job.permissions?.checks shouldBe Access.WRITE
		job.permissions?.contents shouldBe Access.READ
		job.permissions?.deployments shouldBe Access.NONE
		job.permissions?.idToken shouldBe Access.READ
		job.permissions?.issues shouldBe Access.WRITE
		job.permissions?.metadata shouldBe Access.READ
		job.permissions?.packages shouldBe Access.WRITE
		job.permissions?.pages shouldBe Access.WRITE
		job.permissions?.pullRequests shouldBe Access.WRITE
		job.permissions?.repositoryProjects shouldBe Access.READ
		job.permissions?.securityEvents shouldBe Access.WRITE
		job.permissions?.statuses shouldBe Access.WRITE
	}

	@Test fun `workflow has permissions with correct values`() {
		val workflow = load(
			"""
				on: push
				permissions:
				  actions: read
				  attestations: write
				  checks: read
				  contents: write
				  deployments: write
				  id-token: none
				  issues: read
				  metadata: write
				  packages: read
				  pages: read
				  pull-requests: read
				  repository-projects: write
				  security-events: read
				  statuses: read
				jobs:
				  test:
				    runs-on: ubuntu-latest
				    steps:
				      - uses: actions/checkout@v4
			""".trimIndent()
		).content as Workflow

		workflow.permissions?.actions shouldBe Access.READ
		workflow.permissions?.attestations shouldBe Access.WRITE
		workflow.permissions?.checks shouldBe Access.READ
		workflow.permissions?.contents shouldBe Access.WRITE
		workflow.permissions?.deployments shouldBe Access.WRITE
		workflow.permissions?.idToken shouldBe Access.NONE
		workflow.permissions?.issues shouldBe Access.READ
		workflow.permissions?.metadata shouldBe Access.WRITE
		workflow.permissions?.packages shouldBe Access.READ
		workflow.permissions?.pages shouldBe Access.READ
		workflow.permissions?.pullRequests shouldBe Access.READ
		workflow.permissions?.repositoryProjects shouldBe Access.WRITE
		workflow.permissions?.securityEvents shouldBe Access.READ
		workflow.permissions?.statuses shouldBe Access.READ
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
		map shouldBe mapOf(
			"contents" to "read",
			"issues" to "write",
		)
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
		map shouldBe mapOf(
			"some-new-permission" to "read",
		)
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
		map shouldBe mapOf(
			"contents" to "admin",
		)
	}
}
