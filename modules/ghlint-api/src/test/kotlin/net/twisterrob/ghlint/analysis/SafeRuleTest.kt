package net.twisterrob.ghlint.analysis

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.model.Content
import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.InvalidContent
import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Rule
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.testIssue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

class SafeRuleTest {

	@TestFactory fun metadata() =
		testIssue(
			rule = SafeRule(object : Rule {
				override val issues: List<Issue> = emptyList()
				override fun check(file: File): List<Finding> =
					if ((file.content as Workflow).name == "Invalid") {
						error("Fake failure")
					} else {
						emptyList()
					}
			}),
			issue = SafeRule.RuleErrored
		)

	@Test fun `meaningful toString`() {
		val subject = SafeRule(object : Rule {
			override val issues get() = error("Should never be called.")
			override fun check(file: File) = error("Should never be called.")
			override fun toString(): String = "test rule"
		})

		@Suppress("detekt.NullableToStringCall") // REPORT False positive.
		subject.toString() shouldBe "SafeRule(test rule)"
	}

	@Test fun `propagates issues`() {
		val mockIssues: List<Issue> = listOf(mock(), mock())
		val subject = SafeRule(object : Rule {
			override val issues: List<Issue> = mockIssues
			override fun check(file: File) = error("Should never be called.")
		})

		subject.issues shouldContainExactlyInAnyOrder mockIssues + SafeRule.RuleErrored
	}

	@Test fun `propagates no findings`() {
		val subject = SafeRule(FixedFindingsRule(emptyList()))

		val findings = subject.check(mock())

		findings shouldBe emptyList()
	}

	@Test fun `propagates findings`() {
		val mockFindings: List<Finding> = listOf(mock(), mock())
		val subject = SafeRule(FixedFindingsRule(mockFindings))

		val findings = subject.check(mock())

		findings shouldContainExactlyInAnyOrder mockFindings
	}

	@Test fun `propagates exception as finding`() {
		val stubFailure = RuntimeException("Fake failure")
		val subject = SafeRule(AlwaysFailingRule(stubFailure))
		val fakeFile: File = fakeFile()

		val findings = subject.check(fakeFile)

		findings shouldHave singleFinding(
			"RuleErrored",
			@Suppress("detekt.StringShouldBeRawString") // Cannot be, because we don't control stackTraceToString.
			"toString of AlwaysFailingRule: RuntimeException errored while checking:\n" +
					"````\n" +
					stubFailure.stackTraceToString() +
					"````"
		)
		val finding = findings.single()
		finding.rule shouldBe subject
		finding.location shouldBe fakeFile.content.location
	}

	@Test fun `escapes exception markdown`() {
		val stubFailure = RuntimeException(
			"""
				## Markdown failure
				
				 * List `item` 1
				 * List `item` 2
				
				```kotlin
				fun some(code: String) = TODO()
				```
			""".trimIndent()
		)
		val subject = SafeRule(AlwaysFailingRule(stubFailure))
		val fakeFile: File = fakeFile()

		val findings = subject.check(fakeFile)

		findings shouldHave singleFinding(
			"RuleErrored",
			@Suppress("detekt.StringShouldBeRawString") // Cannot be, because we don't control stackTraceToString.
			"toString of AlwaysFailingRule: RuntimeException errored while checking:\n" +
					"````\n" +
					stubFailure.stackTraceToString() +
					"````"
		)

		val finding = findings.single()
		finding.rule shouldBe subject
		finding.location shouldBe fakeFile.content.location
	}

	@Test fun `propagates error as finding`() {
		val stubFailure = OutOfMemoryError("Fake failure")
		val subject = SafeRule(AlwaysFailingRule(stubFailure))
		val fakeFile: File = fakeFile()

		val findings = subject.check(fakeFile)

		findings shouldHave singleFinding(
			"RuleErrored",
			@Suppress("detekt.StringShouldBeRawString") // Cannot be, because we don't control stackTraceToString.
			"toString of AlwaysFailingRule: OutOfMemoryError errored while checking:\n" +
					"````\n" +
					stubFailure.stackTraceToString() +
					"````"
		)
		val finding = findings.single()
		finding.rule shouldBe subject
		finding.location shouldBe fakeFile.content.location
	}

	private fun fakeFile(): File {
		val content: Content = mock<InvalidContent>()
		whenever(content.location).thenReturn(mock())

		val file: File = mock()
		whenever(file.content).thenReturn(content)

		return file
	}
}

private class AlwaysFailingRule(private val stubFailure: Throwable) : Rule {

	override val issues get() = error("Should never be called.")
	override fun check(file: File): List<Finding> = throw stubFailure

	override fun toString(): String {
		val thisClass = this::class.simpleName ?: error("Cannot self-reflect")
		val errorClass = stubFailure::class.simpleName ?: error("Cannot reflect")
		return "toString of ${thisClass}: ${errorClass}"
	}
}

private class FixedFindingsRule(private val findings: List<Finding>) : Rule {

	override val issues get() = error("Should never be called.")
	override fun check(file: File): List<Finding> = findings
}
