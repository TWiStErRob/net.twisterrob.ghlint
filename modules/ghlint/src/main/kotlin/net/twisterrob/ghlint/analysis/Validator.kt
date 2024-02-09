package net.twisterrob.ghlint.analysis

import dev.harrel.jsonschema.Error
import dev.harrel.jsonschema.Validator
import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.results.Location
import net.twisterrob.ghlint.results.toLocation
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Rule
import net.twisterrob.ghlint.yaml.Yaml
import net.twisterrob.ghlint.yaml.array
import net.twisterrob.ghlint.yaml.getRequired
import org.snakeyaml.engine.v2.nodes.Node

internal class Validator {

	internal fun validateWorkflows(files: List<File>): List<Finding> {
		val rule = JsonSchemaValidationRule()
		return files.flatMap { file ->
			val root: Node = Yaml.load(file.readText())
			val result: Validator.Result = Yaml.validate(file.readText())
			result.errors.map { error ->
				Finding(
					rule,
					ValidationIssue,
					error.toLocation(file, root)
				)
			}
		}
	}

	private class JsonSchemaValidationRule : Rule {

		override fun check(workflow: Workflow): List<Finding> =
			error("Should never be called.")
	}

	internal companion object {

		private val ValidationIssue = Issue("JsonSchemaValidation", "JSON-Schema based validation problem.")
	}
}

private fun Error.toLocation(file: File, root: Node): Location =
	root.resolve(this.instanceLocation).toLocation(file)

private fun Node.resolve(instanceLocation: String): Node {
	require(instanceLocation.startsWith("/")) { "Instance location (${instanceLocation}) must start with /." }
	val path = instanceLocation.split("/").drop(1)
	return path.fold(this) { node, key ->
		when (node) {
			is org.snakeyaml.engine.v2.nodes.MappingNode -> node.getRequired(key)
			is org.snakeyaml.engine.v2.nodes.SequenceNode -> node.array[key.toInt()]
			else -> error("Cannot resolve $instanceLocation in $this")
		}
	}
}
