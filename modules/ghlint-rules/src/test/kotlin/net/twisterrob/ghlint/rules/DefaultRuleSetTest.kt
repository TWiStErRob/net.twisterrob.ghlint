package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.testing.validate
import org.junit.jupiter.api.TestFactory

class DefaultRuleSetTest {

	@TestFactory fun test() = validate(DefaultRuleSet::class)
}
