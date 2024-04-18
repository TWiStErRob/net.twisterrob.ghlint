package net.twisterrob.ghlint.docs.issues

import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.ruleset.RuleSet
import java.io.File
import java.nio.file.Path
import kotlin.io.path.createDirectories

internal class FileLocator(
	private val target: Path,
) {

	private fun ruleSetFolder(ruleSet: RuleSet): Path =
		target.resolve(ruleSet.id).apply { createDirectories() }

	fun ruleSetFile(ruleSet: RuleSet): Path =
		ruleSetFolder(ruleSet).resolve("index.md")

	fun issueFile(ruleSet: RuleSet, issue: Issue): Path =
		ruleSetFolder(ruleSet).resolve(issue.fileName)
}

private val Issue.fileName: String
	get() = this.id + ".md"

internal fun Path.asMarkdownPath(): String =
	this.toString().replace(File.separator, "/")
