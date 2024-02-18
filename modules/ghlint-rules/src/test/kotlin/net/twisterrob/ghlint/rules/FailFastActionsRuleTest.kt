package net.twisterrob.ghlint.rules

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class FailFastActionsRuleTest {

	@TestFactory fun metadata() = test(FailFastActionsRule::class)

	@Nested
	inner class FailFastUploadArtifactTest {

		@ParameterizedTest
		@ValueSource(strings = ["error", "warn", "ignore"])
		fun `passes input is defined`(value: String) {
			val results = check<FailFastActionsRule>(
				"""
					jobs:
					  test:
					    steps:
					      - uses: actions/upload-artifact@v4
					        with:
					          if-no-files-found: ${value}
					          path: |
					            build/some/report/
				""".trimIndent()
			)

			results shouldHave noFindings()
		}

		@Test fun `reports when input is missing`() {
			val results = check<FailFastActionsRule>(
				"""
					jobs:
					  test:
					    steps:
					      - uses: actions/upload-artifact@v4
					        with:
					          path: |
					            build/some/report/
				""".trimIndent()
			)


			results shouldHave singleFinding(
				"FailFastUploadArtifact",
				"Step[actions/upload-artifact@v4] in Job[test] should have input `if-no-files-found: error`."
			)
		}
	}

	@Nested
	inner class FailFastPublishUnitTestResultsTest {

		@ParameterizedTest
		@ValueSource(strings = ["true", "false"])
		fun `passes input is defined`(value: String) {
			val results = check<FailFastActionsRule>(
				"""
					jobs:
					  test:
					    steps:
					      - uses: EnricoMi/publish-unit-test-result-action@v2
					        with:
					          action_fail_on_inconclusive: ${value}
					          junit_files: |
					            **/build/**/TEST-*.xml
				""".trimIndent()
			)

			results shouldHave noFindings()
		}

		@Test fun `reports when input is missing`() {
			val results = check<FailFastActionsRule>(
				"""
					jobs:
					  test:
					    steps:
					      - uses: EnricoMi/publish-unit-test-result-action@v2
					        with:
					          junit_files: |
					            **/build/**/TEST-*.xml
				""".trimIndent()
			)

			results shouldHave singleFinding(
				"FailFastPublishUnitTestResults",
				@Suppress("detekt.MaxLineLength")
				"Step[EnricoMi/publish-unit-test-result-action@v2] in Job[test] should have input `action_fail_on_inconclusive: true`."
			)
		}
	}

	@Nested
	inner class FailFastPeterEvansCreatePullRequestTest {

		@Test fun `reports when action is used`() {
			val results = check<FailFastActionsRule>(
				"""
					jobs:
					  test:
					    steps:
					      - uses: peter-evans/create-pull-request@v6
				""".trimIndent()
			)

			results shouldHave singleFinding(
				"FailFastPeterEvansCreatePullRequest",
				"Use `gh pr create` to open a PR instead of Step[peter-evans/create-pull-request@v6] in Job[test]."
			)
		}

		@Test fun `reports when action is used with hash`() {
			val results = check<FailFastActionsRule>(
				"""
					jobs:
					  test:
					    steps:
					      - uses: peter-evans/create-pull-request@b1ddad2c994a25fbc81a28b3ec0e368bb2021c50 # v6.0.0
					        with:
					          title: "Test"
				""".trimIndent()
			)

			results shouldHave singleFinding(
				"FailFastPeterEvansCreatePullRequest",
				@Suppress("detekt.MaxLineLength")
				"Use `gh pr create` to open a PR instead of Step[peter-evans/create-pull-request@b1ddad2c994a25fbc81a28b3ec0e368bb2021c50] in Job[test]."
			)
		}
	}

	@Test fun `passes on other actions`() {
		val results = check<FailFastActionsRule>(
			"""
				jobs:
				  test:
				    steps:
				      - name: "Simple action usage."
				        uses: actions/checkout@v4
				
				      - name: "Action with unrelated inputs."
				        uses: actions/setup-java@v3
				        with:
				          java-version: 17
				
				      - name: "Action with same input names."
				        uses: other/action@v0
				        with:
				          if-no-files-found: ignore
				          action_fail_on_inconclusive: false
				
				      - name: "Action with same name and input name."
				        uses: other/upload-artifact@v0
				        with:
				          if-no-files-found: ignore
				
				      - name: "Action with same name and input name."
				        uses: other/publish-unit-test-result-action@v0
				        with:
				          action_fail_on_inconclusive: false
			""".trimIndent()
		)

		results shouldHave noFindings()
	}
}
