package net.twisterrob.ghlint.rule

import net.twisterrob.ghlint.model.Action
import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.InvalidContent
import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.results.Finding

public interface VisitorRule : Rule {

	override fun check(file: File): List<Finding> {
		val reporting = object : Reporting {
			val findings: MutableList<Finding> = mutableListOf()
			override fun report(finding: Finding) {
				findings.add(finding)
			}
		}
		visit(reporting, file)
		return reporting.findings
	}

	private fun visit(reporting: Reporting, file: File) {
		if (this !is WorkflowVisitor && this !is ActionVisitor && this !is InvalidContentVisitor) {
			throwInvalidImplementation()
		}
		when (file.content) {
			is Workflow -> if (this is WorkflowVisitor) visitWorkflowFile(reporting, file)
			is Action -> if (this is ActionVisitor) visitActionFile(reporting, file)
			is InvalidContent -> if (this is InvalidContentVisitor) visitInvalidContentFile(reporting, file)
		}
	}

	private fun throwInvalidImplementation(): Nothing {
		error(
			"A ${VisitorRule::class.simpleName ?: error("No name!")} must also implement at least one of "
					+ SUPPORTED_VISITORS.joinToString(separator = ", ") { it.simpleName ?: error("No name!") }
					+ " visitors."
		)
	}

	public companion object {

		private val SUPPORTED_VISITORS = listOf(
			WorkflowVisitor::class,
			ActionVisitor::class,
			InvalidContentVisitor::class
		)
	}
}
