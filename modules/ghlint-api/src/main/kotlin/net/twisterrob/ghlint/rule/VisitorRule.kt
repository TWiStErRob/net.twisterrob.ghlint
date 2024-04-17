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
		val content = file.content
		when {
			this is WorkflowVisitor && content is Workflow -> visitWorkflowFile(reporting, file)
			this is ActionVisitor && content is Action -> visitActionFile(reporting, file)
			this is InvalidContentVisitor && content is InvalidContent -> visitInvalidContentFile(reporting, file)
			else -> error(
				"A ${VisitorRule::class.simpleName ?: error("No name!")} must also implement one of "
						+ SUPPORTED_VISITORS.joinToString(separator = ", ") { it.simpleName ?: error("No name!") }
						+ " visitors."
			)
		}
	}

	public companion object {

		private val SUPPORTED_VISITORS = listOf(
			WorkflowVisitor::class,
			ActionVisitor::class,
			InvalidContentVisitor::class
		)
	}
}
