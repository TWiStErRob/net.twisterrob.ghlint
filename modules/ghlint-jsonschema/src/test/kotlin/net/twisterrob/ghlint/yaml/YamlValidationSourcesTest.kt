package net.twisterrob.ghlint.yaml

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.string.shouldContain
import net.twisterrob.ghlint.testing.yaml
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.net.URI
import java.nio.file.Path
import kotlin.io.path.writeText

class YamlValidationSourcesTest {

	@Test fun `embedded workflow schema accepts valid content`() {
		val validation = YamlValidation(workflowSchema = URI("embedded:/github-workflow.json"))
		val node = SnakeYaml.loadRaw(yaml("""
			on: push
			jobs:
			  test:
			    runs-on: ubuntu-latest
			    steps:
			      - run: echo Hello
		""".trimIndent()))

		validation.validate(node, YamlValidationType.WORKFLOW).shouldBeEmpty()
	}

	@Test fun `embedded workflow schema still reports validation errors`() {
		val validation = YamlValidation(workflowSchema = URI("embedded:/github-workflow.json"))
		val node = SnakeYaml.loadRaw(yaml("on: push\njobs: {}"))

		validation.validate(node, YamlValidationType.WORKFLOW).shouldNotBeEmpty()
	}

	@Test fun `embedded action schema accepts valid content`() {
		val validation = YamlValidation(actionSchema = URI("embedded:/github-action.json"))
		val node = SnakeYaml.loadRaw(yaml("""
			name: Test
			description: Test action
			runs:
			  using: composite
			  steps:
			    - run: echo Hello
			      shell: bash
		""".trimIndent()))

		validation.validate(node, YamlValidationType.ACTION).shouldBeEmpty()
	}

	@Test fun `embedded action schema still reports validation errors`() {
		val validation = YamlValidation(actionSchema = URI("embedded:/github-action.json"))
		val node = SnakeYaml.loadRaw(yaml("name: Test"))

		validation.validate(node, YamlValidationType.ACTION).shouldNotBeEmpty()
	}

	@Test fun `local schema overrides only its selected file type`(@TempDir dir: Path) {
		val schema = dir.resolve("workflow.json").apply { writeText("false") }
		val validation = YamlValidation(schema.toUri(), URI("embedded:/github-action.json"))
		val node = SnakeYaml.loadRaw(yaml("name: Test"))

		validation.validate(node, YamlValidationType.WORKFLOW).single().error shouldContain "False schema"
		validation.validate(node, YamlValidationType.ACTION).shouldNotBeEmpty()
	}

	@Test fun `local schema resolves relative file references`(@TempDir dir: Path) {
		val schema = dir.resolve("schema.json").apply {
			writeText("""{"${'$'}ref": "required.json"}""")
		}
		dir.resolve("required.json").writeText("""{"required": ["expected"]}""")
		val validation = YamlValidation(workflowSchema = schema.toUri())
		val missing = SnakeYaml.loadRaw(yaml("other: value"))
		val present = SnakeYaml.loadRaw(yaml("expected: value"))

		validation.validate(missing, YamlValidationType.WORKFLOW).shouldNotBeEmpty()
		validation.validate(present, YamlValidationType.WORKFLOW).shouldBeEmpty()
	}

	@Test fun `local schema rejects network references`(@TempDir dir: Path) {
		val schema = dir.resolve("schema.json").apply {
			writeText("""{"${'$'}ref": "http://127.0.0.1:1/remote.json"}""")
		}
		val validation = YamlValidation(workflowSchema = schema.toUri())
		val node = SnakeYaml.loadRaw(yaml("name: Test"))

		val failure = shouldThrow<IllegalStateException> {
			validation.validate(node, YamlValidationType.WORKFLOW)
		}
		failure.message shouldContain "network access is disabled"
	}
}
