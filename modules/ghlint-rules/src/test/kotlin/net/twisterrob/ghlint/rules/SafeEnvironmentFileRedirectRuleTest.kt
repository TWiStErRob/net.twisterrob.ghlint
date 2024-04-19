package net.twisterrob.ghlint.rules

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.rules.testing.Shell.redirects
import net.twisterrob.ghlint.rules.testing.Shell.x
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class SafeEnvironmentFileRedirectRuleTest {

	@TestFactory fun metadata() = test(SafeEnvironmentFileRedirectRule::class)

	@Test fun `passes when no environment file is used`() {
		val results = check<SafeEnvironmentFileRedirectRule>(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - run: echo "Test"
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `passes when non-environment file is used`() {
		val results = check<SafeEnvironmentFileRedirectRule>(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - run: echo "Test" >> ${'$'}OUTPUT
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@TestFactory
	fun `passes when environment file is mentioned`() =
		environmentFiles().map { environmentFile ->
			dynamicContainer(
				environmentFile,
				(acceptedSyntaxes(environmentFile) + rejectedSyntaxes(environmentFile)).flatMap { (name, syntax) ->
					listOf(
						dynamicTest(name) {
							val results = check<SafeEnvironmentFileRedirectRule>(
								"""
									on: push
									jobs:
									  test:
									    runs-on: test
									    steps:
									      - run: echo ${syntax}
								""".trimIndent()
							)

							results shouldHave noFindings()
						},
					)
				}
			)
		}

	@TestFactory
	fun `passes environment file redirect using good syntax`() =
		environmentFiles().map { environmentFile ->
			dynamicContainer(
				environmentFile,
				((redirects(">") x acceptedSyntaxes(environmentFile)) +
						(redirects(">>") x acceptedSyntaxes(environmentFile)))
					.flatMap { (name, syntax) ->
						listOf(
							dynamicTest(name) {
								val results = check<SafeEnvironmentFileRedirectRule>(
									"""
										on: push
										jobs:
										  test:
										    runs-on: test
										    steps:
										    # Intentionally unconventionally indented, see redirects().
										    - run: |
										        echo ${syntax}
									""".trimIndent()
								)

								results shouldHave noFindings()
							},
						)
					}
			)
		}

	@TestFactory
	fun `fails environment file redirect using bad syntax`() =
		environmentFiles().map { environmentFile ->
			dynamicContainer(
				environmentFile,
				((redirects(">") x rejectedSyntaxes(environmentFile)) +
						(redirects(">>") x rejectedSyntaxes(environmentFile)))
					.flatMap { (name, syntax) ->
						listOf(
							dynamicTest(name) {
								val results = check<SafeEnvironmentFileRedirectRule>(
									"""
										on: push
										jobs:
										  test:
										    runs-on: test
										    steps:
										    # Intentionally unconventionally indented, see redirects().
										    - run: |
										        echo ${syntax}
									""".trimIndent()
								)

								results shouldHave singleFinding(
									"SafeEnvironmentFileRedirect",
									"""Step[#0] in Job[test] should be formatted as `>> "${'$'}{${environmentFile}}"`."""
								)
							},
						)
					}
			)
		}

	companion object {

		private fun environmentFiles(): List<String> =
			listOf("GITHUB_ENV", "GITHUB_OUTPUT", "GITHUB_PATH", "GITHUB_STEP_SUMMARY")

		private fun acceptedSyntaxes(environmentFile: String): Map<String, String> =
			mapOf(
				"quoted/curlied" to """"${'$'}{${environmentFile}}"""",
			)

		private fun rejectedSyntaxes(environmentFile: String): Map<String, String> =
			mapOf(
				"quoted/un-curlied" to """"${'$'}${environmentFile}"""",
				"unquoted/curlied" to """${'$'}{${environmentFile}}""",
				"unquoted/un-curlied" to """${'$'}${environmentFile}""",
			)
	}
}
