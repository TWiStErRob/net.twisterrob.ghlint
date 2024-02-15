package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.testing.test
import org.junit.jupiter.api.TestFactory

class DefaultRuleSetTest {

	@TestFactory fun test() = test(DefaultRuleSet::class)
}
