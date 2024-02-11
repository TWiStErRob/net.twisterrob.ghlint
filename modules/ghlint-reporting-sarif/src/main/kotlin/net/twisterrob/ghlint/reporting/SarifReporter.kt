package net.twisterrob.ghlint.reporting

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
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.descriptionWithExamples
import java.io.Writer
import java.nio.file.Path
import kotlin.io.path.absolute
import kotlin.io.path.bufferedWriter
import kotlin.io.path.relativeTo

public class SarifReporter(
	private val target: Writer,
	private val rootDir: Path,
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
							rules = findings.map { it.issue }.distinct().map { issue ->
								reportingDescriptor(issue)
							},
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

		public fun report(findings: List<Finding>, target: Path, rootDir: Path) {
			target.bufferedWriter().use { writer ->
				val reporter = SarifReporter(
					target = writer,
					rootDir = rootDir
				)
				reporter.report(findings)
			}
		}
	}
}

private fun reportingDescriptor(issue: Issue) = ReportingDescriptor(
	id = issue.id,
	name = issue.title,
	shortDescription = MultiformatMessageString(
		text = issue.title,
	),
	fullDescription = MultiformatMessageString(
		text = "See markdown.", // TODO strip markdown.
		markdown = issue.description,
	),
	help = MultiformatMessageString(
		text = "See markdown.", // TODO strip markdown.
		markdown = issue.descriptionWithExamples,
	),
	// TODO defaultConfiguration = ReportingConfiguration(level = issue.severity), //
	// TODO helpURI = "https://example.com/help", // not visible on GH UI.
	// TODO properties = PropertyBag(tags = listOf("tag1", "tag2")), // visible in detail view on GH UI.
)

private fun result(finding: Finding, base: Path): Result {
	val file = Path.of(finding.location.file.path).absolute().toRealPath()
	return Result(
		message = Message(
			markdown = finding.message,
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
							startLine = 1L + start.line.number,
							startColumn = 1L + start.column.number,
							endLine = 1L + end.line.number,
							endColumn = 1L + end.column.number,
						)
					},
				),
			),
		),
	)
}
