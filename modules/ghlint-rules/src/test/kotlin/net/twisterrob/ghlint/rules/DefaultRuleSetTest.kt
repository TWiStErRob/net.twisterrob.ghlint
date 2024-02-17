package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.testing.test
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.TestFactory

class DefaultRuleSetTest {

	@Disabled("TODO Test framework needs adjustments to make it pass.")
	@TestFactory fun test() = test(DefaultRuleSet::class)
}
