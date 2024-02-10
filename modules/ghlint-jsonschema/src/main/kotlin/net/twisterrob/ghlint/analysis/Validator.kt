package net.twisterrob.ghlint.analysis

import dev.harrel.jsonschema.Validator
import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Rule
import net.twisterrob.ghlint.yaml.Yaml
import net.twisterrob.ghlint.yaml.YamlValidation
import net.twisterrob.ghlint.yaml.resolve
import net.twisterrob.ghlint.yaml.toLocation
import org.snakeyaml.engine.v2.nodes.Node

public class Validator {

	public fun validateWorkflows(files: List<File>): List<Finding> {
		val rule = JsonSchemaValidationRule()
		return files.flatMap { file ->
			val root: Node = Yaml.load(file.content)
			val result: Validator.Result = YamlValidation.validate(file.content)
			result.errors
				.filter { it.error != "False schema always fails" }
				.map { error ->
					Finding(
						rule = rule,
						issue = ValidationIssue,
						location = root.resolve(error.instanceLocation).toLocation(file),
						message = "${error.error} (${error.instanceLocation})"
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
