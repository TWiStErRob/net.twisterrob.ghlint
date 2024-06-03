package net.twisterrob.ghlint.model

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import net.twisterrob.ghlint.testing.load
import net.twisterrob.ghlint.testing.loadUnsafe
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class SnakePermissionsTest {

	private fun File.asJob(): Job.NormalJob = (content as Workflow).jobs.values.single() as Job.NormalJob

	@CsvSource(
		"read, READ",
		"write, WRITE",
		"none, NONE",
	)
	@ParameterizedTest
	fun `job has permissions with correct values`(accessString: String, access: Access) {
		val job = load(
			"""
				on: push
				jobs:
				  test:
				    permissions:
				      actions: ${accessString}
				      attestations: ${accessString}
				      checks: ${accessString}
				      contents: ${accessString}
				      deployments: ${accessString}
				      id-token: ${accessString}
				      issues: ${accessString}
				      #metadata: ${accessString}
				      packages: ${accessString}
				      pages: ${accessString}
				      pull-requests: ${accessString}
				      repository-projects: ${accessString}
				      security-events: ${accessString}
				      statuses: ${accessString}
				    runs-on: ubuntu-latest
				    steps:
				      - uses: actions/checkout@v4
			""".trimIndent()
		).asJob()

		job.permissions?.actions shouldBe access
		job.permissions?.attestations shouldBe access
		job.permissions?.checks shouldBe access
		job.permissions?.contents shouldBe access
		job.permissions?.deployments shouldBe access
		job.permissions?.idToken shouldBe access
		job.permissions?.issues shouldBe access
		//job.permissions?.metadata shouldBe access
		job.permissions?.packages shouldBe access
		job.permissions?.pages shouldBe access
		job.permissions?.pullRequests shouldBe access
		job.permissions?.repositoryProjects shouldBe access
		job.permissions?.securityEvents shouldBe access
		job.permissions?.statuses shouldBe access
	}

	@CsvSource(
		"read, READ",
		"write, WRITE",
		"none, NONE",
	)
	@ParameterizedTest
	fun `workflow has permissions with correct values`(accessString: String, access: Access) {
		val workflow = load(
			"""
				on: push
				permissions:
				  actions: ${accessString}
				  attestations: ${accessString}
				  checks: ${accessString}
				  contents: ${accessString}
				  deployments: ${accessString}
				  id-token: ${accessString}
				  issues: ${accessString}
				  #metadata: ${accessString}
				  packages: ${accessString}
				  pages: ${accessString}
				  pull-requests: ${accessString}
				  repository-projects: ${accessString}
				  security-events: ${accessString}
				  statuses: ${accessString}
				jobs:
				  test:
				    runs-on: ubuntu-latest
				    steps:
				      - uses: actions/checkout@v4
			""".trimIndent()
		).content as Workflow

		workflow.permissions?.actions shouldBe access
		workflow.permissions?.attestations shouldBe access
		workflow.permissions?.checks shouldBe access
		workflow.permissions?.contents shouldBe access
		workflow.permissions?.deployments shouldBe access
		workflow.permissions?.idToken shouldBe access
		workflow.permissions?.issues shouldBe access
		//workflow.permissions?.metadata shouldBe access
		workflow.permissions?.packages shouldBe access
		workflow.permissions?.pages shouldBe access
		workflow.permissions?.pullRequests shouldBe access
		workflow.permissions?.repositoryProjects shouldBe access
		workflow.permissions?.securityEvents shouldBe access
		workflow.permissions?.statuses shouldBe access
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
