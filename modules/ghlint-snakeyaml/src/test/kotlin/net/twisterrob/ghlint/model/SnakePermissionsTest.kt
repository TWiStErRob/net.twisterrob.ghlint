package net.twisterrob.ghlint.model

import io.kotest.matchers.nulls.beNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import net.twisterrob.ghlint.testing.load
import net.twisterrob.ghlint.testing.loadUnsafe
import net.twisterrob.ghlint.testing.workflow
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class SnakePermissionsTest {

	private val File.theWorkflow: Workflow
		get() = content as Workflow

	private val File.theJob: Job.NormalJob
		get() = (content as Workflow).jobs.values.single() as Job.NormalJob

	private fun load(@Language("yaml") yaml: String): File =
		load(workflow(yaml))

	private fun loadUnsafe(@Language("yaml") yaml: String): File =
		loadUnsafe(workflow(yaml))

	@CsvSource(
		"read, READ",
		"write, WRITE",
		"none, NONE",
	)
	@ParameterizedTest
	fun `workflow has permissions with correct values`(accessString: String, access: Access) {
		val file = load(
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
		)

		file.theJob.permissions should beNull()
		val workflow = file.theWorkflow
		workflow.permissions?.actions shouldBe access
		workflow.permissions?.attestations shouldBe access
		workflow.permissions?.checks shouldBe access
		workflow.permissions?.contents shouldBe access
		workflow.permissions?.deployments shouldBe access
		workflow.permissions?.idToken shouldBe access
		workflow.permissions?.issues shouldBe access
		workflow.permissions?.packages shouldBe access
		workflow.permissions?.pages shouldBe access
		workflow.permissions?.pullRequests shouldBe access
		workflow.permissions?.repositoryProjects shouldBe access
		workflow.permissions?.securityEvents shouldBe access
		workflow.permissions?.statuses shouldBe access
	}

	@CsvSource(
		"read, READ",
		"write, WRITE",
		"none, NONE",
	)
	@ParameterizedTest
	fun `job has permissions with correct values`(accessString: String, access: Access) {
		val file = load(
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
		)

		file.theWorkflow.permissions should beNull()
		val job = file.theJob
		job.permissions?.actions shouldBe access
		job.permissions?.attestations shouldBe access
		job.permissions?.checks shouldBe access
		job.permissions?.contents shouldBe access
		job.permissions?.deployments shouldBe access
		job.permissions?.idToken shouldBe access
		job.permissions?.issues shouldBe access
		job.permissions?.packages shouldBe access
		job.permissions?.pages shouldBe access
		job.permissions?.pullRequests shouldBe access
		job.permissions?.repositoryProjects shouldBe access
		job.permissions?.securityEvents shouldBe access
		job.permissions?.statuses shouldBe access
	}

	@Test fun `workflow with no permissions is null`() {
		val workflow = load(
			"""
				on: push
				jobs:
				  test:
				    runs-on: ubuntu-latest
				    steps:
				      - uses: actions/checkout@v4
			""".trimIndent()
		).theWorkflow

		workflow.permissions should beNull()
	}

	@Test fun `job with no permissions is null`() {
		val workflow = load(
			"""
				on: push
				jobs:
				  test:
				    runs-on: ubuntu-latest
				    steps:
				      - uses: actions/checkout@v4
			""".trimIndent()
		).theWorkflow

		workflow.permissions should beNull()
	}

	@Test fun `workflow with no permissions set`() {
		val workflow = load(
			"""
				on: push
				permissions: { }
				jobs:
				  test:
				    runs-on: ubuntu-latest
				    steps:
				      - uses: actions/checkout@v4
			""".trimIndent()
		).theWorkflow

		workflow.permissions.verifyTheRestOf()
	}

	@Test fun `job with no permissions set`() {
		val job = load(
			"""
				on: push
				jobs:
				  test:
				    permissions: { }
				    runs-on: ubuntu-latest
				    steps:
				      - uses: actions/checkout@v4
			""".trimIndent()
		).theJob

		job.permissions.verifyTheRestOf()
	}

	@Test fun `workflow with one permission set, remaining are access NONE`() {
		val workflow = load(
			"""
				on: push
				permissions:
				  repository-projects: read
				jobs:
				  test:
				    runs-on: ubuntu-latest
				    steps:
				      - uses: actions/checkout@v4
			""".trimIndent()
		).theWorkflow

		workflow.permissions?.repositoryProjects shouldBe Access.READ
		workflow.permissions.verifyTheRestOf(Permission.REPOSITORY_PROJECTS)
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
		).theJob

		job.permissions?.repositoryProjects shouldBe Access.READ
		job.permissions.verifyTheRestOf(Permission.REPOSITORY_PROJECTS)
	}

	@Test fun `workflow asMap produces map representing exactly what is in the yaml`() {
		val workflow = load(
			"""
				on: push
				permissions:
				  contents: read
				  issues: write
				jobs:
				  test:
				    runs-on: ubuntu-latest
				    steps:
				      - uses: actions/checkout@v4
			""".trimIndent()
		).theWorkflow

		val map = workflow.permissions?.map
		map shouldBe mapOf(
			"contents" to "read",
			"issues" to "write",
		)
	}

	@Test fun `job asMap produces map representing exactly what is in the yaml`() {
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
		).theJob

		job.permissions.shouldNotBeNull().map shouldBe mapOf(
			"contents" to "read",
			"issues" to "write",
		)
	}

	@Test fun `workflow has a new permission not modelled by GHLint`() {
		val workflow = loadUnsafe(
			"""
				on: push
				permissions:
				  some-new-permission: read
				jobs:
				  test:
				    runs-on: ubuntu-latest
				    steps:
				      - uses: actions/checkout@v4
			""".trimIndent()
		).theWorkflow

		workflow.permissions.shouldNotBeNull().map shouldBe mapOf(
			"some-new-permission" to "read",
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
		).theJob

		job.permissions.shouldNotBeNull().map shouldBe mapOf(
			"some-new-permission" to "read",
		)
	}

	@Test fun `workflow has a new access level not modelled by GHLint`() {
		val workflow = loadUnsafe(
			"""
				on: push
				permissions:
				  contents: admin
				jobs:
				  test:
				    runs-on: ubuntu-latest
				    steps:
				      - uses: actions/checkout@v4
			""".trimIndent()
		).theWorkflow

		workflow.permissions.shouldNotBeNull().map shouldBe mapOf(
			"contents" to "admin",
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
		).theJob

		job.permissions.shouldNotBeNull().map shouldBe mapOf(
			"contents" to "admin",
		)
	}

	companion object {

		private fun Permissions?.verifyTheRestOf(vararg explicitlyTestedPermissions: Permission) {
			(Permission.entries - setOf(*explicitlyTestedPermissions)).forEach {
				this?.get(it) shouldBe Access.NONE
			}
		}
	}
}
