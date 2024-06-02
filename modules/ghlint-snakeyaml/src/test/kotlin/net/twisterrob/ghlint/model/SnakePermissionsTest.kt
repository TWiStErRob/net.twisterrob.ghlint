package net.twisterrob.ghlint.model

import io.kotest.matchers.shouldBe
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

class SnakePermissionsTest {

	@Test fun `job has permissions with correct values`() {
		val job = loadJob(
				"""
				jobs:
				  test:
				    permissions:
				      contents: read
				      issues: write
				    runs-on: ubuntu-latest
				    steps:
				      - uses: actions/checkout@v4
			""".trimIndent()
		)

		job.permissions?.contents shouldBe Access.READ
		job.permissions?.issues shouldBe Access.WRITE
	}

	@Test fun `workflow has permissions with correct values`() {
		val workflow = loadWorkflow(
				"""
				on:
				  push:
				permissions:
				  pull-requests: write
				  id-token: read
				jobs:
				  test:
				    runs-on: ubuntu-latest
				    steps:
				      - uses: actions/checkout@v4
			""".trimIndent()
		)

		workflow.permissions?.pullRequests shouldBe Access.WRITE
		workflow.permissions?.idToken shouldBe Access.READ
	}

	@Test fun `job with no permissions is null`() {
		val job = loadJob(
				"""
				jobs:
				  test:
				    runs-on: ubuntu-latest
				    steps:
				      - uses: actions/checkout@v4
			""".trimIndent()
		)

		job.permissions shouldBe null
	}

	@Test fun `job with one permission set, remaining are access NONE`() {
		val job = loadJob(
				"""
				jobs:
				  test:
				    permissions:
				      repository-projects: read
				    runs-on: ubuntu-latest
				    steps:
				      - uses: actions/checkout@v4
			""".trimIndent()
		)

		job.permissions?.repositoryProjects shouldBe Access.READ

		Permission.entries.forEach {
			if (it != Permission.REPOSITORY_PROJECTS) {
				job.permissions?.accessOf(it) shouldBe Access.NONE
			}
		}
	}

	@Test fun `asMap produces map representing exactly what is in the yaml`() {
		val permissions = loadJob(
				"""
				jobs:
				  test:
				    permissions:
				      contents: read
				      issues: write
				    runs-on: ubuntu-latest
				    steps:
				      - uses: actions/checkout@v4
			""".trimIndent()
		).permissions

		val map = permissions?.asMap()
		map?.size shouldBe 2
		map?.get("contents") shouldBe "read"
		map?.get("issues") shouldBe "write"
	}

	private fun loadJob(@Language("yaml") yaml: String): Job {
		val yamlFile = RawFile(FileLocation("test.yml"), yaml)
		return (SnakeComponentFactory().createFile(yamlFile).content as SnakeWorkflow).jobs.values.single()
	}

	private fun loadWorkflow(@Language("yaml") yaml: String): Workflow {
		val yamlFile = RawFile(FileLocation("test.yml"), yaml)
		return (SnakeComponentFactory().createFile(yamlFile).content as SnakeWorkflow)
	}
}
