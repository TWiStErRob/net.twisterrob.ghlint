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
import java.io.Writer
import java.nio.file.Path
import kotlin.io.path.absolute
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
								ReportingDescriptor(
									id = "gha-lint.${issue.id}",
									name = issue.description,
									shortDescription = MultiformatMessageString(
										text = issue.description,
									),
								)
							},
						),
					),
					originalURIBaseIDS = mapOf(
						"%SRCROOT%" to ArtifactLocation(uri = base.toUri().toString()),
					),
					results = findings.map { finding ->
						val file = Path.of(finding.location.file.path).absolute().toRealPath()
						Result(
							message = Message(
								text = finding.message,
							),
							ruleID = "gha-lint.${finding.issue.id}",
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
					},
				),
			),
		)
		target.write(SarifSerializer.toJson(sarif))
	}
}
