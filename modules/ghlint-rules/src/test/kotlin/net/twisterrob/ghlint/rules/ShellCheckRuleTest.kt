package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.testing.test
import org.junit.jupiter.api.TestFactory

class ShellCheckRuleTest {

	@TestFactory fun metadata() = test(ShellCheckRule::class)
}
