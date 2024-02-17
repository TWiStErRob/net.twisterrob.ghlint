package net.twisterrob.ghlint.testing

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldHave
import io.kotest.matchers.shouldNotHave
import io.kotest.matchers.throwable.shouldHaveMessage
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.testing.TestRule.Companion.testFinding
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MatcherKtTest {

	@Nested
	inner class `onlyFindings Test` {

		@Nested
		inner class positive {

			@Test fun `single targeted finding is matched`() {
				val findings: List<Finding> = listOf(
					testFinding(TestRule.TestIssue1),
				)
				findings shouldHave onlyFindings(TestRule.TestIssue1.id)
			}

			@Test fun `multiple targeted findings are matched`() {
				val findings: List<Finding> = listOf(
					testFinding(TestRule.TestIssue1),
					testFinding(TestRule.TestIssue1),
					testFinding(TestRule.TestIssue1),
				)
				findings shouldHave onlyFindings(TestRule.TestIssue1.id)
			}

			@Test fun `empty list fails to match`() {
				val findings: List<Finding> = emptyList()

				val failure = shouldThrow<AssertionError> {
					findings shouldHave onlyFindings(TestRule.TestIssue1.id)
				}

				failure shouldHaveMessage "Could not find TestIssue1 among findings:\nNo findings.\n"
			}

			@Test fun `different finding fails to match`() {
				val findings: List<Finding> = listOf(
					testFinding(TestRule.TestIssue2),
				)

				val failure = shouldThrow<AssertionError> {
					findings shouldHave onlyFindings(TestRule.TestIssue1.id)
				}

				failure shouldHaveMessage """
					Could not find TestIssue1 among findings:
					Finding(
						rule=toString of TestRule,
						issue=TestIssue2,
						location=test.file/1:2-3:4,
						message=message
					)
				""".trimIndent()
			}

			@Test fun `multiple different findings fail to match`() {
				val findings: List<Finding> = listOf(
					testFinding(TestRule.TestIssue2),
					testFinding(TestRule.TestIssue2),
					testFinding(TestRule.TestIssue2),
				)

				val failure = shouldThrow<AssertionError> {
					findings shouldHave onlyFindings(TestRule.TestIssue1.id)
				}

				failure shouldHaveMessage """
					Could not find TestIssue1 among findings:
					Finding(
						rule=toString of TestRule,
						issue=TestIssue2,
						location=test.file/1:2-3:4,
						message=message
					)
					Finding(
						rule=toString of TestRule,
						issue=TestIssue2,
						location=test.file/1:2-3:4,
						message=message
					)
					Finding(
						rule=toString of TestRule,
						issue=TestIssue2,
						location=test.file/1:2-3:4,
						message=message
					)
				""".trimIndent()
			}

			@Test fun `mixed findings fails to match`() {
				val findings: List<Finding> = listOf(
					testFinding(TestRule.TestIssue1),
					testFinding(TestRule.TestIssue2),
				)

				val failure = shouldThrow<AssertionError> {
					findings shouldHave onlyFindings(TestRule.TestIssue1.id)
				}

				failure shouldHaveMessage """
					Could not find TestIssue1 among findings:
					Finding(
						rule=toString of TestRule,
						issue=TestIssue1,
						location=test.file/1:2-3:4,
						message=message
					)
					Finding(
						rule=toString of TestRule,
						issue=TestIssue2,
						location=test.file/1:2-3:4,
						message=message
					)
				""".trimIndent()
			}

			@Test fun `mixed findings fails to match (reverse)`() {
				val findings: List<Finding> = listOf(
					testFinding(TestRule.TestIssue2),
					testFinding(TestRule.TestIssue1),
				)

				val failure = shouldThrow<AssertionError> {
					findings shouldHave onlyFindings(TestRule.TestIssue1.id)
				}

				failure shouldHaveMessage """
					Could not find TestIssue1 among findings:
					Finding(
						rule=toString of TestRule,
						issue=TestIssue2,
						location=test.file/1:2-3:4,
						message=message
					)
					Finding(
						rule=toString of TestRule,
						issue=TestIssue1,
						location=test.file/1:2-3:4,
						message=message
					)
				""".trimIndent()
			}
		}

		@Nested
		inner class negative {

			@Test fun `same finding fails to match`() {
				val findings: List<Finding> = listOf(
					testFinding(TestRule.TestIssue1),
				)
				val failure = shouldThrow<AssertionError> {
					findings shouldNotHave onlyFindings(TestRule.TestIssue1.id)
				}

				failure shouldHaveMessage """
					Collection should not have TestIssue1, but contained:
					Finding(
						rule=toString of TestRule,
						issue=TestIssue1,
						location=test.file/1:2-3:4,
						message=message
					)
				""".trimIndent()
			}

			@Test fun `different finding matches`() {
				val findings: List<Finding> = listOf(
					testFinding(TestRule.TestIssue1),
				)

				findings shouldNotHave onlyFindings(TestRule.TestIssue2.id)
			}

			@Test fun `multiple different finding matches`() {
				val findings: List<Finding> = listOf(
					testFinding(TestRule.TestIssue1),
					testFinding(TestRule.TestIssue2),
					testFinding(TestRule.TestIssue3),
				)

				findings shouldNotHave onlyFindings(TestRule.TestIssue4.id)
			}

			@Test fun `multiple different finding (including target) matches`() {
				val findings: List<Finding> = listOf(
					testFinding(TestRule.TestIssue1),
					testFinding(TestRule.TestIssue2),
					testFinding(TestRule.TestIssue3),
					testFinding(TestRule.TestIssue4),
				)

				// STOPSHIP questionable
				findings shouldNotHave onlyFindings(TestRule.TestIssue3.id)
			}
		}
	}
}

