package net.twisterrob.ghlint.reporting

import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.rule.Issue
import java.nio.file.Path
import kotlin.io.path.relativeTo

public class GitHubCommandReporter(
	private val repositoryRoot: Path,
	private val output: Appendable,
) : Reporter {

	override fun report(findings: List<Finding>) {
		findings.forEach {
			output.appendLine(it.render(repositoryRoot))
		}
	}
}

/**
 * Docs: https://docs.github.com/en/actions/using-workflows/workflow-commands-for-github-actions#setting-a-warning-message
 * Multiline support: Based on https://github.com/actions/toolkit/issues/193
 * Escaping ref: https://github.com/actions/toolkit/blob/415c42d/packages/core/src/command.ts#L38-L94
 */
@Suppress("detekt.MaxChainedCallsOnSameLine")
private fun Finding.render(repositoryRoot: Path): String {
	val filePath = Path.of(location.file.path)
	val repoRelativePath = filePath.toAbsolutePath().relativeTo(repositoryRoot.toAbsolutePath())
	val file = repoRelativePath.toString().escapeProperty()
	val start = location.start.line.number.toString().escapeProperty()
	val end = location.end.line.number.toString().escapeProperty()
	val title = issue.id.escapeProperty()
	val message = (message + issue.helpLink).escapeData()
	return "::warning file=${file},line=${start},endLine=${end},title=${title}::${message}"
}

private fun String.escapeProperty(): String =
	this
		.escapeData()
		.replace(":", "%3A")
		.replace(",", "%2C")

private fun String.escapeData(): String =
	this
		.replace("%", "%25")
		.replace("\r", "%0D")
		.replace("\n", "%0A")

private val Issue.helpLink: String
	get() = "\nSee also the [online documentation](https://ghlint.twisterrob.net/issues/default/${id}/)."
