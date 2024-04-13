package net.twisterrob.ghlint.analysis

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class SyntaxErrorRuleTest {

	@TestFactory fun metadata() = test(SyntaxErrorRule::class)

	@Test fun `syntax error`() {
		val findings = check<SyntaxErrorRule>("<invalid json />")

		findings shouldHave singleFinding(
			"SyntaxError",
			"File test.yml could not be parsed: java.lang.IllegalArgumentException: " +
					"Root node is not a mapping: ScalarNode."
		)
	}

	@Test fun `wrong yaml contents`() {
		val findings = check<SyntaxErrorRule>("foo: bar")

		findings shouldHave noFindings()
	}
}
