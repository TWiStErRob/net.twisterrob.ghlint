package net.twisterrob.ghlint.analysis

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.FileLocation
import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.model.Yaml
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Rule
import net.twisterrob.ghlint.testing.validate
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

class SafeRuleTest {

	@Test fun metadata() {
		val subject = SafeRule(object : Rule {
			override val issues: List<Issue> = emptyList()
			override fun check(file: File): List<Finding> =
				if ((file.content as Workflow).name == "Invalid") {
					error("Fake failure")
				} else {
					emptyList()
				}
		})

		validate(subject, SafeRule.RuleErrored)
	}

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

		findings shouldHaveSize 1
		val finding = findings.single()
		finding.issue shouldBe SafeRule.RuleErrored
		finding.rule shouldBe subject
		@Suppress("detekt.StringShouldBeRawString") // Cannot be, because we don't control stackTraceToString.
		finding.message shouldBe "toString of AlwaysFailingRule: RuntimeException errored while checking:  \n" +
				"Fake failure\n" +
				"```\n" +
				"${stubFailure.stackTraceToString()}\n" +
				"```"
		finding.location shouldBe fakeFile.content.location
	}

	@Test fun `propagates error as finding`() {
		val stubFailure = OutOfMemoryError("Fake failure")
		val subject = SafeRule(AlwaysFailingRule(stubFailure))
		val fakeFile: File = fakeFile()

		val findings = subject.check(fakeFile)

		findings shouldHaveSize 1
		val finding = findings.single()
		finding.issue shouldBe SafeRule.RuleErrored
		finding.rule shouldBe subject
		@Suppress("detekt.StringShouldBeRawString") // Cannot be, because we don't control stackTraceToString.
		finding.message shouldBe "toString of AlwaysFailingRule: OutOfMemoryError errored while checking:  \n" +
				"Fake failure\n" +
				"```\n" +
				"${stubFailure.stackTraceToString()}\n" +
				"```"
		finding.location shouldBe fakeFile.content.location
	}

	private fun fakeFile(): File =
		Yaml.from(FileLocation("test.yml"), "")
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
