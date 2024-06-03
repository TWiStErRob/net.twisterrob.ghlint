package net.twisterrob.ghlint.model

import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import net.twisterrob.ghlint.testing.load
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

class EffectiveScopesKtTest {

	private fun loadPermissions(@Language("yaml") yaml: String): Permissions {
		val file = load(yaml)
		return (file.content as Workflow).permissions!!
	}

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
		)
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
		)
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
				  deployments: none
				  id-token: none
				  issues: none
				  #metadata: none
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

		permissions.effectiveScopes should beEmpty()
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
				  id-token: read
				  issues: read
				  #metadata: read
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

		permissions.effectiveScopes shouldBe setOf(
			Scope(Permission.ACTIONS, Access.READ),
			Scope(Permission.ATTESTATIONS, Access.READ),
			Scope(Permission.CHECKS, Access.READ),
			Scope(Permission.CONTENTS, Access.READ),
			Scope(Permission.DEPLOYMENTS, Access.READ),
			Scope(Permission.ID_TOKEN, Access.READ),
			Scope(Permission.ISSUES, Access.READ),
			//Scope(Permission.METADATA, Access.READ),
			Scope(Permission.PACKAGES, Access.READ),
			Scope(Permission.PAGES, Access.READ),
			Scope(Permission.PULL_REQUESTS, Access.READ),
			Scope(Permission.REPOSITORY_PROJECTS, Access.READ),
			Scope(Permission.SECURITY_EVENTS, Access.READ),
			Scope(Permission.STATUSES, Access.READ),
		)
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
				  id-token: write
				  issues: write
				  #metadata: write
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

		permissions.effectiveScopes shouldBe setOf(
			Scope(Permission.ACTIONS, Access.READ),
			Scope(Permission.ACTIONS, Access.WRITE),
			Scope(Permission.ATTESTATIONS, Access.READ),
			Scope(Permission.ATTESTATIONS, Access.WRITE),
			Scope(Permission.CHECKS, Access.READ),
			Scope(Permission.CHECKS, Access.WRITE),
			Scope(Permission.CONTENTS, Access.READ),
			Scope(Permission.CONTENTS, Access.WRITE),
			Scope(Permission.DEPLOYMENTS, Access.READ),
			Scope(Permission.DEPLOYMENTS, Access.WRITE),
			Scope(Permission.ID_TOKEN, Access.READ),
			Scope(Permission.ID_TOKEN, Access.WRITE),
			Scope(Permission.ISSUES, Access.READ),
			Scope(Permission.ISSUES, Access.WRITE),
			//Scope(Permission.METADATA, Access.READ),
			//Scope(Permission.METADATA, Access.WRITE),
			Scope(Permission.PACKAGES, Access.READ),
			Scope(Permission.PACKAGES, Access.WRITE),
			Scope(Permission.PAGES, Access.READ),
			Scope(Permission.PAGES, Access.WRITE),
			Scope(Permission.PULL_REQUESTS, Access.READ),
			Scope(Permission.PULL_REQUESTS, Access.WRITE),
			Scope(Permission.REPOSITORY_PROJECTS, Access.READ),
			Scope(Permission.REPOSITORY_PROJECTS, Access.WRITE),
			Scope(Permission.SECURITY_EVENTS, Access.READ),
			Scope(Permission.SECURITY_EVENTS, Access.WRITE),
			Scope(Permission.STATUSES, Access.READ),
			Scope(Permission.STATUSES, Access.WRITE),
		)
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
			Scope(Permission.ACTIONS, Access.READ),
			Scope(Permission.CHECKS, Access.WRITE),
			Scope(Permission.CHECKS, Access.READ),
			Scope(Permission.CONTENTS, Access.READ),
			Scope(Permission.ISSUES, Access.READ),
			Scope(Permission.PAGES, Access.WRITE),
			Scope(Permission.PAGES, Access.READ),
			Scope(Permission.PULL_REQUESTS, Access.READ),
			Scope(Permission.STATUSES, Access.READ),
		)
	}
}
