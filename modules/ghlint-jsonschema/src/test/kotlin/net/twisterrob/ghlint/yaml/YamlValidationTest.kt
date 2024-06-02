package net.twisterrob.ghlint.yaml

import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.should
import net.twisterrob.ghlint.model.FileLocation
import net.twisterrob.ghlint.model.RawFile
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

class YamlValidationTest {

	private fun validate(@Language("yaml") yaml: String, expectedValid: Boolean): List<YamlValidationProblem> {
		val node = SnakeYaml.loadRaw(RawFile(FileLocation("test.yaml"), yaml))
		val results = YamlValidation.validate(node, YamlValidationType.WORKFLOW)
		if (expectedValid) {
			@Suppress("detekt.ForbiddenMethodCall") // Required to diagnose.
			results.forEach { println(it.toDisplayString()) }
			results should beEmpty()
		}
		return results
	}

	@Test
	fun `empty file fails validation`() {
		val yaml = """
		""".trimIndent()
		validate(yaml, false)
	}

	@Test
	fun `minimal workflow passes validation`() {
		val yaml = """
			on:
			  workflow_dispatch:
			jobs:
			  build:
			    runs-on: ubuntu-latest
			    steps:
			      - uses: actions/checkout@v4
		""".trimIndent()
		validate(yaml, true)
	}

	@Test
	fun `empty jobs fails validation`() {
		val yaml = """
			on:
			  workflow_dispatch:
			jobs:
		""".trimIndent()
		validate(yaml, false)
	}

	@Test
	fun `missing jobs fails validation`() {
		val yaml = """
			on:
			  workflow_dispatch:
		""".trimIndent()
		validate(yaml, false)
	}

	@Test
	fun `duplicate job fails validation`() {
		@Suppress("YAMLDuplicatedKeys")
		val yaml = """
			on:
			  workflow_dispatch:
			jobs:
			  job:
			    uses: reusable/workflow.yml
			  job:
			    uses: reusable/workflow.yml
		""".trimIndent()
		validate(yaml, false)
	}
}
