package net.twisterrob.ghlint.docs

import net.twisterrob.ghlint.rules.DefaultRuleSet
import java.nio.file.Path

public fun main(vararg args: String) {
	val target = Path.of(args[0])
	Generator(target).generate(DefaultRuleSet())
}
