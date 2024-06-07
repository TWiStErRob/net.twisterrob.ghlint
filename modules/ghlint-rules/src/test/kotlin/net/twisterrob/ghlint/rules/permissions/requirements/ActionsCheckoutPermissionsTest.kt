package net.twisterrob.ghlint.rules.permissions.requirements

import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import net.twisterrob.ghlint.model.Access
import net.twisterrob.ghlint.model.Permission
import net.twisterrob.ghlint.model.Scope
import net.twisterrob.ghlint.rules.permissions.RequiredScope
import net.twisterrob.ghlint.rules.permissions.stepWith
import org.junit.jupiter.api.Test

class ActionsCheckoutPermissionsTest {

	@Test fun `action name`() {
		ActionsCheckoutPermissions.actionName shouldBe "actions/checkout"
	}

	@Test fun `unknown token requires no permissions`() {
		val result = ActionsCheckoutPermissions.infer(
			stepWith(
				"token" to "unknown-token",
			)
		)

		result should beEmpty()
	}

	@Test fun `default token requires contents`() {
		val result = ActionsCheckoutPermissions.infer(
			stepWith(null)
		)

		result shouldBe setOf(
			RequiredScope(
				Scope(Permission.CONTENTS, Access.READ),
				"To read the repository contents during git clone/fetch.",
			)
		)
	}

	@Test fun `explicit github token requires contents`() {
		val result = ActionsCheckoutPermissions.infer(
			stepWith(
				"token" to "\${{ github.token }}",
			)
		)

		result shouldBe setOf(
			RequiredScope(
				Scope(Permission.CONTENTS, Access.READ),
				"To read the repository contents during git clone/fetch.",
			)
		)
	}

	@Test fun `non-github token requires no permissions`() {
		val result = ActionsCheckoutPermissions.infer(
			stepWith(
				"token" to "\${{ secrets.MY_TOKEN }}",
			)
		)

		result should beEmpty()
	}
}
