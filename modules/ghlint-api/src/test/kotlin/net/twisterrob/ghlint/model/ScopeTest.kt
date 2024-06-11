package net.twisterrob.ghlint.model

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

class ScopeTest {

	@Test
	fun `toString returns correct format`() {
		val scope = Scope(Permission.ACTIONS, Access.READ)
		scope.toString() shouldBe "actions: read"
	}

	@Test fun `equals returns true for same instance`() {
		val scope = Scope(Permission.ACTIONS, Access.READ)
		@Suppress("KotlinConstantConditions") // False positive, it assumes .equals() is correct.
		(scope == scope) shouldBe true
	}

	@Test fun `equals returns false for different class`() {
		val scope = Scope(Permission.ACTIONS, Access.READ)
		scope.equals("actions: read") shouldBe false
		"actions: read".equals(scope) shouldBe false
	}

	@Test fun `equals returns false for null`() {
		val scope = Scope(Permission.ACTIONS, Access.READ)
		@Suppress("detekt.EqualsNullCall") // False positive, == null would not call the method.
		scope.equals(null) shouldBe false
	}

	@Test
	fun `equals returns true for same permission and access`() {
		val scope1 = Scope(Permission.ACTIONS, Access.READ)
		val scope2 = Scope(Permission.ACTIONS, Access.READ)
		(scope1 == scope2) shouldBe true
		(scope2 == scope1) shouldBe true
	}

	@Test
	fun `equals returns false for different permission`() {
		val scope1 = Scope(Permission.ACTIONS, Access.WRITE)
		val scope2 = Scope(Permission.CONTENTS, Access.WRITE)
		(scope1 == scope2) shouldBe false
		(scope2 == scope1) shouldBe false
	}

	@Test
	fun `equals returns false for different access`() {
		val scope1 = Scope(Permission.ACTIONS, Access.READ)
		val scope2 = Scope(Permission.ACTIONS, Access.WRITE)
		(scope1 == scope2) shouldBe false
		(scope2 == scope1) shouldBe false
	}

	@Test
	fun `hashCode is consistent for same permission and access`() {
		val scope1 = Scope(Permission.ACTIONS, Access.READ)
		val scope2 = Scope(Permission.ACTIONS, Access.READ)
		scope1.hashCode() shouldBe scope2.hashCode()
	}

	@Test
	fun `hashCode is different for different access`() {
		val scope1 = Scope(Permission.ACTIONS, Access.READ)
		val scope2 = Scope(Permission.ACTIONS, Access.WRITE)
		scope1.hashCode() shouldNotBe scope2.hashCode()
	}

	@Test
	fun `hashCode is different for different permission`() {
		val scope1 = Scope(Permission.ACTIONS, Access.WRITE)
		val scope2 = Scope(Permission.ISSUES, Access.WRITE)
		scope1.hashCode() shouldNotBe scope2.hashCode()
	}

	@Test
	fun `hashCode is different for different permission and access`() {
		val scope1 = Scope(Permission.ACTIONS, Access.WRITE)
		val scope2 = Scope(Permission.ISSUES, Access.READ)
		scope1.hashCode() shouldNotBe scope2.hashCode()
	}
}
