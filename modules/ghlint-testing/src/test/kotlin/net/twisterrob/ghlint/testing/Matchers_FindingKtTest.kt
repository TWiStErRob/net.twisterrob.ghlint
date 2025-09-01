package net.twisterrob.ghlint.testing

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldHave
import io.kotest.matchers.shouldNotHave
import io.kotest.matchers.throwable.shouldHaveMessage
import net.twisterrob.ghlint.results.Finding
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@Suppress("detekt.ClassNaming")
class Matchers_FindingKtTest {

	@Nested
	inner class `noFindings Test` {

		@Nested
		inner class positive {

			@Test fun `empty passes`() {
				val results: List<Finding> = emptyList()

				results shouldHave noFindings()
			}

			@Test fun `one finding fails`() {
				val results: List<Finding> = listOf(
					testFinding(TestRule(), TestRule.TestIssue1, message = "message"),
				)

				val failure = shouldThrow<AssertionError> {
					results shouldHave noFindings()
				}

				failure shouldHaveMessage """
					Collection should have size 0 but has size 1. Values: [Finding(
						rule=toString of TestRule,
						issue=TestIssue1,
						location=test.file/1:2-3:4,
						message=message
					)]
					expected:<0> but was:<1>
				""".trimIndent()
			}

			@Test fun `multiple findings fails`() {
				val results: List<Finding> = listOf(
					testFinding(TestRule(), TestRule.TestIssue1, message = "message1"),
					testFinding(TestRule(), TestRule.TestIssue2, message = "message2"),
				)

				val failure = shouldThrow<AssertionError> {
					results shouldHave noFindings()
				}

				failure shouldHaveMessage """
					Collection should have size 0 but has size 2. Values: [Finding(
						rule=toString of TestRule,
						issue=TestIssue1,
						location=test.file/1:2-3:4,
						message=message1
					), Finding(
						rule=toString of TestRule,
						issue=TestIssue2,
						location=test.file/1:2-3:4,
						message=message2
					)]
					expected:<0> but was:<2>
				""".trimIndent()
			}
		}
	}

