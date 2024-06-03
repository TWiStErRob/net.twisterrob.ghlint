package net.twisterrob.ghlint.model

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.doCallRealMethod
import org.mockito.kotlin.whenever

class PermissionsTest {

	@MethodSource("mappings")
	@ParameterizedTest
	fun `lookup delegates to the property`(permission: Permission, prop: Permissions.() -> Access) {
		val permissions: Permissions = mock()
		doCallRealMethod().whenever(permissions)[any()]

		whenever(permissions.prop()).thenReturn(Access.READ)

		permissions[permission] shouldBe Access.READ
	}

	@Test fun `all permissions are covered`() {
		val covered = mappings().map { it.get()[0] }
		covered shouldContainExactlyInAnyOrder Permission.entries
	}

	companion object {
		@JvmStatic
		fun mappings() = listOf(
			arguments(Permission.ACTIONS, Permissions::actions),
			arguments(Permission.ATTESTATIONS, Permissions::attestations),
			arguments(Permission.CHECKS, Permissions::checks),
			arguments(Permission.CONTENTS, Permissions::contents),
			arguments(Permission.DEPLOYMENTS, Permissions::deployments),
			arguments(Permission.ID_TOKEN, Permissions::idToken),
			arguments(Permission.ISSUES, Permissions::issues),
			arguments(Permission.METADATA, Permissions::metadata),
			arguments(Permission.PACKAGES, Permissions::packages),
			arguments(Permission.PAGES, Permissions::pages),
			arguments(Permission.PULL_REQUESTS, Permissions::pullRequests),
			arguments(Permission.REPOSITORY_PROJECTS, Permissions::repositoryProjects),
			arguments(Permission.SECURITY_EVENTS, Permissions::securityEvents),
			arguments(Permission.STATUSES, Permissions::statuses),
		)
	}
}
