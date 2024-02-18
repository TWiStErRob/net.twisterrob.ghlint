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
	inner class `singleFinding Test` {

		@Nested
		inner class positive {

			@Test fun `exact match passes`() {
				val results: List<Finding> = listOf(
					testFinding(TestRule.TestIssue1, message = "message"),
				)

				results shouldHave singleFinding(
					TestRule.TestIssue1.id,
					"message"
				)
			}

			@Test fun `different message fails`() {
				val results: List<Finding> = listOf(
					testFinding(TestRule.TestIssue1, message = "message"),
				)

				val failure = shouldThrow<AssertionError> {
					results shouldHave singleFinding(
						TestRule.TestIssue1.id,
						"not the right message"
					)
				}

				failure shouldHaveMessage """
					Could not find "TestIssue1: not the right message" among findings:
					Finding(
						rule=toString of TestRule,
						issue=TestIssue1,
						location=test.file/1:2-3:4,
						message=message
					)
				""".trimIndent()
			}

			@Test fun `different issue fails`() {
				val results: List<Finding> = listOf(
					testFinding(TestRule.TestIssue1, message = "message"),
				)

				val failure = shouldThrow<AssertionError> {
					results shouldHave singleFinding(
						"WrongIssueId",
						"message"
					)
				}

				failure shouldHaveMessage """
					Could not find "WrongIssueId: message" among findings:
					Finding(
						rule=toString of TestRule,
						issue=TestIssue1,
						location=test.file/1:2-3:4,
						message=message
					)
				""".trimIndent()
			}

			@Test fun `mismatching details fails`() {
				val results: List<Finding> = listOf(
					testFinding(TestRule.TestIssue1, message = "message"),
				)

				val failure = shouldThrow<AssertionError> {
					results shouldHave singleFinding(
						"WrongIssueId",
						"not the right message"
					)
				}

				failure shouldHaveMessage """
					Could not find "WrongIssueId: not the right message" among findings:
					Finding(
						rule=toString of TestRule,
						issue=TestIssue1,
						location=test.file/1:2-3:4,
						message=message
					)
				""".trimIndent()
			}

			@Test fun `empty fails`() {
				val results: List<Finding> = emptyList()

				val failure = shouldThrow<AssertionError> {
					results shouldHave singleFinding(
						TestRule.TestIssue1.id,
						"message"
					)
				}

				failure shouldHaveMessage """
					Collection should have size 1 but has size 0. Values: []
				""".trimIndent()
			}

			@Test fun `duplicate fails`() {
				val results: List<Finding> = listOf(
					testFinding(TestRule.TestIssue1, message = "message"),
					testFinding(TestRule.TestIssue1, message = "message"),
				)

				val failure = shouldThrow<AssertionError> {
					results shouldHave singleFinding(
						TestRule.TestIssue1.id,
						"message"
					)
				}

				failure shouldHaveMessage """
					Collection should have size 1 but has size 2. Values: [Finding(
						rule=toString of TestRule,
						issue=TestIssue1,
						location=test.file/1:2-3:4,
						message=message
					), Finding(
						rule=toString of TestRule,
						issue=TestIssue1,
						location=test.file/1:2-3:4,
						message=message
					)]
				""".trimIndent()
			}

			@Test fun `multiple partial match fails`() {
				val results: List<Finding> = listOf(
					testFinding(TestRule.TestIssue1, message = "message"),
					testFinding(TestRule.TestIssue2, message = "message"),
				)

				val failure = shouldThrow<AssertionError> {
					results shouldHave singleFinding(
						TestRule.TestIssue1.id,
						"message"
					)
				}

				failure shouldHaveMessage """
					Collection should have size 1 but has size 2. Values: [Finding(
						rule=toString of TestRule,
						issue=TestIssue1,
						location=test.file/1:2-3:4,
						message=message
					), Finding(
						rule=toString of TestRule,
						issue=TestIssue2,
						location=test.file/1:2-3:4,
						message=message
					)]
				""".trimIndent()
			}

			@Test fun `multiple different findings fails`() {
				val results: List<Finding> = listOf(
					testFinding(TestRule.TestIssue2, message = "message"),
					testFinding(TestRule.TestIssue3, message = "message"),
				)

				val failure = shouldThrow<AssertionError> {
					results shouldHave singleFinding(
						TestRule.TestIssue1.id,
						"message"
					)
				}

				failure shouldHaveMessage """
					Collection should have size 1 but has size 2. Values: [Finding(
						rule=toString of TestRule,
						issue=TestIssue2,
						location=test.file/1:2-3:4,
						message=message
					), Finding(
						rule=toString of TestRule,
						issue=TestIssue3,
						location=test.file/1:2-3:4,
						message=message
					)]
				""".trimIndent()
			}
		}
	}

	@Nested
	inner class `onlyFindings Test` {

		@Nested
		inner class positive {

			@Test fun `single targeted finding is matched`() {
				val results: List<Finding> = listOf(
					testFinding(TestRule.TestIssue1),
				)
				results shouldHave onlyFindings(TestRule.TestIssue1.id)
			}

			@Test fun `multiple targeted results are matched`() {
				val results: List<Finding> = listOf(
					testFinding(TestRule.TestIssue1),
					testFinding(TestRule.TestIssue1),
					testFinding(TestRule.TestIssue1),
				)
				results shouldHave onlyFindings(TestRule.TestIssue1.id)
			}

			@Test fun `empty list fails to match`() {
				val results: List<Finding> = emptyList()

				val failure = shouldThrow<AssertionError> {
					results shouldHave onlyFindings(TestRule.TestIssue1.id)
				}

				failure shouldHaveMessage "Could not find TestIssue1 among findings:\nNo findings.\n"
			}

			@Test fun `different finding fails to match`() {
				val results: List<Finding> = listOf(
					testFinding(TestRule.TestIssue2),
				)

				val failure = shouldThrow<AssertionError> {
					results shouldHave onlyFindings(TestRule.TestIssue1.id)
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

			@Test fun `multiple different results fail to match`() {
				val results: List<Finding> = listOf(
					testFinding(TestRule.TestIssue2),
					testFinding(TestRule.TestIssue2),
					testFinding(TestRule.TestIssue2),
				)

				val failure = shouldThrow<AssertionError> {
					results shouldHave onlyFindings(TestRule.TestIssue1.id)
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

			@Test fun `mixed results fails to match`() {
				val results: List<Finding> = listOf(
					testFinding(TestRule.TestIssue1),
					testFinding(TestRule.TestIssue2),
				)

				val failure = shouldThrow<AssertionError> {
					results shouldHave onlyFindings(TestRule.TestIssue1.id)
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

			@Test fun `mixed results fails to match (reverse)`() {
				val results: List<Finding> = listOf(
					testFinding(TestRule.TestIssue2),
					testFinding(TestRule.TestIssue1),
				)

				val failure = shouldThrow<AssertionError> {
					results shouldHave onlyFindings(TestRule.TestIssue1.id)
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
				val results: List<Finding> = listOf(
					testFinding(TestRule.TestIssue1),
				)
				val failure = shouldThrow<AssertionError> {
					results shouldNotHave onlyFindings(TestRule.TestIssue1.id)
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
				val results: List<Finding> = listOf(
					testFinding(TestRule.TestIssue1),
				)

				results shouldNotHave onlyFindings(TestRule.TestIssue2.id)
			}

			@Test fun `multiple different finding matches`() {
				val results: List<Finding> = listOf(
					testFinding(TestRule.TestIssue1),
					testFinding(TestRule.TestIssue2),
					testFinding(TestRule.TestIssue3),
				)

				results shouldNotHave onlyFindings(TestRule.TestIssue4.id)
			}

			@Test fun `multiple different finding (including target) matches`() {
				val results: List<Finding> = listOf(
					testFinding(TestRule.TestIssue1),
					testFinding(TestRule.TestIssue2),
					testFinding(TestRule.TestIssue3),
					testFinding(TestRule.TestIssue4),
				)

				// This is questionable, so keeping the method internal.
				results shouldNotHave onlyFindings(TestRule.TestIssue3.id)
			}
		}
	}
}

