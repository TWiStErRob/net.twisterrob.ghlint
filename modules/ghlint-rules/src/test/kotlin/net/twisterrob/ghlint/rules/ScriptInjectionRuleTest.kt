package net.twisterrob.ghlint.rules

import io.kotest.matchers.shouldHave
import io.kotest.matchers.throwable.shouldHaveMessage
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows

class ScriptInjectionRuleTest {

	@TestFactory fun metadata() = test(ScriptInjectionRule::class)

	@Nested
	inner class ShellScriptInjectionTest {

		@Test fun `passes when there's no variable usage`() {
			val results = check<ScriptInjectionRule>(
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

		@Test fun `passes when there's just an environment variable`() {
			val results = check<ScriptInjectionRule>(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - run: echo "${'$'}{VAR}"
					        env:
					          VAR: value
				""".trimIndent()
			)

			results shouldHave noFindings()
		}

		@Test fun `reports when there's possibility of shell injection`() {
			val results = check<ScriptInjectionRule>(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - run: echo "${'$'}{{ github.event.pull_request.title }}"
				""".trimIndent()
			)

			results shouldHave singleFinding(
				"ShellScriptInjection",
				"Step[#0] in Job[test] shell script contains GitHub Expressions.",
			)
		}
	}

	@Nested
	inner class JSScriptInjectionTest {

		@Test fun `passes when there's no variable usage`() {
			val results = check<ScriptInjectionRule>(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - uses: actions/github-script@v7
					        with:
					          script: return "Test";
				""".trimIndent()
			)

			results shouldHave noFindings()
		}

		@Test fun `passes when there's just an environment variable`() {
			val results = check<ScriptInjectionRule>(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - uses: actions/github-script@v7
					        env:
					          INPUT: value
					        with:
					          script: |
					            return process.env.INPUT;
				""".trimIndent()
			)

			results shouldHave noFindings()
		}

		@Test fun `passes when there's just string interpolation in JavaScript`() {
			val results = check<ScriptInjectionRule>(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - uses: actions/github-script@v7
					        env:
					          INPUT: value
					        with:
					          script: |
					            return `prefix ${'$'}{process.env.INPUT} suffix`;
				""".trimIndent()
			)

			results shouldHave noFindings()
		}

		@Test fun `reports when there's possibility of script injection`() {
			val results = check<ScriptInjectionRule>(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - uses: actions/github-script@v7
					        with:
					          script: |
					            const title = "${'$'}{{ github.event.pull_request.title }}";
					            return title;
				""".trimIndent()
			)

			results shouldHave singleFinding(
				"JSScriptInjection",
				// TODO re-think identifier, there could be multiple like this, it's not unique
				"Step[actions/github-script@v7] in Job[test] JavaScript contains GitHub Expressions.",
			)
		}

		@Test fun `reports when there's possibility of script injection regardless of version`() {
			val results = check<ScriptInjectionRule>(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - name: "Get title"
					        uses: actions/github-script@84724927e3e992f17768c17f57a47a85ea2a5160 # v7.0.1
					        with:
					          script: |
					            const title = "${'$'}{{ github.event.pull_request.title }}";
					            return title;
				""".trimIndent()
			)

			results shouldHave singleFinding(
				"JSScriptInjection",
				// TODO remove quotes from the name?
				"Step[\"Get title\"] in Job[test] JavaScript contains GitHub Expressions.",
			)
		}

		@Test fun `fails when there's a missing script`() {
			val result = assertThrows<RuntimeException> {
				check<ScriptInjectionRule>(
					"""
						on: push
						jobs:
						  test:
						    runs-on: test
						    steps:
						      - uses: actions/github-script@v7
						        with:
						          scirpt: "Wrong"
					""".trimIndent()
				)
			}

			result shouldHaveMessage "Key script is missing in the map."
		}

		@Test fun `fails when there's a missing with`() {
			val result = assertThrows<RuntimeException> {
				check<ScriptInjectionRule>(
					"""
						on: push
						jobs:
						  test:
						    runs-on: test
						    steps:
						      - uses: actions/github-script@v7
					""".trimIndent()
				)
			}

			result shouldHaveMessage "Key script is missing in the map."
		}
	}
}
