package net.twisterrob.ghlint.analysis

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.results.Location
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Rule
import net.twisterrob.ghlint.testing.validate
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

class SafeRuleTest {

	@Test fun metadata() {
		val subject = SafeRule(object : Rule {
			override val issues: List<Issue> = emptyList()
			override fun check(workflow: Workflow): List<Finding> =
				if (workflow.name == "Invalid") {
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
			override fun check(workflow: Workflow) = error("Should never be called.")
			override fun toString(): String = "test rule"
		})

		@Suppress("detekt.NullableToStringCall") // False positive: STOPSHIP
		subject.toString() shouldBe "SafeRule(test rule)"
	}

	@Test fun `propagates issues`() {
		val mockIssues: List<Issue> = listOf(mock(), mock())
		val subject = SafeRule(object : Rule {
			override val issues: List<Issue> = mockIssues
			override fun check(workflow: Workflow) = error("Should never be called.")
		})

		subject.issues shouldContainExactlyInAnyOrder mockIssues + SafeRule.RuleErrored
	}

	@Test fun `propagates no findings`() {
		val subject = SafeRule(object : Rule {
			override val issues get() = error("Should never be called.")
			override fun check(workflow: Workflow): List<Finding> = emptyList()
		})

		val findings = subject.check(mock())

		findings shouldBe emptyList()
	}

	@Test fun `propagates findings`() {
		val mockFindings: List<Finding> = listOf(mock(), mock())
		val subject = SafeRule(object : Rule {
			override val issues get() = error("Should never be called.")
			override fun check(workflow: Workflow) = mockFindings
		})

		val findings = subject.check(mock())

		findings shouldContainExactlyInAnyOrder mockFindings
	}

	@Test fun `propagates exception as finding`() {
		val stubFailure = RuntimeException("Fake failure")
		val subject = SafeRule(object : Rule {
			override val issues get() = error("Should never be called.")
			override fun check(workflow: Workflow) = throw stubFailure
		})
		val mockWorkflow: Workflow = mock()
		val mockLocation: Location = mock()
		whenever(mockWorkflow.location).thenReturn(mockLocation)

		val findings = subject.check(mockWorkflow)

		findings shouldHaveSize 1
		val finding = findings.single()
		finding.issue shouldBe SafeRule.RuleErrored
		finding.rule shouldBe subject
		finding.message shouldBe stubFailure.stackTraceToString()
		finding.location shouldBe mockLocation
	}

	@Test fun `propagates error as finding`() {
		val stubFailure = OutOfMemoryError("Fake failure")
		val subject = SafeRule(object : Rule {
			override val issues get() = error("Should never be called.")
			override fun check(workflow: Workflow) = throw stubFailure
		})
		val mockWorkflow: Workflow = mock()
		val mockLocation: Location = mock()
		whenever(mockWorkflow.location).thenReturn(mockLocation)

		val findings = subject.check(mockWorkflow)

		findings shouldHaveSize 1
		val finding = findings.single()
		finding.issue shouldBe SafeRule.RuleErrored
		finding.rule shouldBe subject
		finding.message shouldBe stubFailure.stackTraceToString()
		finding.location shouldBe mockLocation
	}
}
