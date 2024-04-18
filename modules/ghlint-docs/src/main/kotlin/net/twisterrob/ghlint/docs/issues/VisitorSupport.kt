package net.twisterrob.ghlint.docs.issues

import net.twisterrob.ghlint.rule.Rule
import net.twisterrob.ghlint.rule.visitor.ActionVisitor
import net.twisterrob.ghlint.rule.visitor.InvalidContentVisitor
import net.twisterrob.ghlint.rule.visitor.WorkflowVisitor
import java.nio.file.Path
import kotlin.io.path.relativeTo
import kotlin.reflect.KClass

internal class VisitorSupport(
	val klass: KClass<*>,
	val label: String,
	val path: String,
) {

	companion object {

		private val ALL = listOf(
			VisitorSupport(
				klass = WorkflowVisitor::class,
				label = "workflows",
				path = "rules/workflows.md",
			),
			VisitorSupport(
				klass = ActionVisitor::class,
				label = "actions",
				path = "rules/actions.md",
			),
			VisitorSupport(
				klass = InvalidContentVisitor::class,
				label = "invalid content",
				path = "rules/invalid.md",
			),
		)

		@Suppress("detekt.FunctionMinLength")
		fun of(rule: Rule, locator: FileLocator, referencingFile: Path): String? =
			ALL
				.filter { it.klass.java.isAssignableFrom(rule::class.java) }
				.takeIf { it.isNotEmpty() }
				?.joinToString(separator = ", ") { support ->
					val doc = locator.docFile(support.path).relativeTo(referencingFile)
					"[${support.label}](${doc.asMarkdownPath()})"
				}
	}
}
