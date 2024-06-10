package net.twisterrob.ghlint.model

import io.kotest.matchers.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class PermissionTest {

	@CsvSource(
		"ACTIONS, actions",
		"ATTESTATIONS, attestations",
		"CHECKS, checks",
		"CONTENTS, contents",
		"DEPLOYMENTS, deployments",
		"DISCUSSIONS, discussions",
		"ID_TOKEN, id-token",
		"ISSUES, issues",
		"PACKAGES, packages",
		"PAGES, pages",
		"PULL_REQUESTS, pull-requests",
		"REPOSITORY_PROJECTS, repository-projects",
		"SECURITY_EVENTS, security-events",
		"STATUSES, statuses",
	)
	@ParameterizedTest
	fun `enum values match expected strings`(permission: Permission, expectedValue: String) {
		permission.value shouldBe expectedValue
	}
}
