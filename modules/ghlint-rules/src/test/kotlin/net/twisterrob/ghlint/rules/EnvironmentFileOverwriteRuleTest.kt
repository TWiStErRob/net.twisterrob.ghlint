package net.twisterrob.ghlint.rules

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.rules.testing.Shell.redirects
import net.twisterrob.ghlint.rules.testing.Shell.x
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
import net.twisterrob.ghlint.testing.yaml
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class EnvironmentFileOverwriteRuleTest {

	@TestFactory fun metadata() = test(EnvironmentFileOverwriteRule::class)

	@Test fun `passes when no environment file is used`() {
		val file = yaml(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - run: echo "Test"
			""".trimIndent(),
		)

		val results = check<EnvironmentFileOverwriteRule>(file)

		results shouldHave noFindings()
	}

	@Test fun `passes when no environment file is used in actions`() {
		val results = check<EnvironmentFileOverwriteRule>(
			"""
				name: "Test"
				description: Test
				runs:
				  using: composite
				  steps:
				    - run: echo "Test"
				      shell: bash
			""".trimIndent(),
			fileName = "action.yml",
		)

		results shouldHave noFindings()
	}

	@TestFactory
	fun `passes when environment file is mentioned`() =
		environmentFiles().map { environmentFile ->
			dynamicContainer(
				environmentFile,
				syntaxes(environmentFile).flatMap { (name, syntax) ->
					listOf(
						dynamicTest(name) {
							val file = yaml(
								"""
									on: push
									jobs:
									  test:
									    runs-on: test
									    steps:
									    # Intentionally unconventionally indented, see redirects().
									    - run: echo ${syntax}
								""".trimIndent(),
							)

							val results = check<EnvironmentFileOverwriteRule>(file)

							results shouldHave noFindings()
						},
						dynamicTest("${name} in actions") {
							val results = check<EnvironmentFileOverwriteRule>(
								"""
									name: "Test"
									description: Test
									runs:
									  using: composite
									  steps:
									    - run: echo ${syntax}
									      shell: bash
								""".trimIndent(),
								fileName = "action.yml",
							)

							results shouldHave noFindings()
						},
					)
				}
			)
		}

	@TestFactory
	fun `passes when environment file is appended`(): List<DynamicNode> =
		environmentFiles().map { environmentFile ->
			dynamicContainer(
				environmentFile,
				(redirects(">>") x syntaxes(environmentFile)).flatMap { (name, syntax) ->
					listOf(
						dynamicTest(name) {
							val file = yaml(
								"""
									on: push
									jobs:
									  test:
									    runs-on: test
									    steps:
									    # Intentionally unconventionally indented, see redirects().
									    - run: |
									        echo "Test" ${syntax}
								""".trimIndent(),
							)

							val results = check<EnvironmentFileOverwriteRule>(file)

							results shouldHave noFindings()
						},
						dynamicTest("${name} in actions") {
							val results = check<EnvironmentFileOverwriteRule>(
								"""
									name: "Test"
									description: Test
									runs:
									  using: composite
									  steps:
									    - run: |
									        echo "Test" ${syntax}
									      shell: bash
								""".trimIndent(),
								fileName = "action.yml",
							)

							results shouldHave noFindings()
						},
					)
				}
			)
		}

	@TestFactory
	fun `fails when environment file is overwritten`(): List<DynamicNode> =
		environmentFiles().map { environmentFile ->
			dynamicContainer(
				environmentFile,
				(redirects(">") x syntaxes(environmentFile)).flatMap { (name, syntax) ->
					listOf(
						dynamicTest(name) {
							val file = yaml(
								"""
									on: push
									jobs:
									  test:
									    runs-on: test
									    # Intentionally unconventionally indented, see redirects().
									    steps:
									    - run: |
									        echo "Test" ${syntax}
								""".trimIndent(),
							)

							val results = check<EnvironmentFileOverwriteRule>(file)

							results shouldHave singleFinding(
								"EnvironmentFileOverwritten",
								"Step[#0] in Job[test] overwrites environment file `${environmentFile}`."
							)
						},
						dynamicTest("${name} in actions") {
							val results = check<EnvironmentFileOverwriteRule>(
								"""
									name: "Test"
									description: Test
									runs:
									  using: composite
									  steps:
									    - run: |
									        echo "Test" ${syntax}
									      shell: bash
								""".trimIndent(),
								fileName = "action.yml",
							)

							results shouldHave singleFinding(
								"EnvironmentFileOverwritten",
								"""Step[#0] in Action["Test"] overwrites environment file `${environmentFile}`."""
							)
						},
					)
				}
			)
		}

	companion object {

		private fun environmentFiles(): List<String> =
			listOf("GITHUB_ENV", "GITHUB_OUTPUT", "GITHUB_PATH", "GITHUB_STEP_SUMMARY")

		private fun syntaxes(environmentFile: String): Map<String, String> =
			mapOf(
				"quoted/curlied" to """"${'$'}{${environmentFile}}"""",
				"quoted/un-curlied" to """"${'$'}${environmentFile}"""",
				"unquoted/curlied" to """${'$'}{${environmentFile}}""",
				"unquoted/un-curlied" to """${'$'}${environmentFile}""",
			)
	}
}
