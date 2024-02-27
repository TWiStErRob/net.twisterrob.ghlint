package net.twisterrob.ghlint.docs.issues

import net.twisterrob.ghlint.rules.DefaultRuleSet
import java.nio.file.Path

public fun main(vararg args: String) {
	val target = Path.of(args[0])
	val locator = FileLocator(target)
	Generator(locator, MarkdownRenderer(locator)).generate(DefaultRuleSet())
}
