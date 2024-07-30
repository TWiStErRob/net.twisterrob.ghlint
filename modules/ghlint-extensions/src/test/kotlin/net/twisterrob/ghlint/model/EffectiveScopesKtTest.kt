package net.twisterrob.ghlint.model

import io.kotest.matchers.shouldBe
import net.twisterrob.ghlint.testing.load
import net.twisterrob.ghlint.testing.workflow
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

class EffectiveScopesKtTest {

	private fun loadPermissions(@Language("yaml") yaml: String): Permissions {
		val file = load(workflow(yaml))
		return (file.content as Workflow).permissions!!
	}

	private fun implicitPermissionsExcluding(vararg excluded: Permission): Set<Scope> =
		Permission.entries
			.filter { it !in excluded }
			.map { Scope(it, Access.NONE) }
			.toSet()

	@Test fun `single read permission`() {
		val permissions = loadPermissions(
			"""
				on: push
				permissions:
				  contents: read
				jobs:
				  test:
				    uses: reusable/workflow.yml
			""".trimIndent(),
		)

		permissions.effectiveScopes shouldBe setOf(
			Scope(Permission.CONTENTS, Access.READ),
		) + implicitPermissionsExcluding(Permission.CONTENTS)
	}

	@Test fun `single write permission includes read`() {
		val permissions = loadPermissions(
			"""
				on: push
				permissions:
				  pull-requests: write
				jobs:
				  test:
				    uses: reusable/workflow.yml
			""".trimIndent(),
		)

		permissions.effectiveScopes shouldBe setOf(
			Scope(Permission.PULL_REQUESTS, Access.READ),
			Scope(Permission.PULL_REQUESTS, Access.WRITE),
		) + implicitPermissionsExcluding(Permission.PULL_REQUESTS)
	}

	@Test fun `all none includes nothing`() {
		val permissions = loadPermissions(
			"""
				on: push
				permissions:
				  actions: none
				  attestations: none
				  checks: none
				  contents: none
				  discussions: none
				  deployments: none
				  id-token: none
				  issues: none
				  packages: none
				  pages: none
				  pull-requests: none
				  repository-projects: none
				  security-events: none
				  statuses: none
				jobs:
				  test:
				    uses: reusable/workflow.yml
			""".trimIndent(),
		)

		permissions.effectiveScopes shouldBe implicitPermissionsExcluding()
	}

	@Test fun `all read includes reads`() {
		val permissions = loadPermissions(
			"""
				on: push
				permissions:
				  actions: read
				  attestations: read
				  checks: read
				  contents: read
				  deployments: read
				  discussions: read
				  id-token: read
				  issues: read
				  packages: read
				  pages: read
				  pull-requests: read
				  repository-projects: read
				  security-events: read
				  statuses: read
				jobs:
				  test:
				    uses: reusable/workflow.yml
			""".trimIndent(),
		)

		permissions.effectiveScopes shouldBe Permission.entries
				.map { Scope(it, Access.READ) }
				.toSet()
	}

	@Test fun `all write includes reads and writes`() {
		val permissions = loadPermissions(
			"""
				on: push
				permissions:
				  actions: write
				  attestations: write
				  checks: write
				  contents: write
				  deployments: write
				  discussions: write
				  id-token: write
				  issues: write
				  packages: write
				  pages: write
				  pull-requests: write
				  repository-projects: write
				  security-events: write
				  statuses: write
				jobs:
				  test:
				    uses: reusable/workflow.yml
			""".trimIndent(),
		)

		permissions.effectiveScopes shouldBe Permission.entries
				.flatMap { listOf(Scope(it, Access.READ), Scope(it, Access.WRITE)) }
				.toSet()
	}

	@Test fun `mixed permissions include extra reads for writes`() {
		val permissions = loadPermissions(
			"""
				on: push
				permissions:
				  actions: none
				  checks: write
				  contents: none
				  issues: read
				  pages: write
				  pull-requests: read
				  statuses: none
				jobs:
				  test:
				    uses: reusable/workflow.yml
			""".trimIndent(),
		)

		permissions.effectiveScopes shouldBe setOf(
			Scope(Permission.ACTIONS, Access.NONE),
			Scope(Permission.CHECKS, Access.WRITE),
			Scope(Permission.CHECKS, Access.READ),
			Scope(Permission.CONTENTS, Access.NONE),
			Scope(Permission.ISSUES, Access.READ),
			Scope(Permission.PAGES, Access.WRITE),
			Scope(Permission.PAGES, Access.READ),
			Scope(Permission.PULL_REQUESTS, Access.READ),
			Scope(Permission.STATUSES, Access.NONE),
		) + implicitPermissionsExcluding(
			Permission.ACTIONS,
			Permission.CHECKS,
			Permission.CONTENTS,
			Permission.ISSUES,
			Permission.PAGES,
			Permission.PULL_REQUESTS,
			Permission.STATUSES,
		)
	}
}
