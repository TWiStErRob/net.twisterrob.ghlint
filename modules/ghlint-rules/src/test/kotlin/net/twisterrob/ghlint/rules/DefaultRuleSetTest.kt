package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.testing.validate
import org.junit.jupiter.api.Test

class DefaultRuleSetTest {
	@Test fun test() {
		validate<DefaultRuleSet>()
	}
}
