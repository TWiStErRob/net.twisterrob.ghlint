package net.twisterrob.ghlint.rules

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
import net.twisterrob.ghlint.testing.workflow
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
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - uses: actions/upload-artifact@v4
					        with:
					          if-no-files-found: ${value}
					          path: |
					            build/some/report/
				""".trimIndent(),
			)

			val results = check<FailFastActionsRule>(file)

			results shouldHave noFindings()
		}

		@Test fun `reports when input is missing`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - uses: actions/upload-artifact@v4
					        with:
					          path: |
					            build/some/report/
				""".trimIndent(),
			)

			val results = check<FailFastActionsRule>(file)


			results shouldHave singleFinding(
				"FailFastUploadArtifact",
				"""Step[actions/upload-artifact@v4] in Job[test] should have input `if-no-files-found: error`."""
			)
		}
	}

	@Nested
	inner class FailFastPublishUnitTestResultsTest {

		@ParameterizedTest
		@ValueSource(strings = ["true", "false"])
		fun `passes input is defined`(value: String) {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - uses: EnricoMi/publish-unit-test-result-action@v2
					        with:
					          action_fail_on_inconclusive: ${value}
					          junit_files: |
					            **/build/**/TEST-*.xml
				""".trimIndent(),
			)

			val results = check<FailFastActionsRule>(file)

			results shouldHave noFindings()
		}

		@Test fun `reports when input is missing`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - uses: EnricoMi/publish-unit-test-result-action@v2
					        with:
					          junit_files: |
					            **/build/**/TEST-*.xml
				""".trimIndent(),
			)

			val results = check<FailFastActionsRule>(file)

			results shouldHave singleFinding(
				"FailFastPublishUnitTestResults",
				@Suppress("detekt.MaxLineLength")
				"""Step[EnricoMi/publish-unit-test-result-action@v2] in Job[test] should have input `action_fail_on_inconclusive: true`."""
			)
		}
	}

	@Nested
	inner class FailFastPeterEvansCreatePullRequestTest {

		@Test fun `reports when action is used`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - uses: peter-evans/create-pull-request@v6
				""".trimIndent(),
			)

			val results = check<FailFastActionsRule>(file)

			results shouldHave singleFinding(
				"FailFastPeterEvansCreatePullRequest",
				"""Use `gh pr create` to open a PR instead of Step[peter-evans/create-pull-request@v6] in Job[test]."""
			)
		}

		@Test fun `reports when action is used with hash`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - uses: peter-evans/create-pull-request@b1ddad2c994a25fbc81a28b3ec0e368bb2021c50 # v6.0.0
					        with:
					          title: "Test"
				""".trimIndent(),
			)

			val results = check<FailFastActionsRule>(file)

			results shouldHave singleFinding(
				"FailFastPeterEvansCreatePullRequest",
				@Suppress("detekt.MaxLineLength")
				"""Use `gh pr create` to open a PR instead of Step[peter-evans/create-pull-request@b1ddad2c994a25fbc81a28b3ec0e368bb2021c50] in Job[test]."""
			)
		}
	}

	@Nested
	inner class FailFastSoftpropsGhReleaseTest {

		@ParameterizedTest
		@ValueSource(strings = ["true", "false"])
		fun `passes input is defined`(value: String) {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - uses: softprops/action-gh-release@v2
					        with:
					          fail_on_unmatched_files: ${value}
					          files: |
					            LICENCE
					            executable.exe
					            package*.zip
				""".trimIndent(),
			)

			val results = check<FailFastActionsRule>(file)

			results shouldHave noFindings()
		}

		@Test fun `reports when input is missing`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - uses: softprops/action-gh-release@v2
					        with:
					          files: |
					            LICENCE
					            executable.exe
					            package*.zip
				""".trimIndent(),
			)

			val results = check<FailFastActionsRule>(file)

			results shouldHave singleFinding(
				"FailFastSoftpropsGhRelease",
				"""Step[softprops/action-gh-release@v2] in Job[test] should have input `fail_on_unmatched_files: true`."""
			)
		}

		@Test fun `passes when input is not relevant`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - uses: softprops/action-gh-release@v2
				""".trimIndent(),
			)

			val results = check<FailFastActionsRule>(file)

			results shouldHave noFindings()
		}
	}

	@Test fun `passes on other actions`() {
		val file = workflow(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
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
			""".trimIndent(),
		)

		val results = check<FailFastActionsRule>(file)

		results shouldHave noFindings()
	}

	@Test fun `reports problems in actions`() {
		val results = check<FailFastActionsRule>(
			"""
				name: Test
				description: Test
				runs:
				  using: composite
				  steps:
				    - uses: actions/upload-artifact@v4
				      with:
				        path: |
				          build/some/report/
			""".trimIndent(),
			fileName = "action.yml",
		)

		results shouldHave singleFinding(
			"FailFastUploadArtifact",
			"""Step[actions/upload-artifact@v4] in Action["Test"] should have input `if-no-files-found: error`."""
		)
	}

	@Test fun `passes in actions`() {
		val results = check<FailFastActionsRule>(
			"""
				name: Test
				description: Test
				runs:
				  using: composite
				  steps:
				    - name: "Simple action usage."
				      uses: actions/checkout@v4
				
				    - uses: actions/upload-artifact@v4
				      with:
				        if-no-files-found: error
				        path: |
				          build/some/report/
				
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
			""".trimIndent(),
			fileName = "action.yml",
		)

		results shouldHave noFindings()
	}
}
