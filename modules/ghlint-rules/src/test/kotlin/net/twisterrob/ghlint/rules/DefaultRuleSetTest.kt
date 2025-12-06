package net.twisterrob.ghlint.rules

import io.kotest.matchers.collections.shouldBeSortedBy
import net.twisterrob.ghlint.testing.jupiter.AcceptFailingDynamicTest
import net.twisterrob.ghlint.testing.test
import net.twisterrob.ghlint.testing.testRulesPackage
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class DefaultRuleSetTest {

	@AcceptFailingDynamicTest(
		displayName = "Issue MissingJobPermissions compliant example #2 has no findings",
		reason = "Rule triggers another another finding, but it's acceptable for this issue.",
		acceptableFailure = "^\\Q"
				+ "Collection should have size 0 but has size 1. Values: ["
				+ "Finding(\n"
				+ "	rule=net.twisterrob.ghlint.rules.ExplicitJobPermissionsRule@\\E[0-9a-f]+\\Q,\n"
				+ "	issue=ExplicitJobPermissions,\n"
				+ "	location=compliant/example.yml/5:3-5:10,\n"
				+ "	message=Job[example] should have explicit permissions.\n"
				+ ")"
				+ "]\n"
				+ "expected:<0> but was:<1>"
				+ "\\E$"
	)
	@AcceptFailingDynamicTest(
		displayName = "Issue MissingShell non-compliant example #2 has findings",
		reason = "Shell is mandatory for Actions, JSON-schema validation catches it.",
		acceptableFailure = "^\\QCould not find exclusively `MissingShell`s among findings:\n"
				+ "Finding(\n"
				+ "\trule=net.twisterrob.ghlint.analysis.ValidationRule@\\E[0-9a-f]+\\Q,\n"
				+ "\tissue=JsonSchemaValidation,\n"
				+ "\tlocation=non-compliant/example.yml/1:1-6:50,\n"
				+ "\tmessage=Object does not have some of the required properties [jobs, on] ()\n"
				+ ")\n"
				+ "Finding(\n"
				+ "\trule=net.twisterrob.ghlint.analysis.ValidationRule@\\E[0-9a-f]+\\Q,\n"
				+ "\tissue=YamlLoadError,\n"
				+ "\tlocation=non-compliant/example.yml/1:1-6:50,\n"
				+ "\tmessage=File non-compliant/example.yml could not be loaded:\n"
				+ "```\n"
				+ "java.lang.IllegalStateException: Missing required key: jobs in [name, description, runs]\n"
				+ "```\n"
				+ ")\\E$"
	)
	@Suppress("detekt.StringShouldBeRawString") // Cannot trimIndent on annotation parameters.
	@TestFactory fun test() = test(DefaultRuleSet::class)

	@Test
	fun `includes all rules in the package`() {
		testRulesPackage(DefaultRuleSet::class)
	}

	@Test
	fun `rules are alphabetically sorted`() {
		val rules = DefaultRuleSet().createRules()

		rules shouldBeSortedBy { it::class.simpleName.orEmpty() }
	}
}
