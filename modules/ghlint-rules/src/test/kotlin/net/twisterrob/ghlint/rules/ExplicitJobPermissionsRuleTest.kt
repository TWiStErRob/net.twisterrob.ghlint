package net.twisterrob.ghlint.rules

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.invoke
import net.twisterrob.ghlint.testing.jupiter.AcceptFailingDynamicTest
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
import net.twisterrob.ghlint.testing.workflow
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class ExplicitJobPermissionsRuleTest {

	@AcceptFailingDynamicTest(
		displayName = "Issue MissingJobPermissions compliant example #2 has no findings",
		reason = "Rule triggers another another finding, but it's acceptable for this issue.",
		acceptableFailure = "^\\Q"
				+ "Collection should have size 0 but has size 1. Values: ["
				+ "Finding(\n"
				+ "	rule=net.twisterrob.ghlint.rules.ExplicitJobPermissionsRule@\\E[0-9a-f]+\\Q,\n"
				+ "	issue=ExplicitJobPermissions,\n"
				+ "	location=compliant/example.yml/5:3-5:10,\n"
				+ "	message=Job[example] should have explicit permissions.\n"
				+ ")"
				+ "]"
				+ "\\E$"
	)
	@Suppress("detekt.StringShouldBeRawString") // Cannot trimIndent on annotation parameters.
	@TestFactory fun metadata() = test(ExplicitJobPermissionsRule::class)

	@Nested
	inner class MissingJobPermissionsTest {

		@Test fun `reports when there are no permissions declared`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - run: echo "Test"
				""".trimIndent(),
			)

			val results = check<ExplicitJobPermissionsRule>(file)

			results shouldHave singleFinding(
				issue = "MissingJobPermissions",
				message = "Job[test] is missing permissions.",
				location = file("test"),
			)
		}

		@Test fun `reports when there are no permissions declared on reusable call`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    uses: reusable/workflow.yml
				""".trimIndent(),
			)

			val results = check<ExplicitJobPermissionsRule>(file)

			results shouldHave singleFinding(
				issue = "MissingJobPermissions",
				message = "Job[test] is missing permissions.",
				location = file("test"),
			)
		}

		@Test fun `passes explicit permissions on the job`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    permissions:
					      contents: read
					    steps:
					      - run: echo "Test"
				""".trimIndent(),
			)

			val results = check<ExplicitJobPermissionsRule>(file)

			results shouldHave noFindings()
		}

		@Test fun `passes explicit permissions on the reusable workflow call`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    uses: reusable/workflow.yml
					    permissions:
					      contents: read
				""".trimIndent(),
			)

			val results = check<ExplicitJobPermissionsRule>(file)

			results shouldHave noFindings()
		}

		@Test fun `reports the job that has no permissions declared`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  has-perms-1:
					    runs-on: test
					    permissions:
					      contents: read
					    steps:
					      - run: echo "Test"
					  test:
					    runs-on: test
					    steps:
					      - run: echo "Test"
					  has-perms-2:
					    runs-on: test
					    permissions:
					      contents: read
					    steps:
					      - run: echo "Test"
				""".trimIndent(),
			)

			val results = check<ExplicitJobPermissionsRule>(file)

			results shouldHave singleFinding(
				issue = "MissingJobPermissions",
				message = "Job[test] is missing permissions.",
				location = file("test", 2),
			)
		}

		@Test fun `reports reusable workflow call that has no permissions declared`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  has-perms-1:
					    runs-on: test
					    permissions:
					      contents: read
					    steps:
					      - run: echo "Test"
					  test:
					    uses: reusable/workflow.yml
					  has-perms-2:
					    runs-on: test
					    permissions:
					      contents: read
					    steps:
					      - run: echo "Test"
				""".trimIndent(),
			)

			val results = check<ExplicitJobPermissionsRule>(file)

			results shouldHave singleFinding(
				issue = "MissingJobPermissions",
				message = "Job[test] is missing permissions.",
				location = file("test", 2),
			)
		}

		@Test fun `passes explicit no permissions on the job`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    permissions: {}
					    steps:
					      - run: echo "Test"
				""".trimIndent(),
			)

			val results = check<ExplicitJobPermissionsRule>(file)

			results shouldHave noFindings()
		}

		@Test fun `passes explicit no permissions on reusable workflow call`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    uses: reusable/workflow.yml
					    permissions: {}
				""".trimIndent(),
			)

			val results = check<ExplicitJobPermissionsRule>(file)

			results shouldHave noFindings()
		}
	}

	@Nested
	inner class ExplicitJobPermissionsTest {

		@Test fun `reports when permissions are on the workflow level for normal job`() {
			val file = workflow(
				"""
					on: push
					permissions:
					  contents: read
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - run: echo "Test"
				""".trimIndent(),
			)

			val results = check<ExplicitJobPermissionsRule>(file)

			results shouldHave singleFinding(
				issue = "ExplicitJobPermissions",
				message = "Job[test] should have explicit permissions.",
				location = file("test"),
			)
		}

		@Test fun `reports when permissions are on the workflow level for reusable job`() {
			val file = workflow(
				"""
					on: push
					permissions:
					  contents: read
					jobs:
					  test:
					    uses: reusable/workflow.yml
				""".trimIndent(),
			)

			val results = check<ExplicitJobPermissionsRule>(file)

			results shouldHave singleFinding(
				issue = "ExplicitJobPermissions",
				message = "Job[test] should have explicit permissions.",
				location = file("test"),
			)
		}

		@Test fun `passes when permissions are on the job-level for normal job`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    permissions:
					      contents: read
					    steps:
					      - run: echo "Test"
				""".trimIndent(),
			)

			val results = check<ExplicitJobPermissionsRule>(file)

			results shouldHave noFindings()
		}

		@Test fun `passes when permissions are on the job-level for reusable job`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    uses: reusable/workflow.yml
					    permissions:
					      contents: read
				""".trimIndent(),
			)

			val results = check<ExplicitJobPermissionsRule>(file)

			results shouldHave noFindings()
		}

		@Test fun `should report when redundant workflow level permissions and job-level permissions`() {
			val file = workflow(
				"""
					on: push
					permissions:
					  pull-requests: write
					  contents: write
					jobs:
					  test:
					    runs-on: ubuntu-latest
					    permissions:
					      pull-requests: write
					      contents: write
					    steps:
					      - uses: actions/checkout@v4
				""".trimIndent(),
			)

			val results = check<ExplicitJobPermissionsRule>(file)

			results shouldHave singleFinding(
				issue = "ExplicitJobPermissions",
				message = "Workflow[test] has redundant permissions.",
				location = file("jobs"),
			)
		}
	}
}
