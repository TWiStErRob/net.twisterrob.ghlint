package net.twisterrob.ghlint.reporting.sarif

import io.github.detekt.sarif4k.ArtifactLocation
import io.github.detekt.sarif4k.Location
import io.github.detekt.sarif4k.Message
import io.github.detekt.sarif4k.MultiformatMessageString
import io.github.detekt.sarif4k.PhysicalLocation
import io.github.detekt.sarif4k.Region
import io.github.detekt.sarif4k.ReportingDescriptor
import io.github.detekt.sarif4k.Result
import io.github.detekt.sarif4k.Run
import io.github.detekt.sarif4k.SarifSchema210
import io.github.detekt.sarif4k.SarifSerializer
import io.github.detekt.sarif4k.Tool
import io.github.detekt.sarif4k.ToolComponent
import io.github.detekt.sarif4k.Version
import net.twisterrob.ghlint.reporting.Reporter
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.descriptionWithExamples
import net.twisterrob.ghlint.ruleset.RuleSet
import java.io.Writer
import java.nio.file.Path
import kotlin.io.path.absolute
import kotlin.io.path.bufferedWriter
import kotlin.io.path.relativeTo

public class SarifReporter(
	private val target: Writer,
	private val rootDir: Path,
	private val ruleSets: List<RuleSet>,
) : Reporter {

	override fun report(findings: List<Finding>) {
		val base = rootDir.absolute().toRealPath()
		val sarif = SarifSchema210(
			version = Version.The210,
			schema = "https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Schemata/sarif-schema-2.1.0.json",
			runs = listOf(
				Run(
					tool = Tool(
						driver = ToolComponent(
							name = "GHA-lint",
							version = BuildConfig.APP_VERSION,
							semanticVersion = BuildConfig.APP_VERSION,
							rules = ruleSets
								.asSequence()
								.flatMap { it.createRules() }
								.flatMap { it.issues }
								.map(::reportingDescriptor)
								.toList(),
						),
					),
					originalURIBaseIDS = mapOf(
						"%SRCROOT%" to ArtifactLocation(uri = base.toUri().toString()),
					),
					results = findings.map { finding ->
						result(finding, base)
					},
				),
			),
		)
		target.write(SarifSerializer.toJson(sarif))
	}

	public companion object {

		public fun report(ruleSets: List<RuleSet>, findings: List<Finding>, target: Path, rootDir: Path) {
			target.bufferedWriter().use { writer ->
				val reporter = SarifReporter(
					target = writer,
					rootDir = rootDir,
					ruleSets = ruleSets,
				)
				reporter.report(findings)
			}
		}
	}
}

private fun reportingDescriptor(issue: Issue): ReportingDescriptor =
	ReportingDescriptor(
		id = issue.id,
		name = issue.title,
		shortDescription = MultiformatMessageString(
			text = issue.title,
		),
		fullDescription = MultiformatMessageString(
			text = "See fullDescription markdown.",
			markdown = issue.description,
		),
		help = MultiformatMessageString(
			text = "See help markdown.",
			markdown = issue.descriptionWithExamples + issue.helpLink,
		),
		helpURI = "https://ghlint.twisterrob.net/issues/default/${issue.id}/", // not visible on GH UI.
		// TODO defaultConfiguration = ReportingConfiguration(level = issue.severity), //
		// TODO properties = PropertyBag(tags = listOf("tag1", "tag2")), // visible in detail view on GH UI.
	)

private fun result(finding: Finding, base: Path): Result {
	val file = Path.of(finding.location.file.path).absolute().toRealPath()
	return Result(
		message = Message(
			text = finding.message,
		),
		ruleID = finding.issue.id,
		locations = listOf(
			Location(
				physicalLocation = PhysicalLocation(
					artifactLocation = ArtifactLocation(
						uriBaseID = "%SRCROOT%",
						uri = file.relativeTo(base).toString(),
					),
					region = with(finding.location) {
						Region(
							startLine = start.line.number.toLong(),
							startColumn = start.column.number.toLong(),
							endLine = end.line.number.toLong(),
							endColumn = end.column.number.toLong(),
						)
					},
				),
			),
		),
	)
}

private val Issue.helpLink: String
	get() = "\n---\nSee also the [online documentation](https://ghlint.twisterrob.net/issues/default/${id}/)."
