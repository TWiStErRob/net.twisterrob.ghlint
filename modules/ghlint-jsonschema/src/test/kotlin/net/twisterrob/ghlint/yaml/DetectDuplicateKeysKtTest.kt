package net.twisterrob.ghlint.yaml

import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.should
import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.testing.yaml
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

@Suppress("YAMLDuplicatedKeys") // This test is focusing on duplicate keys.
class DetectDuplicateKeysKtTest {

	private fun validate(@Language("yaml") yaml: String): List<YamlValidationProblem> {
		val node = SnakeYaml.loadRaw(yaml(yaml, "test.yaml"))
		return detectDuplicateKeys(node)
	}

	@Test fun `no duplication is fine`() {
		val yaml = """
			on:
			  workflow_dispatch:
			jobs:
			  test1:
			    uses: reusable/workflow.yml
			  test2:
			    uses: reusable/workflow.yml
		""".trimIndent()

		val result = validate(yaml)

		result should beEmpty()
	}

	@Test fun `duplicate job is reported`() {
		val yaml = """
			on:
			  workflow_dispatch:
			jobs:
			  test:
			    uses: reusable/workflow.yml
			  test:
			    uses: reusable/workflow.yml
		""".trimIndent()

		val result = validate(yaml)

		result shouldHave singleProblem(
			instanceLocation = "/jobs/test",
			error = "Duplicate key: test",
		)
	}

	@Test fun `duplicate runs-on is reported`() {
		val yaml = """
			on:
			  workflow_dispatch:
			jobs:
			  test:
			    runs-on: ubuntu-latest
			    runs-on: ubuntu-latest
			    steps:
			      - uses: actions/checkout@v4
		""".trimIndent()

		val result = validate(yaml)

		result shouldHave singleProblem(
			instanceLocation = "/jobs/test/runs-on",
			error = "Duplicate key: runs-on",
		)
	}

	@Test fun `duplicate env is reported`() {
		val yaml = """
			on:
			  workflow_dispatch:
			jobs:
			  test:
			    runs-on: ubuntu-latest
			    steps:
			      - uses: actions/checkout@v4
			        env:
			          KEY: value1
			          KEY: value2
		""".trimIndent()

		val result = validate(yaml)

		result shouldHave singleProblem(
			instanceLocation = "/jobs/test/steps/0/env/KEY",
			error = "Duplicate key: KEY",
		)
	}

	@Test fun `duplicate input is reported`() {
		val yaml = """
			on:
			  workflow_dispatch:
			jobs:
			  test:
			    uses: reusable/workflow.yml
			    with:
			      input: value1
			      input: value2
		""".trimIndent()

		val result = validate(yaml)

		result shouldHave singleProblem(
			instanceLocation = "/jobs/test/with/input",
			error = "Duplicate key: input",
		)
	}
}
