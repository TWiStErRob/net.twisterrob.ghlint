package net.twisterrob.ghlint.analysis

import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.ruleset.RuleSet
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.measureTimedValue
import kotlin.time.toDuration

public class Analyzer {

	@Suppress("detekt.ForbiddenMethodCall") // TODO logging.
	public fun analyze(files: List<File>, ruleSets: List<RuleSet>, verbose: Boolean): List<Finding> {
		val findings = files.flatMap { file ->
			if (verbose) {
				print("Analyzing ${file.location.path}...")
			}
			val fileFindings = measureTimedValue {
				ruleSets
					.asSequence()
					.flatMap { it.createRules() }
					.map(::SafeRule)
					.flatMap { rule -> rule.check(file) }
					.toList()
			}
			if (verbose) {
				val timing = fileFindings.duration.roundToMilliseconds()
				println(" found ${fileFindings.value.size} findings in ${timing}.")
			}
			fileFindings.value
		}
		return findings
	}
}

private fun Duration.roundToMilliseconds(): Duration =
	inWholeMilliseconds.toDuration(DurationUnit.MILLISECONDS)