	@Nested
	inner class `singleFinding Test` {

		@Nested
		inner class positive {

			@Test fun `exact match passes`() {
				val results: List<Finding> = listOf(
					testFinding(TestRule(), TestRule.TestIssue1, message = "message"),
				)

				results shouldHave singleFinding(
					issue = TestRule.TestIssue1.id,
					message = "message",
				)
			}

			@Test fun `different message fails`() {
				val results: List<Finding> = listOf(
					testFinding(TestRule(), TestRule.TestIssue1, message = "message"),
				)

				val failure = shouldThrow<AssertionError> {
					results shouldHave singleFinding(
						issue = TestRule.TestIssue1.id,
						message = "not the right message",
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
					testFinding(TestRule(), TestRule.TestIssue1, message = "message"),
				)

				val failure = shouldThrow<AssertionError> {
					results shouldHave singleFinding(
						issue = "WrongIssueId",
						message = "message",
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
					testFinding(TestRule(), TestRule.TestIssue1, message = "message"),
				)

				val failure = shouldThrow<AssertionError> {
					results shouldHave singleFinding(
						issue = "WrongIssueId",
						message = "not the right message",
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
						issue = TestRule.TestIssue1.id,
						message = "message",
					)
				}

				failure shouldHaveMessage """
					Collection should have size 1 but has size 0. Values: []
					expected:<1> but was:<0>
				""".trimIndent()
			}

			@Test fun `duplicate fails`() {
				val results: List<Finding> = listOf(
					testFinding(TestRule(), TestRule.TestIssue1, message = "message"),
					testFinding(TestRule(), TestRule.TestIssue1, message = "message"),
				)

				val failure = shouldThrow<AssertionError> {
					results shouldHave singleFinding(
						issue = TestRule.TestIssue1.id,
						message = "message",
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
					expected:<1> but was:<2>
				""".trimIndent()
			}

			@Test fun `multiple partial match fails`() {
				val results: List<Finding> = listOf(
					testFinding(TestRule(), TestRule.TestIssue1, message = "message"),
					testFinding(TestRule(), TestRule.TestIssue2, message = "message"),
				)

				val failure = shouldThrow<AssertionError> {
					results shouldHave singleFinding(
						issue = TestRule.TestIssue1.id,
						message = "message",
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
					expected:<1> but was:<2>
				""".trimIndent()
			}

			@Test fun `multiple different findings fails`() {
				val results: List<Finding> = listOf(
					testFinding(TestRule(), TestRule.TestIssue2, message = "message"),
					testFinding(TestRule(), TestRule.TestIssue3, message = "message"),
				)

				val failure = shouldThrow<AssertionError> {
					results shouldHave singleFinding(
						issue = TestRule.TestIssue1.id,
						message = "message",
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
					expected:<1> but was:<2>
				""".trimIndent()
			}
		}
	}

	@Nested
	inner class `singleFinding with location Test` {

		@Nested
		inner class positive {

			@Test fun `exact match passes`() {
				val results: List<Finding> = listOf(
					testFinding(TestRule(), TestRule.TestIssue1, message = "message"),
				)

				results shouldHave singleFinding(
					issue = TestRule.TestIssue1.id,
					message = "message",
					location = "test.file/1:2-3:4",
				)
			}

			@Test fun `different location fails`() {
				val results: List<Finding> = listOf(
					testFinding(TestRule(), TestRule.TestIssue1, message = "message"),
				)

				val failure = shouldThrow<AssertionError> {
					results shouldHave singleFinding(
						issue = TestRule.TestIssue1.id,
						message = "not the right message",
						location = "file.test/5:6-7:8",
					)
				}

				failure shouldHaveMessage """
					Could not find "TestIssue1: not the right message" at file.test/5:6-7:8 among findings:
					Finding(
						rule=toString of TestRule,
						issue=TestIssue1,
						location=test.file/1:2-3:4,
						message=message
					)
				""".trimIndent()
			}

			@Test fun `different message fails`() {
				val results: List<Finding> = listOf(
					testFinding(TestRule(), TestRule.TestIssue1, message = "message"),
				)

				val failure = shouldThrow<AssertionError> {
					results shouldHave singleFinding(
						issue = TestRule.TestIssue1.id,
						message = "not the right message",
						location = "test.file/1:2-3:4",
					)
				}

				failure shouldHaveMessage """
					Could not find "TestIssue1: not the right message" at test.file/1:2-3:4 among findings:
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
					testFinding(TestRule(), TestRule.TestIssue1, message = "message"),
				)

				val failure = shouldThrow<AssertionError> {
					results shouldHave singleFinding(
						issue = "WrongIssueId",
						message = "message",
						location = "test.file/1:2-3:4",
					)
				}

				failure shouldHaveMessage """
					Could not find "WrongIssueId: message" at test.file/1:2-3:4 among findings:
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
					testFinding(TestRule(), TestRule.TestIssue1, message = "message"),
				)

				val failure = shouldThrow<AssertionError> {
					results shouldHave singleFinding(
						issue = "WrongIssueId",
						message = "not the right message",
						location = "test.file/5:6-7:8",
					)
				}

				failure shouldHaveMessage """
					Could not find "WrongIssueId: not the right message" at test.file/5:6-7:8 among findings:
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
						issue = TestRule.TestIssue1.id,
						message = "message",
						location = "test.file/1:2-3:4",
					)
				}

				failure shouldHaveMessage """
					Collection should have size 1 but has size 0. Values: []
					expected:<1> but was:<0>
				""".trimIndent()
			}

			@Test fun `duplicate fails`() {
				val results: List<Finding> = listOf(
					testFinding(TestRule(), TestRule.TestIssue1, message = "message"),
					testFinding(TestRule(), TestRule.TestIssue1, message = "message"),
				)

				val failure = shouldThrow<AssertionError> {
					results shouldHave singleFinding(
						issue = TestRule.TestIssue1.id,
						message = "message",
						location = "test.file/1:2-3:4",
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
					expected:<1> but was:<2>
				""".trimIndent()
			}

			@Test fun `multiple partial match fails`() {
				val results: List<Finding> = listOf(
					testFinding(TestRule(), TestRule.TestIssue1, message = "message"),
					testFinding(TestRule(), TestRule.TestIssue2, message = "message"),
				)

				val failure = shouldThrow<AssertionError> {
					results shouldHave singleFinding(
						issue = TestRule.TestIssue1.id,
						message = "message",
						location = "test.file/1:2-3:4",
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
					expected:<1> but was:<2>
				""".trimIndent()
			}

			@Test fun `multiple different findings fails`() {
				val results: List<Finding> = listOf(
					testFinding(TestRule(), TestRule.TestIssue2, message = "message"),
					testFinding(TestRule(), TestRule.TestIssue3, message = "message"),
				)

				val failure = shouldThrow<AssertionError> {
					results shouldHave singleFinding(
						issue = TestRule.TestIssue1.id,
						message = "message",
						location = "test.file/1:2-3:4",
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
					expected:<1> but was:<2>
				""".trimIndent()
			}
		}
	}

	@Nested
	inner class `exactFindings Test` {

		@Nested
		inner class positive {

			@Test fun `empty match passes`() {
				val results: List<Finding> = emptyList()

				results shouldHave exactFindings()
			}

			@Test fun `exactly one match passes`() {
				val results: List<Finding> = listOf(
					testFinding(TestRule(), TestRule.TestIssue1, message = "message"),
				)

				results shouldHave exactFindings(
					aFinding(
						issue = TestRule.TestIssue1.id,
						message = "message",
					)
				)
			}

			@Test fun `exactly two match passes`() {
				val results: List<Finding> = listOf(
					testFinding(TestRule(), TestRule.TestIssue1, message = "message1"),
					testFinding(TestRule(), TestRule.TestIssue2, message = "message2"),
				)

				results shouldHave exactFindings(
					aFinding(
						issue = TestRule.TestIssue1.id,
						message = "message1",
					),
					aFinding(
						issue = TestRule.TestIssue2.id,
						message = "message2",
					),
				)
			}

			@Test fun `partial mismatch fails`() {
				val results: List<Finding> = listOf(
					testFinding(TestRule(), TestRule.TestIssue1, message = "message1"),
					testFinding(TestRule(), TestRule.TestIssue2, message = "message2"),
				)

				val failure = shouldThrow<AssertionError> {
					results shouldHave exactFindings(
						aFinding(
							issue = TestRule.TestIssue1.id,
							message = "message1",
						),
						aFinding(
							issue = TestRule.TestIssue2.id,
							message = "different message",
						),
					)
				}

				failure shouldHaveMessage """
					Could not find "TestIssue2: different message" among findings:
					Finding(
						rule=toString of TestRule,
						issue=TestIssue1,
						location=test.file/1:2-3:4,
						message=message1
					)
					Finding(
						rule=toString of TestRule,
						issue=TestIssue2,
						location=test.file/1:2-3:4,
						message=message2
					)
				""".trimIndent()
			}

			@Test fun `partial mismatch and more fails`() {
				val results: List<Finding> = listOf(
					testFinding(TestRule(), TestRule.TestIssue1, message = "message1"),
					testFinding(TestRule(), TestRule.TestIssue2, message = "extra finding"),
				)

				val failure = shouldThrow<AssertionError> {
					results shouldHave exactFindings(
						aFinding(
							issue = TestRule.TestIssue1.id,
							message = "message1",
						),
					)
				}

				failure shouldHaveMessage """
					Collection should have size 1 but has size 2. Values: [Finding(
						rule=toString of TestRule,
						issue=TestIssue1,
						location=test.file/1:2-3:4,
						message=message1
					), Finding(
						rule=toString of TestRule,
						issue=TestIssue2,
						location=test.file/1:2-3:4,
						message=extra finding
					)]
					expected:<1> but was:<2>
				""".trimIndent()
			}

			@Test fun `partial mismatch and less fails`() {
				val results: List<Finding> = listOf(
					testFinding(TestRule(), TestRule.TestIssue1, message = "message1"),
					testFinding(TestRule(), TestRule.TestIssue3, message = "message3"),
				)

				val failure = shouldThrow<AssertionError> {
					results shouldHave exactFindings(
						aFinding(
							issue = TestRule.TestIssue1.id,
							message = "message1",
						),
						aFinding(
							issue = TestRule.TestIssue2.id,
							message = "missing finding",
						),
						aFinding(
							issue = TestRule.TestIssue3.id,
							message = "message3",
						),
					)
				}

				failure shouldHaveMessage """
					Collection should have size 3 but has size 2. Values: [Finding(
						rule=toString of TestRule,
						issue=TestIssue1,
						location=test.file/1:2-3:4,
						message=message1
					), Finding(
						rule=toString of TestRule,
						issue=TestIssue3,
						location=test.file/1:2-3:4,
						message=message3
					)]
					expected:<3> but was:<2>
				""".trimIndent()
			}

			@Test fun `full mismatch fails`() {
				val results: List<Finding> = listOf(
					testFinding(TestRule(), TestRule.TestIssue1, message = "message1"),
					testFinding(TestRule(), TestRule.TestIssue2, message = "message2"),
				)

				val failure = shouldThrow<AssertionError> {
					results shouldHave exactFindings(
						aFinding(
							issue = TestRule.TestIssue3.id,
							message = "message3",
						),
						aFinding(
							issue = TestRule.TestIssue4.id,
							message = "message4",
						),
					)
				}

				failure shouldHaveMessage """
					Could not find "TestIssue3: message3" among findings:
					Finding(
						rule=toString of TestRule,
						issue=TestIssue1,
						location=test.file/1:2-3:4,
						message=message1
					)
					Finding(
						rule=toString of TestRule,
						issue=TestIssue2,
						location=test.file/1:2-3:4,
						message=message2
					)
				""".trimIndent()
			}

			@Test fun `full mismatch and more fails`() {
				val results: List<Finding> = listOf(
					testFinding(TestRule(), TestRule.TestIssue1, message = "message1"),
					testFinding(TestRule(), TestRule.TestIssue2, message = "message2"),
				)

				val failure = shouldThrow<AssertionError> {
					results shouldHave exactFindings(
						aFinding(
							issue = TestRule.TestIssue3.id,
							message = "message3",
						),
					)
				}

				failure shouldHaveMessage """
					Collection should have size 1 but has size 2. Values: [Finding(
						rule=toString of TestRule,
						issue=TestIssue1,
						location=test.file/1:2-3:4,
						message=message1
					), Finding(
						rule=toString of TestRule,
						issue=TestIssue2,
						location=test.file/1:2-3:4,
						message=message2
					)]
					expected:<1> but was:<2>
				""".trimIndent()
			}

			@Test fun `full mismatch and less fails`() {
				val results: List<Finding> = listOf(
					testFinding(TestRule(), TestRule.TestIssue1, message = "message1"),
				)

				val failure = shouldThrow<AssertionError> {
					results shouldHave exactFindings(
						aFinding(
							issue = TestRule.TestIssue2.id,
							message = "message1",
						),
						aFinding(
							issue = TestRule.TestIssue3.id,
							message = "message3",
						),
					)
				}

				failure shouldHaveMessage """
					Collection should have size 2 but has size 1. Values: [Finding(
						rule=toString of TestRule,
						issue=TestIssue1,
						location=test.file/1:2-3:4,
						message=message1
					)]
					expected:<2> but was:<1>
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
					testFinding(TestRule(), TestRule.TestIssue1),
				)
				results shouldHave onlyFindings(TestRule.TestIssue1.id)
			}

			@Test fun `multiple targeted results are matched`() {
				val results: List<Finding> = listOf(
					testFinding(TestRule(), TestRule.TestIssue1),
					testFinding(TestRule(), TestRule.TestIssue1),
					testFinding(TestRule(), TestRule.TestIssue1),
				)
				results shouldHave onlyFindings(TestRule.TestIssue1.id)
			}

			@Test fun `empty list fails to match`() {
				val results: List<Finding> = emptyList()

				val failure = shouldThrow<AssertionError> {
					results shouldHave onlyFindings(TestRule.TestIssue1.id)
				}

				failure shouldHaveMessage "Could not find exclusively `TestIssue1`s among findings:\nNo findings.\n"
			}

			@Test fun `different finding fails to match`() {
				val results: List<Finding> = listOf(
					testFinding(TestRule(), TestRule.TestIssue2),
				)

				val failure = shouldThrow<AssertionError> {
					results shouldHave onlyFindings(TestRule.TestIssue1.id)
				}

				failure shouldHaveMessage """
					Could not find exclusively `TestIssue1`s among findings:
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
					testFinding(TestRule(), TestRule.TestIssue2),
					testFinding(TestRule(), TestRule.TestIssue2),
					testFinding(TestRule(), TestRule.TestIssue2),
				)

				val failure = shouldThrow<AssertionError> {
					results shouldHave onlyFindings(TestRule.TestIssue1.id)
				}

				failure shouldHaveMessage """
					Could not find exclusively `TestIssue1`s among findings:
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
					testFinding(TestRule(), TestRule.TestIssue1),
					testFinding(TestRule(), TestRule.TestIssue2),
				)

				val failure = shouldThrow<AssertionError> {
					results shouldHave onlyFindings(TestRule.TestIssue1.id)
				}

				failure shouldHaveMessage """
					Could not find exclusively `TestIssue1`s among findings:
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
					testFinding(TestRule(), TestRule.TestIssue2),
					testFinding(TestRule(), TestRule.TestIssue1),
				)

				val failure = shouldThrow<AssertionError> {
					results shouldHave onlyFindings(TestRule.TestIssue1.id)
				}

				failure shouldHaveMessage """
					Could not find exclusively `TestIssue1`s among findings:
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
					testFinding(TestRule(), TestRule.TestIssue1),
				)
				val failure = shouldThrow<AssertionError> {
					results shouldNotHave onlyFindings(TestRule.TestIssue1.id)
				}

				failure shouldHaveMessage """
					Collection should not have `TestIssue1`s, but contained:
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
					testFinding(TestRule(), TestRule.TestIssue1),
				)

				results shouldNotHave onlyFindings(TestRule.TestIssue2.id)
			}

			@Test fun `multiple different finding matches`() {
				val results: List<Finding> = listOf(
					testFinding(TestRule(), TestRule.TestIssue1),
					testFinding(TestRule(), TestRule.TestIssue2),
					testFinding(TestRule(), TestRule.TestIssue3),
				)

				results shouldNotHave onlyFindings(TestRule.TestIssue4.id)
			}

			@Test fun `multiple different finding (including target) matches`() {
				val results: List<Finding> = listOf(
					testFinding(TestRule(), TestRule.TestIssue1),
					testFinding(TestRule(), TestRule.TestIssue2),
					testFinding(TestRule(), TestRule.TestIssue3),
					testFinding(TestRule(), TestRule.TestIssue4),
				)

				// This is questionable, so keeping the method internal.
				results shouldNotHave onlyFindings(TestRule.TestIssue3.id)
			}
		}
	}
}

